# Code Snippets
```kotlin
suspend fun generateAiSession(bot: BotContext, owner: User, aiLevel: AiLevel): GameSession {
//        val ownerHasBlack = Random(System.nanoTime()).nextBoolean()

//        val board = if (ownerHasBlack) Notation.EmptyBoard else ScalaBoard.newBoard()

    val ownerHasBlack = true
    val board = Notation.BoardIOInstance.fromBoardText(
        """
               A B C D E F G H I J K L M N O
            15 . . . . . . . . . . . . . . . 15
            14 . . . . . . . . . . . . . . . 14
            13 . . . . . . . . . . . . . . . 13
            12 . . . . . . . . . . . . . . . 12
            11 . . . . . . . . . . . . . . . 11
            10 . . . . . . . . . . . . . . . 10
             9 . . . . . . O O . . . . . . . 9
             8 . . . . . . . X X . . . . . . 8
             7 . . . . . . X O X . . . . . . 7
             6 . . . . . O . X . . . . . . . 6
             5 . . . . . . . . O . . . . . . 5
             4 . . . . . . . . . . . . . . . 4
             3 . . . . . . . . . . . . . . . 3
             2 . . . . . . . . . . . . . . . 2
             1 . . . . . . . . . . . . . . . 1
               A B C D E F G H I J K L M N O
        """.trimIndent(), Pos.fromCartesian("i8").get().idx()
    ).get()
}
```
