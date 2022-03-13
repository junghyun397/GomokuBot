package interact.i18n

import java.util.*

class LanguagePRK : LanguageKOR() {

    override fun targetRegion() = emptyArray<Locale>()

    override fun languageCode() = "PRK"

    override fun languageName() = "조선말:flag_kp:"
    override fun languageSuggestion() = "`~lang` `PRK` 시킴말을 사용 하라우."

    override fun helpAboutInfo() = "GomokuBot / 도움말"
    override fun helpAboutDescription() =
        "GomokuBot은 불협화음에서 오목 오락을 즐길 수 있게 하는 열린문서 Discord Bot 이라우. 모아진 기보 내용은 강화학습 형태 훈련에 사용된다우."

    override fun helpAboutDeveloper() = "주체자"
    override fun helpAboutRepository() = "직결 저장소"
    override fun helpAboutVersion() = "판올림"
    override fun helpAboutSupport() = "련락 공간"

    override fun helpCommandInfo() = "GomokuBot / 시킴말"
    override fun helpCommandHelp() = "`~help` 시킴말을 알려 드리겠소."
    override fun helpCommandLang(langList: String) =
        "`~lang` $langList 이 봉사기에서 사용되는 말씨 설정을 바꾼다우. Ex) `~lang` `PRK`"

    override fun helpCommandPVE() = "`~start` 콤퓨타와의 놀음을 시작합네다."
    override fun helpCommandPVP() = "`~start` `@언급` 언급된 놀음꾼과의 놀음을 시작 합네다. Ex) `~start` `@player`"
    override fun helpCommandResign() = "`~resign` 현재 진행하고 있는 놀음을 포기합네다."

    override fun rankInfo() = "GomokuBot / 순위"
    override fun rankDescription() = "1위부터 10위까지의 순위 입네다."
    override fun rankWin() = "평양"
    override fun rankLose() = "아오지"

    override fun langUpdateError() = "말씨 지정에 문제가 있습네다."
    override fun langUpdateSuccess() = "말씨 설정이 조선말:flag_kp:로 바뀌었습네다!"

    override fun gameNotFound(nameTag: String) =
        "$nameTag 동무, 진행중인 놀음을 찾을 수 없습니다. `~start`시킴말로 놀음을 시작 해주시오!"
    override fun gameAlreadyInProgress(nameTag: String) =
        "$nameTag 동무, 놀음 만들기에 실패 했습네다. 즐기고 있던 놀음을 마무리 해주시우. :thinking:"

    override fun gameSyntaxFail(nameTag: String) =
        "$nameTag 동무, 그건 잘못된 시킴말 입네다. `~s 미국말 숫자` 형식으로 적어주시우. :thinking:"

    override fun gameInvalidMoveAlreadyExits(nameTag: String) = nameTag + "동무, 그곳에는 이미 돌이 놓여 있습네다. :thinking:"

    override fun gameCreatedInfo(playerName: String, targetName: String, fAttack: String) =
        "`$playerName`동무와 `$targetName`동무과의 쌈박질이 시작 되었습네다! 선공은 `$fAttack`동무 입네다."

    override fun gameCommandInfo() = " `~s` `미국말` `숫자` 형식으로 돌을 놓아주시우. Ex) `~s` `h` `8`"

    override fun gameNextTurn(curName: String, prvName: String, lastPos: String) =
        "`" + curName + "`동무, 다음 돌을 놓아주시우. `" + prvName + "`동무는 " + lastPos + "에 놓았습네다."

    override fun gamePVPPleaseWait(turnName: String) =
        "지금은 `$turnName`동무의 차례입니다. `$turnName`동무의 다음 수를 기다려 주시우. :thinking:"

    override fun gamePVPWin(winName: String, loseName: String, lastPos: String) =
        "`" + winName + "`동무가 " + lastPos + "에 놓음으로서 `" + loseName + "`동무에게 이겼습네다!"

    override fun gamePVPWinCausedByResign(winName: String, loseName: String) =
        "`$loseName`동무가 항복을 선언 함으로서 `$winName`동무가 이겼습네다!"

    override fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int) =
        winName + "동무와 " + loseName + "동무의 쌈박질이 `" + winCount + " : " + loseCount + "`로 바뀜질 되었습네다."

    override fun gamePVEWin(lastPos: String) = lastPos + "에 놓음으로서 콤퓨타에게 승리 하였습네다. 축하합네다!"
    override fun gamePVELose(lastPos: String) = "콤퓨타가 " + lastPos + "에 놓음으로서 콤퓨타에게 패배 하였습네다."
    override fun gamePVEResign() = "항복을 외침으로써 콤퓨타에게 패배 하였습네다."

    override fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int) =
        "콤퓨타 상대 전적이 `$winCount : $loseCount`로 바뀜질 되었습네다. 현재 $playerName 동무의 순위는 $rank 위 입네다."

    override fun gameTieCausedByFull() = "더이상 돌을 넣을 자리가 없어 무승부 처리 되었습네다."

    override fun inProgress() = "쌈박질중"
    override fun finish() = "초토화됨"
    override fun progress() = "순서 진행도"
    override fun turns() = "순서"
    override fun move() = "마지막 위치"

}
