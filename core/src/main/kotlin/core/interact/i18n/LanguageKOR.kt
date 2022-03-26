package core.interact.i18n

open class LanguageKOR : LanguageENG() {

    override fun languageCode() = "KOR"

    override fun languageName() = "한국어\uD83C\uDDF0\uD83C\uDDF7"
    override fun languageSuggestion() = "`~lang` `KOR` 명령어를 사용해주세요."

    // TOKENS

    override fun user() = "유저"

    // HELP

    override fun helpAboutTitle() = "GomokuBot / 도움말"
    override fun helpAboutDescription() =
        "GomokuBot 은 디스코드에서 오목 기능을 제공하는 오픈소스 디스코드 봇 입니다. 수집된 기보 데이터는 강화학습 모델 훈련에 사용됩니다."
    override fun helpAboutDeveloper() = "개발자"
    override fun helpAboutRepository() = "Git 저장소"
    override fun helpAboutVersion() = "판올림"
    override fun helpAboutSupport() = "지원 채널"
    override fun helpAboutInvite() = "초대 링크"

    override fun helpCommandInfo() = "GomokuBot / 명령어"

    override fun helpCommandLang(langList: String) =
        "`~lang` $langList 이 서버에서 사용되는 언어 설정을 바꿉니다. Ex) `~lang` `ENG`"
    override fun helpCommandStyle() = "`~skin` `A` `B` `C` 이 서버에서 사용되는 오목판의 스타일 설정을 바꿉니다. Ex) `~skin` `A`"
    override fun helpCommandPVE() = "`~start` 인공지능과의 게임을 시작합니다."
    override fun helpCommandPVP() = "`~start` `@언급` 언급된 플레이어와의 게임을 시작합니다. Ex) `~start` `@player`"
    override fun helpCommandResign() = "`~resign` 현재 진행되고 있는 게임을 포기합니다."

    override fun styleInfo() = "GomokuBot / 스타일"
    override fun styleDescription() =
        "GomokuBot에서 제공하는 기본 오목판(``Style A``)이 제대로 표시되지 않을 수 있습니다." +
                "준비된 세 가지 스타일 중 하나를 선택해 이 서버에서 사용할 스타일을 설정해 주세요."
    override fun styleSuggestion(style: String) = "이 스타일을 사용 하려면 ``~skin`` ``$style`` 명령어를 입력해주세요."
    override fun styleUpdateError() = "스타일 지정에 오류가 있습니다."
    override fun styleUpdateSuccess(style: String) = "스타일 설정이 스타일 ``$style``로 바뀌었습니다!"

    override fun rankInfo() = "GomokuBot / 순위"
    override fun helpCommandRank() = "`~rank` 1위부터 10위까지의 순위를 출력합니다."
    override fun rankDescription() = "1위부터 10위까지의 순위입니다."
    override fun rankWin() = "승리"
    override fun rankLose() = "패배"

    override fun langUpdateError() = "언어 지정에 오류가 있습니다."
    override fun langUpdateSuccess() = "언어 설정이 한국어:flag_kr:로 바뀌었습니다!"

    override fun gameNotFound(nameTag: String) =
        "$nameTag 님, 진행 중인 게임을 찾을 수 없습니다. `~start` 명령어로 게임을 시작해 주세요!"
    override fun gameAlreadyInProgress(nameTag: String) =
        "$nameTag 님, 게임 생성에 실패했습니다. 진행중에 있는 게임을 마무리해 주세요. :thinking:"
    override fun gameSyntaxFail(nameTag: String) =
        "$nameTag 님, 그것은 잘못된 명령어입니다. `~s 알파벳 숫자` 형식으로 적어주세요. :thinking:"
    override fun gameInvalidMoveAlreadyExits(nameTag: String) = "$nameTag 님, 그곳에는 이미 돌이 놓여 있습니다. :thinking:"

    override fun gameCreatedInfo(playerName: String, targetName: String, fAttack: String) =
        "`$playerName`님과 `$targetName`님과의 대결이 시작되었습니다! 선공은 `$fAttack`님입니다."
    override fun gameCommandInfo() = " `~s` `알파벳` `숫자` 형식으로 돌을 놓아주세요. Ex) `~s` `h` `8`"

    override fun gameNextTurn(curName: String, prvName: String, lastPos: String) =
        "`$curName`님, 다음 수를 놓아주세요. `$prvName`는 $lastPos 에 놓았습니다."

    override fun gamePVPPleaseWait(turnName: String) =
        "지금은 `$turnName`님의 차례입니다. `$turnName`님의 다음 수를 기다려 주세요. :thinking:"
    override fun gamePVPWin(winName: String, loseName: String, lastPos: String) =
        "`$winName`님이 $lastPos 에 놓음으로써 `$loseName`님에게 승리하였습니다!"
    override fun gamePVPWinCausedByResign(winName: String, loseName: String) =
        "`$loseName`님이 항복을 선언 함으로써 `$winName`님이 승리하였습니다!"

    override fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int) =
        "$winName 님과 $loseName 님의 전적이 `$winCount:$loseCount`로 업데이트되었습니다."

    override fun gamePVEWin(lastPos: String) = "$lastPos 에 놓음으로써 A.I.에게 승리하셨습니다. 축하합니다!"
    override fun gamePVELose(lastPos: String) = "A.I. 가 $lastPos 에 놓음으로써 패배하셨습니다."
    override fun gamePVEResign() = "항복을 선언 함으로써 A.I.에게 패배하였습니다."

    override fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int) =
        "A.I. 상대 전적이 `$winCount:$loseCount`로 업데이트되었습니다. 현재 $playerName 님의 순위는 $rank 위 입니다."

    override fun gameTieCausedByFull() = "더는 돌을 놓을 자리가 없어 무승부 처리되었습니다."

    override fun inProgress() = "진행중"
    override fun finish() = "종료됨"
    override fun progress() = "턴 진행도"
    override fun turns() = "턴"
    override fun move() = "최근 착수 위치"

}
