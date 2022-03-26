package core.inference

import inference.InferenceGrpcKt
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.io.Closeable
import java.util.concurrent.TimeUnit

class B3nzeneClient(private val channel: ManagedChannel) : Closeable {
    private val b3nzeneStub = InferenceGrpcKt.InferenceCoroutineStub(channel)

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    companion object {

        fun connectionFrom(serverAddress: String, serverPort: Int): B3nzeneClient =
            B3nzeneClient(
                ManagedChannelBuilder
                    .forAddress(serverAddress, serverPort)
                    .build()
            )

    }

}
