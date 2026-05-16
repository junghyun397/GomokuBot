package core.mintaka

import core.mintaka.types.BestMove
import core.mintaka.types.BoardData
import core.mintaka.types.Config
import core.mintaka.types.CreateSessionRequest
import core.mintaka.types.CreateSessionResponse
import core.mintaka.types.DurationSchema
import core.mintaka.types.GameStateData
import core.mintaka.types.LaunchSessionRequest
import core.mintaka.types.ResponseSchema
import core.mintaka.types.Timer
import core.mintaka.types.hashKey
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import renju.notation.HashKey

typealias MintakaCommand = core.mintaka.types.Command
typealias MintakaCommandResult = core.mintaka.types.CommandResult
typealias MintakaResponse = core.mintaka.types.Response

data class MintakaServer(
    val url: String,
    val password: String,
)

object MintakaProvider {
    val DefaultConfig = Config(
        rule_kind = "Renju",
        draw_condition = 225U,
        max_nodes_in_1k = 1_000U,
        max_depth = null,
        max_vcf_depth = 10,
        tt_size = 100000000,
        workers = 2U,
        pondering = false,
        dynamic_time = false,
        initial_timer = Timer(
            total_remaining = null,
            increment = DurationSchema(
                secs = 0,
                nanos = 0U,
            ),
            turn = null
        ),
        spawn_depth_specialist = false,
    )

    val EmptyBoard = core.mintaka.types.Board(
        hash_key = "",
        player_color = "Black",
        bitfield = listOf("", "")
    )

    private const val SESSION_TOKEN_HEADER_NAME = "mintaka-session-token"
    private const val SESSION_TOKEN_QUERY_NAME = "token"

    private val jsonCodec = Json {
        ignoreUnknownKeys = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonCodec)
        }
        install(SSE)
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun validateServer(server: MintakaServer): Boolean {
        return runCatching {
            val response = client.get("${server.url}/status")

            ensureSuccess(response)

            true
        }.getOrDefault(false)
    }

    data class CreateSessionHandle(
        val waiting: StateFlow<WaitingState?>,
        val session: Deferred<MintakaIdleSession>,
    )

    suspend fun createSession(server: MintakaServer, config: Config, initialState: BoardData): CreateSessionHandle {
        val waiting = MutableStateFlow<WaitingState?>(null)

        val session = coroutineScope { async {
            val response = client.post("${server.url}/sessions") {
                contentType(ContentType.Application.Json)
                setBody(CreateSessionRequest(
                    api_password = server.password.ifBlank { null },
                    config = config,
                    state = GameStateData(initialState, emptyList()),
                ))
            }

            ensureSuccess(response)

            val result = response.body<CreateSessionResponse>()

            MintakaIdleSession(
                sid = result.sid,
                token = result.token,
                hash = hashKey(result.hash),
            )
        } }

        return CreateSessionHandle(waiting, session)
    }

    suspend fun commandSession(server: MintakaServer, sid: String, token: HashKey, command: MintakaCommand): MintakaCommandResult {
        val response = client.post("${server.url}/sessions/$sid/commands") {
            contentType(ContentType.Application.Json)
            header(SESSION_TOKEN_HEADER_NAME, token)
            setBody(command)
        }

        ensureSuccess(response)

        return response.body<MintakaCommandResult>()
    }

    data class LaunchSessionHandle(
        val waiting: StateFlow<WaitingState?>,
        val begins: Deferred<ResponseSchema.Begins>,
        val status: StateFlow<ResponseSchema.Status?>,
        val bestmove: Deferred<BestMove>,
        val abort: () -> Unit,
    )

    suspend fun launchSession(
        server: MintakaServer,
        session: MintakaIdleSession,
    ): LaunchSessionHandle {
        val waiting = MutableStateFlow<WaitingState?>(null)
        val begins = CompletableDeferred<ResponseSchema.Begins>()
        val status = MutableStateFlow<ResponseSchema.Status?>(null)
        val bestmove = CompletableDeferred<BestMove>()

        scope.launch {
            try {
                client.sse(
                    "${server.url}/sessions/${session.sid}/stream?$SESSION_TOKEN_QUERY_NAME=${session.token}"
                ) {
                    coroutineScope {
                        val launchRequest = launch {
                            val response = client.post("${server.url}/sessions/${session.sid}/launch") {
                                contentType(ContentType.Application.Json)
                                header(SESSION_TOKEN_HEADER_NAME, session.token)
                                setBody(LaunchSessionRequest(
                                    position_hash = session.hash.value,
                                    nodes_polling_interval_in_ms = 1000U,
                                ))
                            }

                            ensureSuccess(response)
                        }

                        try {
                            incoming.first { event ->
                                when (event.event) {
                                    "Response" -> {
                                        when (val response = jsonCodec.decodeFromString<ResponseSchema>(
                                            event.data ?: error("Mintaka Response event has no data")
                                        )) {
                                            is ResponseSchema.Begins -> begins.complete(response)
                                            is ResponseSchema.Status -> status.value = response
                                        }

                                        false
                                    }
                                    "BestMove" -> {
                                        bestmove.complete( jsonCodec.decodeFromString<BestMove>(
                                            event.data ?: error("Mintaka BestMove event has no data")
                                        ))

                                        true
                                    }
                                    else -> false
                                }
                            }
                        } finally {
                            launchRequest.cancel()
                        }
                    }
                }
            } catch (cause: Throwable) {
                begins.completeExceptionally(cause)
                bestmove.completeExceptionally(cause)
            }
        }

        return LaunchSessionHandle(
            waiting = waiting,
            begins = begins,
            status = status,
            bestmove = bestmove,
            abort = {
                scope.launch {
                    runCatching {
                        val response = client.post("${server.url}/sessions/${session.sid}/abort") {
                            header(SESSION_TOKEN_HEADER_NAME, session.token)
                        }

                        ensureSuccess(response)
                    }
                }
            },
        )
    }

    suspend fun deleteSession(server: MintakaServer, sid: String, token: HashKey) {
        coroutineScope { launch {
            runCatching {
                val response = client.delete("${server.url}/sessions/$sid") {
                    header(SESSION_TOKEN_HEADER_NAME, token)
                }

                ensureSuccess(response)
            }
        } }
    }

    private suspend fun ensureSuccess(response: HttpResponse) {
        if (response.status.value in HttpStatusCode.OK.value until HttpStatusCode.MultipleChoices.value) {
            return
        }

        val body = response.bodyAsText()
        val detail = body.ifBlank { "${response.status.value} ${response.status.description}" }
        throw IllegalStateException(detail)
    }

}
