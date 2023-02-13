package core.interact.i18n

class LanguageJPN : LanguageENG() {

    override fun languageCode() = "JPN"

    override fun languageName() = "\uD83C\uDDEF\uD83C\uDDF5 日本語"
    override fun languageSuggestion() = "`/lang` `JPN` コマンドを使ってください。"

    override fun helpAboutEmbedTitle() = "GomokuBot / ヘルプ"
    override fun helpAboutEmbedDescription(platform: String) =
        "GomokuBotはDiscordで五目を楽しませてくれるオープンソースDiscord Botです。収集される基本データは強化学習モデル訓練に使われます。"
    override fun helpAboutEmbedDeveloper() = "ディベロッパー"
    override fun helpAboutEmbedRepository() = "Git 貯蔵所"
    override fun helpAboutEmbedVersion() = "バージョン"
    override fun helpAboutEmbedSupport() = "サポートチャンネル"

    override fun commandUsageEmbedTitle() = "GomokuBot / コマンド"
    override fun commandUsageHelp() = "`~help` ヘルプを案内します。"
    override fun commandUsageLang(langList: String) =
        "`~lang` $langList このサーバーで使われる言語設定を変更します。Ex) `~lang` `ENG`"
    override fun commandUsageStartPVE() = "`~start` A.Iとゲームを開始します。"
    override fun commandUsageStartPVP() = "`~start` `@言及` 言及されたプレーヤーとゲームを開始します。 Ex) `~start` `@player`"
    override fun commandUsageResign() = "`~resign` 現在進行中のゲームを諦めます。"

    override fun rankEmbedTitle() = "GomokuBot / ランキング"
    override fun rankEmbedDescription() = "1位から10位までのランキングです。"
    override fun rankEmbedWin() = "勝利"
    override fun rankEmbedLose() = "敗北"

    override fun languageUpdated() = "言語設定を日本語:flag_jp:に変更しました\n`Translated by`: `S1RO`"

    override fun startErrorSessionAlready() =
        "ゲーム生成に失敗しました。今のゲームを終了してください。 :thinking:"
    override fun setErrorIllegalArgument() =
        "さん, 間違いコマンドです。`~s アルファベット 数字` 形式に書いてください。 :thinking:"
    override fun setErrorExist(move: String) = "さん, そこにはすでに碁石があります。 :thinking:"

    override fun beginPVP(blackPlayer: String, whitePlayer: String) =
        "`$blackPlayer`と `$whitePlayer`のマッチが開始しました。先攻は `$blackPlayer`です。"

    override fun processNextPVP(priorPlayer: String, lastMove: String) =
        "次の手を置いてください。 `$priorPlayer`は $lastMove に置きました。"

    override fun processErrorOrder(player: String) =
        "今は`$player`'さんの番です。`$player`さんの次の番を待ってください。 :thinking:"
    override fun endPVPWin(winner: String, loser: String, lastMove: String) =
        "`$winner`さんが $lastMove に置いて`$loser`さんに勝ちました。"
    override fun endPVPResign(winner: String, loser: String) =
        "`$loser`さんが降伏を宣言して`$winner`さんが勝ちました。"

    override fun endPVEWin(player: String, lastPos: String) = lastPos + "に置いてAIに勝ちました。おめでとうございます。"
    override fun endPVELose(player: String, lastPos: String) = "AIが" + lastPos + "に置いて負けました。"
    override fun endPVEResign(player: String) = "降伏を宣言してAIに負けました。"

    override fun endPVPTie(owner: String, opponent: String) = "もう碁石を置く所が無くて無勝負に処理されました。"

    override fun boardInProgress() = "進行中"
    override fun boardFinished() = "終了"
    override fun boardMoves() = "ターン進行度"
    override fun boardLastMove() = "最近着手位置"

}
