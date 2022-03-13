package database

data class DatabaseConnection(val unit: Unit) {
    companion object {

        fun connectionFrom(serverURL: String, serverUname: String, serverPassword: String): DatabaseConnection = DatabaseConnection(Unit)

    }
}
