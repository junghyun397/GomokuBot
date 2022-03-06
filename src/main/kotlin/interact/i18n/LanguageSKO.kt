package interact.i18n

class LanguageSKO : LanguageKOR() {

    override fun languageCode(): String = "SKO"

    override fun languageName(): String = "國漢文混用體:flag_kr:"
    override fun languageDescription(): String = "`~lang` `SKO` 命令語를使用해주세요."

    override fun helpInfo(): String = "GomokuBot / 助言"
    override fun helpDescription(): String =
        "GomokuBot은Discord에서五目을滿喫할수있게해주는資料開放DiscordBot입니다. 收集된棋譜資料는强化學習模型訓鍊에使用됩니다. :)"
    override fun helpDeveloper(): String = "開發者"
    override fun helpRepository(): String = "Git 貯藏所"
    override fun helpVersion(): String = "版"
    override fun helpSupport(): String = "支援手段"

    override fun helpCommandInfo(): String = "GomokuBot / 命令語"
    override fun helpCommandHelp(): String = "`~help` 助言을告知해드립니다."
    override fun helpCommandLang(langList: String): String =
        "`~lang` $langList 此奉仕機에서使用되는言語設定을改變합니다. Ex) `~lang` `ENG`"
    override fun helpCommandPVE(): String = "`~start` 人工知能와의遊戱을開始합니다."
    override fun helpCommandPVP(): String = "`~start` `@言及` 言及된遊戱者와의遊戱을開始합니다. Ex) `~start` `@player`"
    override fun helpCommandResign(): String = "`~resign` 現在進行하고있는遊戱을抛棄합니다."

    override fun rankInfo(): String = "GomokuBot / 順位"
    override fun rankDescription(): String = "一位부터十位까지의順位입니다. :D"
    override fun rankWin(): String = "勝利"
    override fun rankLose(): String = "敗北"

    override fun langUpdateError(): String = "言語指定에誤謬가有합니다!"
    override fun langUpdateSuccess(): String =
        "言語設定이韓國語:flag_kr:로改變했습니다!\n`Translated by`: `1,2,3,4-TetraMethylBenzene(Kawaii-cirno)`"

    override fun gameNotFound(nameTag: String): String =
        "$nameTag 님, 進行중인遊戱을찾을수없습니다. `~start`命令語로遊戱을開始해주세요!"
    override fun gameAlreadyInProgress(nameTag: String): String =
        "$nameTag 님, 遊戱生成에失敗했습니다. 滿喫하고계신遊戱를完了해주세요. :thinking:"
    override fun gameSyntaxFail(nameTag: String): String =
        "$nameTag 님, 彼는不正한命令語입니다. `~s 洋文 數字`形式으로記入하여주세요. :thinking:"
    override fun gameAlreadyExits(nameTag: String): String = "$nameTag 님, 彼地에는이미棋가置하여있습니다. :thinking:"

    override fun gameCreatedInfo(playerName: String, targetName: String, fAttack: String): String =
        "`$playerName`님과의對決이開始되었습니다! 先攻은`$fAttack`님입니다."
    override fun gameCommandInfo(): String = " `~s` `洋文` `數字`型式으로棋를置하여주세요. Ex) `~s` `h` `8`"

    override fun gameNextTurn(curName: String, prvName: String, lastPos: String): String =
        "`$curName`님, 다음手를置하여주세요. `" + prvName + "`는 " + lastPos + "에置하였습니다."

    override fun gamePVPPleaseWait(turnName: String): String =
        "只今은`$turnName`님의次例입니다. `$turnName`님의다음手를待機해주세요. :thinking:"
    override fun gamePVPWin(winName: String, loseName: String, lastPos: String): String =
        "`" + winName + "`님이" + lastPos + "에置함으로서`" + loseName + "`님에게勝利하였습니다!"
    override fun gamePVPWinCausedByResign(winName: String, loseName: String): String =
        "`$loseName`님이降伏을宣言함으로서`$winName`님이勝利하였습니다!"

    override fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int): String =
        "$winName 님과$loseName 님의戰績이`$winCount : $loseCount`로更新되었습니다."

    override fun gamePVEWin(lastPos: String): String = lastPos +  "에置함으로서人工知能에게勝利하셨습니다. 祝賀합니다!"
    override fun gamePVELose(lastPos: String): String = "人工知能이" + lastPos + "에置함으로서敗北하셨습니다."
    override fun gamePVEResign(): String = "降伏을宣言함으로서人工知能에게敗北하였습니다."

    override fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int): String =
        "人工知能相對戰績이`$winCount : $loseCount`로更新되었습니다. 現在$playerName 님의順位는$rank 位입니다."

    override fun gameTieCausedByFull(): String = "더以上棋를置할位가無하여無勝負處理되었습니다."

    override fun boardInProgress(): String = "進行中"
    override fun boardFinish(): String = "終了됨"
    override fun boardProgress(): String = "次例進行度"
    override fun boardTurns(): String = "次例"
    override fun boardLocation(): String = "最近着手位置"

}