package core.inference

import inference.InferenceGrpcKt
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.io.Closeable
import java.util.concurrent.TimeUnit

class KvineClient(private val channel: ManagedChannel) : Closeable {
    private val kvineStub = InferenceGrpcKt.InferenceCoroutineStub(this.channel)

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
