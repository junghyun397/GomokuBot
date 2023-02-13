package core.interact.i18n

class LanguageSKO : LanguageKOR() {

    override fun languageCode() = "SKO"

    override fun languageName() = "\uD83C\uDDF0\uD83C\uDDF7 國漢文混用體"
    override fun languageSuggestion() = "`/lang` `SKO` 命令語를使用해주세요."

    override fun helpAboutEmbedTitle() = "GomokuBot / 助言"
    override fun helpAboutEmbedDescription(platform: String) =
        "GomokuBot은${platform}에서五目을滿喫할수있게해주는資料開放DiscordBot입니다. 收集된棋譜資料는强化學習模型訓鍊에使用됩니다. :)"
    override fun helpAboutEmbedDeveloper() = "開發者"
    override fun helpAboutEmbedRepository() = "Git 貯藏所"
    override fun helpAboutEmbedVersion() = "版"
    override fun helpAboutEmbedSupport() = "支援手段"

    override fun commandUsageEmbedTitle() = "GomokuBot / 命令語"
    override fun commandUsageHelp() = "`~help` 助言을告知해드립니다."
    override fun commandUsageLang(langList: String) =
        "`~lang` $langList 此奉仕機에서使用되는言語設定을改變합니다. Ex) `~lang` `ENG`"
    override fun commandUsageStartPVE() = "`~start` 人工知能와의遊戱을開始합니다."
    override fun commandUsageStartPVP() = "`~start` `@言及` 言及된遊戱者와의遊戱을開始합니다. Ex) `~start` `@player`"
    override fun commandUsageResign() = "`~resign` 現在進行하고있는遊戱을抛棄합니다."

    override fun rankEmbedTitle() = "GomokuBot / 順位"
    override fun rankEmbedDescription() = "一位부터十位까지의順位입니다. :D"
    override fun rankEmbedWin() = "勝利"
    override fun rankEmbedLose() = "敗北"

    override fun languageUpdated() =
        "言語設定이韓國語:flag_kr:로改變했습니다!\n`Translated by`: `1,2,3,4-TetraMethylBenzene(Kawaii-cirno)`"

    override fun startErrorSessionAlready() =
        "遊戱生成에失敗했습니다. 滿喫하고계신遊戱를完了해주세요. :thinking:"

    override fun beginPVP(blackPlayer: String, whitePlayer: String) =
        "$blackPlayer 님과의對決이開始되었습니다! 先攻은 $blackPlayer 님입니다."

    override fun processNextPVP(priorPlayer: String, lastMove: String) =
        "다음手를置하여주세요. " + priorPlayer + "는 " + lastMove + "에置하였습니다."

    override fun processErrorOrder(player: String) =
        "只今은 $player 님의次例입니다. $player 님의다음手를待機해주세요. :thinking:"
    override fun endPVPWin(winner: String, loser: String, lastMove: String) =
        winner + " 님이" + lastMove + "에置함으로서 " + loser + " 님에게勝利하였습니다!"
    override fun endPVPResign(winner: String, loser: String) =
        "$loser 님이降伏을宣言함으로서 $winner 님이勝利하였습니다!"

    override fun endPVEWin(player: String, lastPos: String) = lastPos +  "에置함으로서人工知能에게勝利하셨습니다. 祝賀합니다!"
    override fun endPVELose(player: String, lastPos: String) = "人工知能이" + lastPos + "에置함으로서敗北하셨습니다."
    override fun endPVEResign(player: String) = "降伏을宣言함으로서人工知能에게敗北하였습니다."

    override fun endPVPTie(owner: String, opponent: String) = "더以上棋를置할位가無하여無勝負處理되었습니다."

    override fun boardInProgress() = "進行中"
    override fun boardFinished() = "終了됨"
    override fun boardMoves() = "次例進行度"
    override fun boardLastMove() = "最近着手位置"

}
