package database

data class DatabaseConnection(val unit: Unit = TODO()) {

    companion object {

        fun connectionFrom(serverURL: String, serverUname: String, serverPassword: String): DatabaseConnection = TODO()

    }

}