package route

import database.DatabaseConnection
import inference.InferenceRepository
import session.SessionRepository

data class BotContext(
    val databaseConnection: DatabaseConnection,
    val sessionRepository: SessionRepository,
    val inferenceRepository: InferenceRepository,
)