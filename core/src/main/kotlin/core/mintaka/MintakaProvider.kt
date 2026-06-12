package core.mintaka

import core.mintaka.types.*
import core.session.entities.MintakaIdleSession
import core.session.entities.WaitingState
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
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
import renju.Board
import renju.GameState
import renju.notation.Color
import renju.notation.HashKey
import renju.notation.Pos
import renju.notation.toStringOrNone
import java.util.*

typealias MintakaCommand = Command
typealias MintakaCommandResult = CommandResult
typealias MintakaResponse = Response

data class MintakaServer(
    val url: String,
    val password: String,
)

object MintakaProvider {

    private const val SESSION_TOKEN_HEADER_NAME = "mintaka-session-token"
    private const val SESSION_TOKEN_QUERY_NAME = "token"

    private val jsonCodec = Json {
        ignoreUnknownKeys = true
    }

    private val bitfieldEncoder = Base64.getUrlEncoder().withoutPadding()

    private val client = HttpClient(OkHttp) {
        engine {
            config {
                okhttp3.ConnectionPool(
                    maxIdleConnections = 100,
                    keepAliveDuration = 5,
                    timeUnit = java.util.concurrent.TimeUnit.MINUTES,
                )
            }
        }
        install(ContentNegotiation) {
            json(this@MintakaProvider.jsonCodec)
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

    suspend fun createSession(server: MintakaServer, engineLevel: EngineLevel, state: GameState): CreateSessionHandle {
        val waiting = MutableStateFlow<WaitingState?>(null)

        val session = coroutineScope { async {
            val response = client.post("${server.url}/sessions") {
                contentType(ContentType.Application.Json)
                setBody(CreateSessionRequest(
                    api_password = server.password.ifBlank { null },
                    config = engineLevel.config,
                    state = GameStateData(state.board.toBoardData(), state.history.toStringList()),
                ))
            }

            ensureSuccess(response)

            val result = response.body<CreateSessionResponse>()

            MintakaIdleSession(
                sid = result.sid,
                token = result.token,
                hash = HashKey.from(result.hash)!!,
            )
        } }

        return CreateSessionHandle(waiting, session)
    }

    suspend fun playSession(server: MintakaServer, session: MintakaIdleSession, hashKey: HashKey, pos: Pos): HashKey {
        val result = this.commandSession(server, session.sid, session.token, Command.Play(CommandPlayInner(
            hash=hashKey.toString(),
            pos=pos.toString(),
        )))

        return HashKey.from(result.hash_key)!!
    }

    suspend fun undoSession(server: MintakaServer, session: MintakaIdleSession, hashKey: HashKey): HashKey {
        val result = this.commandSession(server, session.sid, session.token, Command.Undo(CommandUndoInner(
            hash=hashKey.toString(),
        )))

        return HashKey.from(result.hash_key)!!
    }

    suspend fun syncSession(server: MintakaServer, session: MintakaIdleSession, state: GameState): HashKey {
        val result = this.commandSession(server, session.sid, session.token, Command.Sync(
            GameStateData(
                board_data = state.board.toBoardData(),
                history = state.history.sequence.map { it.toStringOrNone() },
            )
        ))

        return HashKey.from(result.hash_key)!!
    }

    private suspend fun commandSession(server: MintakaServer, sid: String, token: String, command: MintakaCommand): MintakaCommandResult {
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
        val begins: Deferred<Response.Begins>,
        val status: StateFlow<Response.Status?>,
        val bestmove: Deferred<BestMove>,
        val abort: () -> Unit,
    )

    fun launchSession(
        server: MintakaServer,
        session: MintakaIdleSession,
        hashKey: HashKey,
    ): LaunchSessionHandle {
        val waiting = MutableStateFlow<WaitingState?>(null)
        val begins = CompletableDeferred<Response.Begins>()
        val status = MutableStateFlow<Response.Status?>(null)
        val bestmove = CompletableDeferred<BestMove>()

        scope.launch {
            try {
                client.sse(
                    "${server.url}/sessions/${session.sid}/stream?$SESSION_TOKEN_QUERY_NAME=${session.token}"
                ) { coroutineScope {
                    val launchRequest = launch {
                        val response = client.post("${server.url}/sessions/${session.sid}/launch") {
                            contentType(ContentType.Application.Json)
                            header(SESSION_TOKEN_HEADER_NAME, session.token)
                            setBody(LaunchSessionRequest(
                                position_hash = hashKey.toString(),
                                nodes_polling_interval_in_ms = 1000U,
                            ))
                        }

                        ensureSuccess(response)
                    }

                    try {
                        incoming.first { event ->
                            when (event.event) {
                                "Response" -> {
                                    when (val response = jsonCodec.decodeFromString<Response>(
                                        event.data ?: error("Mintaka Response event has no data")
                                    )) {
                                        is Response.Begins -> begins.complete(response)
                                        is Response.Status -> status.value = response
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
                } }
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

    private fun Board.toBoardData(): BoardData {
        return BoardData(
            rule_kind = "Renju",
            hash_key = this.hashKey.toString(),
            player_color = this.playerColor.toString(),
            bitfield = listOf(
                toBitfield(Color.Black),
                toBitfield(Color.White),
            )
        )
    }

    private fun Board.toBitfield(color: Color): Bitfield {
        val bytes = ByteArray(32)

        for (idx in 0 until Pos.BOARD_SIZE) {
            if (this.stoneKind(Pos.fromIdx(idx)) == color) {
                val byteIndex = idx / 8
                val bitMask = 1 shl (idx % 8)
                bytes[byteIndex] = (bytes[byteIndex].toInt() or bitMask).toByte()
            }
        }

        return bitfieldEncoder.encodeToString(bytes)
    }

}
