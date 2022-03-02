package i18n

open class LanguageKOR : LanguageENG() {

    override fun languageCode(): String = "KOR"

    override fun languageName(): String = "한국어:flag_kr:"
    override fun languageDescription(): String = "`~lang` `KOR` 명령어를 사용해주세요."

    override fun helpInfo(): String = "GomokuBot / 도움말"
    override fun helpDescription(): String =
        "GomokuBot 은 디스코드에서 오목 기능을 제공하는 오픈소스 디스코드 봇 입니다. 수집된 기보 데이터는 강화학습 모델 훈련에 사용됩니다."
    override fun helpDev(): String = "개발자"
    override fun helpRepo(): String = "Git 저장소"
    override fun helpVersion(): String = "판올림"
    override fun helpSupport(): String = "지원 채널"
    override fun helpInvite(): String = "초대 링크"

    override fun helpCmdInfo(): String = "GomokuBot / 명령어"

    override fun helpCmdLang(langList: String): String =
        "`~lang` $langList 이 서버에서 사용되는 언어 설정을 바꿉니다. Ex) `~lang` `ENG`"
    override fun helpCmdSkin(): String = "`~skin` `A` `B` `C` 이 서버에서 사용되는 오목판의 스타일 설정을 바꿉니다. Ex) `~skin` `A`"
    override fun helpCmdPVE(): String = "`~start` 인공지능과의 게임을 시작합니다."
    override fun helpCmdPVP(): String = "`~start` `@언급` 언급된 플레이어와의 게임을 시작합니다. Ex) `~start` `@player`"
    override fun helpCmdResign(): String = "`~resign` 현재 진행되고 있는 게임을 포기합니다."

    override fun skinInfo(): String = "GomokuBot / 스타일"
    override fun skinDescription(): String =
        "GomokuBot에서 제공하는 기본 오목판(``Style A``)이 제대로 표시되지 않을 수 있습니다." +
                "준비된 세 가지 스타일 중 하나를 선택해 이 서버에서 사용할 스타일을 설정해 주세요."
    override fun skinCmdInfo(style: String): String = "이 스타일을 사용 하려면 ``~skin`` ``$style`` 명령어를 입력해주세요."
    override fun skinUpdateError(): String = "스타일 지정에 오류가 있습니다."
    override fun skinUpdateSuccess(style: String): String = "스타일 설정이 스타일 ``$style``로 바뀌었습니다!"

    override fun rankInfo(): String = "GomokuBot / 순위"
    override fun helpCmdRank(): String = "`~rank` 1위부터 10위까지의 순위를 출력합니다."
    override fun rankDescription(): String = "1위부터 10위까지의 순위입니다."
    override fun rankWin(): String = "승리"
    override fun rankLose(): String = "패배"

    override fun langUpdateError(): String = "언어 지정에 오류가 있습니다."
    override fun langUpdateSuccess(): String = "언어 설정이 한국어:flag_kr:로 바뀌었습니다!"

    override fun gameNotFound(nameTag: String): String =
        "$nameTag 님, 진행 중인 게임을 찾을 수 없습니다. `~start` 명령어로 게임을 시작해 주세요!"
    override fun gameCreationFail(nameTag: String): String =
        "$nameTag 님, 게임 생성에 실패했습니다. 진행중에 있는 게임을 마무리해 주세요. :thinking:"
    override fun gameSyntaxFail(nameTag: String): String =
        "$nameTag 님, 그것은 잘못된 명령어입니다. `~s 알파벳 숫자` 형식으로 적어주세요. :thinking:"
    override fun gameAlreadyInProgress(nameTag: String): String = "$nameTag 님, 그곳에는 이미 돌이 놓여 있습니다. :thinking:"

    override fun gameCreateInfo(playerName: String, targetName: String, fAttack: String): String =
        "`$playerName`님과 `$targetName`님과의 대결이 시작되었습니다! 선공은 `$fAttack`님입니다."
    override fun gameCmdInfo(): String = " `~s` `알파벳` `숫자` 형식으로 돌을 놓아주세요. Ex) `~s` `h` `8`"

    override fun gameNextTurn(curName: String, prvName: String, lastPos: String): String =
        "`$curName`님, 다음 수를 놓아주세요. `$prvName`는 $lastPos 에 놓았습니다."

    override fun gamePVPNextTurn(turnName: String): String =
        "지금은 `$turnName`님의 차례입니다. `$turnName`님의 다음 수를 기다려 주세요. :thinking:"
    override fun gamePVPWin(winName: String, loseName: String, lastPos: String): String =
        "`$winName`님이 $lastPos 에 놓음으로써 `$loseName`님에게 승리하였습니다!"
    override fun gamePVPResign(winName: String, loseName: String): String =
        "`$loseName`님이 항복을 선언 함으로써 `$winName`님이 승리하였습니다!"

    override fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int): String =
        "$winName 님과 $loseName 님의 전적이 `$winCount:$loseCount`로 업데이트되었습니다."

    override fun gamePVEWin(lastPos: String): String = "$lastPos 에 놓음으로써 A.I.에게 승리하셨습니다. 축하합니다!"
    override fun gamePVELose(lastPos: String): String = "A.I. 가 $lastPos 에 놓음으로써 패배하셨습니다."
    override fun gamePVEResign(): String = "항복을 선언 함으로써 A.I.에게 패배하였습니다."

    override fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int): String =
        "A.I. 상대 전적이 `$winCount:$loseCount`로 업데이트되었습니다. 현재 $playerName 님의 순위는 $rank 위 입니다."

    override fun gameFull(): String = "더는 돌을 놓을 자리가 없어 무승부 처리되었습니다."

    override fun boardInProgress(): String = "진행중"
    override fun boardFinish(): String = "종료됨"
    override fun boardProgress(): String = "턴 진행도"
    override fun boardTurns(): String = "턴"
    override fun boardLocation(): String = "최근 착수 위치"

}
