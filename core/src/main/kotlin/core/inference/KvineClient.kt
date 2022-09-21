package core.inference

import com.google.protobuf.kotlin.toByteString
import core.session.GameResult
import core.session.Token
import inference.InferenceGrpcKt
import inference.InferenceProto
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import jrenju.Board
import jrenju.notation.Color
import jrenju.protocol.AiPreset
import jrenju.protocol.Solution
import jrenju.protocol.`Solution$`
import scala.Enumeration
import java.io.Closeable
import java.util.concurrent.TimeUnit

class KvineClient(private val channel: ManagedChannel) : Closeable {

    private val kvineStub = InferenceGrpcKt
        .InferenceCoroutineStub(this.channel)
        .withWaitForReady()

    private fun Board.toProtoStatus(): InferenceProto.Status =
        InferenceProto.Status.newBuilder()
            .setBoard(this.field().toByteString())
            .setMoves(this.moves())
            .setLastMove(this.lastMove())
            .build()

    private fun AiPreset.toProtoAiPreset(): InferenceProto.Begins.AiPreset =
        InferenceProto.Begins.AiPreset.newBuilder()
            .setMaxNodes(this.maxNodes())
            .setMaxDepth(this.maxDepth())
            .setAiVCFDepth(this.aiVcfDepth())
            .setPlayerVCFDepth(this.playerVcfDepth())
            .build()

    private fun Enumeration.Value.toProtoColor(): InferenceProto.Color =
        when (this) {
            Color.BLACK() -> InferenceProto.Color.BLACK
            Color.WHITE() -> InferenceProto.Color.WHITE
            else -> InferenceProto.Color.EMPTY
        }

    private fun Token.toProtoToken(): InferenceProto.Token =
        InferenceProto.Token.newBuilder()
            .setToken(this.token)
            .build()

    private fun GameResult.toProtoResult(): InferenceProto.Report.Result =
        InferenceProto.Report.Result.newBuilder().apply {
            winColor = when (this@toProtoResult) {
                is GameResult.Win -> this@toProtoResult.winColor.toProtoColor()
                is GameResult.Full -> InferenceProto.Color.EMPTY
            }

            cause = when (this@toProtoResult.cause) {
                GameResult.Cause.FIVE_IN_A_ROW -> InferenceProto.Report.Result.Cause.FIVE_IN_A_ROW
                GameResult.Cause.RESIGN, GameResult.Cause.TIMEOUT -> InferenceProto.Report.Result.Cause.RESIGN
                GameResult.Cause.DRAW -> InferenceProto.Report.Result.Cause.DRAW
            }
        }.build()

    private fun InferenceProto.Token.parseToken(): Token =
        Token(this.token)

    private fun InferenceProto.Solution.parseSolution(): Solution =
        `Solution$`.`MODULE$`.fromBinary(this.solution.toByteArray()).get()

    suspend fun begins(aiPreset: AiPreset, aiColor: Enumeration.Value, board: Board): Token {
        val request = InferenceProto.Begins.newBuilder()
            .setAiPreset(aiPreset.toProtoAiPreset())
            .setAiColor(aiColor.toProtoColor())
            .setInitStatus(board.toProtoStatus())
            .build()

        return this.kvineStub.begins(request).parseToken()
    }

    suspend fun update(token: Token, board: Board): Solution {
        val request = InferenceProto.Update.newBuilder()
            .setToken(token.toProtoToken())
            .setStatus(board.toProtoStatus())
            .build()

        return this.kvineStub.update(request).parseSolution()
    }

    suspend fun report(token: Token, board: Board, gameResult: GameResult) {
        val request = InferenceProto.Report.newBuilder()
            .setToken(token.toProtoToken())
            .setStatus(board.toProtoStatus())
            .setResult(gameResult.toProtoResult())
            .build()

        this.kvineStub.report(request)
    }

    override fun close() {
        this.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    companion object {

        fun connectionFrom(serverAddress: String, serverPort: Int): KvineClient =
            KvineClient(
                ManagedChannelBuilder
                    .forAddress(serverAddress, serverPort)
                    .build()
            )

    }

}
