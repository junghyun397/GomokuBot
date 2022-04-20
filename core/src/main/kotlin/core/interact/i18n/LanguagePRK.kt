package core.interact.i18n

class LanguagePRK : LanguageKOR() {

    override fun languageCode() = "PRK"

    override fun languageName() = "조선말\uD83C\uDDF0\uD83C\uDDF5"
    override fun languageSuggestion() = "`~lang` `PRK` 시킴말을 사용 하라우."

    override fun helpAboutEmbedTitle() = "GomokuBot / 도움말"
    override fun helpAboutEmbedDescription() =
        "GomokuBot은 불협화음에서 오목 오락을 즐길 수 있게 하는 열린문서 Discord Bot 이라우. 모아진 기보 내용은 강화학습 형태 훈련에 사용된다우."

    override fun helpAboutEmbedDeveloper() = "주체자"
    override fun helpAboutEmbedRepository() = "직결 저장소"
    override fun helpAboutEmbedVersion() = "판올림"
    override fun helpAboutEmbedSupport() = "련락 공간"

    override fun helpCommandEmbedTitle() = "GomokuBot / 시킴말"
    override fun helpCommandEmbedHelp() = "`~help` 시킴말을 알려 드리겠소."
    override fun helpCommandEmbedLang(langList: String) =
        "`~lang` $langList 이 봉사기에서 사용되는 말씨 설정을 바꾼다우. Ex) `~lang` `PRK`"

    override fun helpCommandEmbedStartPVE() = "`~start` 콤퓨타와의 놀음을 시작합네다."
    override fun helpCommandEmbedStartPVP() = "`~start` `@언급` 언급된 놀음꾼과의 놀음을 시작 합네다. Ex) `~start` `@player`"
    override fun helpCommandEmbedResign() = "`~resign` 현재 진행하고 있는 놀음을 포기합네다."

    override fun rankEmbedTitle() = "GomokuBot / 순위"
    override fun rankEmbedDescription() = "1위부터 10위까지의 순위 입네다."
    override fun rankEmbedWin() = "평양"
    override fun rankEmbedLose() = "아오지"

    override fun languageUpdated() = "말씨 설정이 조선말:flag_kp:로 바뀌었습네다!"

    override fun startErrorSessionAlready(user: String) =
        "$user 동무, 놀음 만들기에 실패 했습네다. 즐기고 있던 놀음을 마무리 해주시우. :thinking:"

    override fun beginPVP(owner: String, opponent: String, opener: String) =
        "`$owner`동무와 `$opponent`동무과의 쌈박질이 시작 되었습네다! 선공은 `$opener`동무 입네다."

    override fun processNext(player: String, priorPlayer: String, latestMove: String) =
        "`" + player + "`동무, 다음 돌을 놓아주시우. `" + priorPlayer + "`동무는 " + latestMove + "에 놓았습네다."

    override fun processErrorOrder(user: String, player: String) =
        "지금은 `$player`동무의 차례입니다. `$player`동무의 다음 수를 기다려 주시우. :thinking:"

    override fun endPVPWin(winner: String, looser: String, latestMove: String) =
        "`" + winner + "`동무가 " + latestMove + "에 놓음으로서 `" + looser + "`동무에게 이겼습네다!"

    override fun endPVPResign(winner: String, looser: String) =
        "`$looser`동무가 항복을 선언 함으로서 `$winner`동무가 이겼습네다!"

    override fun endPVEWin(player: String, latestPos: String) = latestPos + "에 놓음으로서 콤퓨타에게 승리 하였습네다. 축하합네다!"
    override fun endPVELose(player: String, latestPos: String) = "콤퓨타가 " + latestPos + "에 놓음으로서 콤퓨타에게 패배 하였습네다."
    override fun endPVEResign(player: String) = "항복을 외침으로써 콤퓨타에게 패배 하였습네다."

    override fun endPVPTie(owner: String, opponent: String) = "더이상 돌을 넣을 자리가 없어 무승부 처리 되었습네다."

    override fun boardInProgress() = "쌈박질중"
    override fun boardFinished() = "초토화됨"
    override fun boardMoves() = "순서 진행도"
    override fun boardLatestMove() = "마지막 위치"

}
