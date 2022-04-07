package core.interact.i18n

open class LanguageKOR : LanguageENG() {

    override fun languageCode() = "KOR"

    override fun languageName() = "한국어\uD83C\uDDF0\uD83C\uDDF7"
    override fun languageSuggestion() = "`~lang` `KOR` 명령어를 사용해주세요."

    override fun helpCommand() = "도움말"

    override fun helpAboutEmbedTitle() = "GomokuBot / 도움말"
    override fun helpAboutEmbedDescription() =
        "GomokuBot 은 디스코드에서 오목 기능을 제공하는 오픈소스 디스코드 봇 입니다. 수집된 기보 데이터는 강화학습 모델 훈련에 사용됩니다."
    override fun helpAboutEmbedDeveloper() = "개발자"
    override fun helpAboutEmbedRepository() = "Git 저장소"
    override fun helpAboutEmbedVersion() = "판올림"
    override fun helpAboutEmbedSupport() = "지원 채널"
    override fun helpAboutEmbedInvite() = "초대 링크"

    override fun helpCommandEmbedTitle() = "GomokuBot / 명령어"

    override fun helpCommandEmbedLang(langList: String) =
        "`~lang` $langList 이 서버에서 사용되는 언어 설정을 바꿉니다. Ex) `~lang` `ENG`"
    override fun helpCommandEmbedStyle() = "`~skin` `A` `B` `C` 이 서버에서 사용되는 오목판의 스타일 설정을 바꿉니다. Ex) `~skin` `A`"
    override fun helpCommandEmbedStartPVE() = "`~start` 인공지능과의 게임을 시작합니다."
    override fun helpCommandEmbedStartPVP() = "`~start` `@언급` 언급된 플레이어와의 게임을 시작합니다. Ex) `~start` `@player`"
    override fun helpCommandEmbedResign() = "`~resign` 현재 진행되고 있는 게임을 포기합니다."

    override fun styleEmbedTitle() = "GomokuBot / 스타일"
    override fun styleEmbedDescription() =
        "GomokuBot에서 제공하는 기본 오목판(``Style A``)이 제대로 표시되지 않을 수 있습니다." +
                "준비된 세 가지 스타일 중 하나를 선택해 이 서버에서 사용할 스타일을 설정해 주세요."
    override fun styleEmbedSuggestion(style: String) = "이 스타일을 사용 하려면 ``~skin`` ``$style`` 명령어를 입력해주세요."
    override fun styleErrorNotfound() = "스타일 지정에 오류가 있습니다."
    override fun styleUpdated(style: String) = "스타일 설정이 스타일 ``$style``로 바뀌었습니다!"

    override fun rankEmbedTitle() = "GomokuBot / 순위"
    override fun helpCommandEmbedRank() = "`~rank` 1위부터 10위까지의 순위를 출력합니다."
    override fun rankEmbedDescription() = "1위부터 10위까지의 순위입니다."
    override fun rankEmbedWin() = "승리"
    override fun rankEmbedLose() = "패배"

    override fun languageUpdated() = "언어 설정이 한국어:flag_kr:로 바뀌었습니다!"

    override fun startErrorSessionAlready(nameTag: String) =
        "$nameTag 님, 게임 생성에 실패했습니다. 진행중에 있는 게임을 마무리해 주세요. :thinking:"

    override fun beginPVP(ownerName: String, opponentName: String, fMove: String) =
        "`$ownerName`님과 `$opponentName`님과의 대결이 시작되었습니다! 선공은 `$fMove`님입니다."

    override fun processNext(curName: String, prvName: String, lastPos: String) =
        "`$curName`님, 다음 수를 놓아주세요. `$prvName`는 $lastPos 에 놓았습니다."

    override fun processErrorOrder(turnName: String) =
        "지금은 `$turnName`님의 차례입니다. `$turnName`님의 다음 수를 기다려 주세요. :thinking:"
    override fun endPVPWin(winName: String, loseName: String, lastPos: String) =
        "`$winName`님이 $lastPos 에 놓음으로써 `$loseName`님에게 승리하였습니다!"
    override fun endPVPResign(winName: String, loseName: String) =
        "`$loseName`님이 항복을 선언 함으로써 `$winName`님이 승리하였습니다!"

    override fun endPVEWin(latestPos: String) = "$latestPos 에 놓음으로써 A.I.에게 승리하셨습니다. 축하합니다!"
    override fun endPVELose(latestPos: String) = "A.I. 가 $latestPos 에 놓음으로써 패배하셨습니다."
    override fun endPVEResign() = "항복을 선언 함으로써 A.I.에게 패배하였습니다."

    override fun endPVPTie() = "더는 돌을 놓을 자리가 없어 무승부 처리되었습니다."

    override fun boardInProgress() = "진행중"
    override fun boardFinished() = "종료됨"
    override fun boardMoves() = "턴 진행도"
    override fun boardLatestMove() = "최근 착수 위치"

}
