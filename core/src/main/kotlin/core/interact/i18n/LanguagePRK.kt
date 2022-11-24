package core.interact.i18n

class LanguagePRK : LanguageKOR() {

    override fun languageCode() = "PRK"

    override fun languageName() = "\uD83C\uDDF0\uD83C\uDDF5 조선말"
    override fun languageSuggestion() = "``/lang`` ``PRK`` 시킴말을 써 주시오."

    // # 0. TOKENS

    override fun aiLevelAmoeba() = "아메바"
    override fun aiLevelApe() = "유인원"
    override fun aiLevelBeginner() = "초심자"
    override fun aiLevelModerate() = "보통"
    override fun aiLevelExpert() = "숙련자"
    override fun aiLevelGuru() = "전문가"

    // # 1. INFORM

    // ## 1-1. HELP

    // ### 1-1-1. HELP (COMMAND)

    override fun helpCommand() = "도움말"
    override fun helpCommandDescription() = "도움말을 알아보오."

    // ### 1-1-2. HELP:ABOUT (EMBED)

    override fun helpAboutEmbedTitle() = "GomokuBot / 도움말"
    override fun helpAboutEmbedDescription(platform: String) =
        "이제 **$platform**에서도 **오목**을 즐겨 보시오. **GomokuBot** 동무가 함께하오." +
                " ― GomokuBot은 ${platform}에서 오목([렌주](https://www.renju.net/rules/)) 기능을 제공하는 오픈소스 콤퓨타 오목 봇입니다. " +
                "수집된 기보 자료는 강화학습 모델 훈련에 사용됩니다."
    override fun helpAboutEmbedDeveloper() = "개발자"
    override fun helpAboutEmbedRepository() = "직결 저장소"
    override fun helpAboutEmbedVersion() = "판올림"
    override fun helpAboutEmbedSupport() = "련락 공간"
    override fun helpAboutEmbedInvite() = "초대 초련결"

    // ### 1-1-3. HELP:COMMAND (EMBED)

    override fun helpCommandEmbedTitle() = "GomokuBot / 시킴말"
    override fun helpCommandEmbedHelp() = "도움말을 알아보오."
    override fun helpCommandEmbedRank() = "1위부터 10위까지의 순위를 알아보오."
    override fun helpCommandEmbedRating() = "``GomokuBot ELO`` 레이팅을 알아보오."

    override fun helpCommandEmbedLang(langList: String) =
        "이 봉사기에서 쓰이는 말씨 설정을 바꾸오. Ex) ``/lang`` ``ENG``"
    override fun helpCommandEmbedStyle() =
        "이 봉사기에서 쓰이는 오목판 꼴을 바꾸오. Ex) ``/스타일`` ``A``"

    override fun helpCommandEmbedStartPVE() = "콤퓨타와 함께 새 놀음을 시작하오."
    override fun helpCommandEmbedStartPVP() =
        "멘션 된 인민에게 새 놀음을 제안하오. Ex) ``/시작`` ``@유저``"
    override fun helpCommandEmbedResign() = "진행 중인 놀음을 포기하오."

    // ## 1-2. RANK

    // ### 1-2-1. RANK (COMMAND)

    override fun rankCommand() = "순위"
    override fun rankCommandDescription() = "1위부터 10위까지의 순위를 알아봅니다."

    // ### 1-2-2. RANK:LIST (EMBED)

    override fun rankEmbedTitle() = "GomokuBot / 순위"
    override fun rankEmbedDescription() = "1위부터 10위까지의 승리 순위"
    override fun rankEmbedWin() = "승"
    override fun rankEmbedLose() = "패"

    // ## 1-3. RATING

    // ### 1-3-1. RATING (COMMAND)

    override fun ratingCommand() = "레이팅"
    override fun ratingCommandDescription() = "레이팅을 알아봅니다."
    override fun ratingCommandOptionUser() = "유저"
    override fun ratingCommandOptionUserDescription() = "레이팅을 알아볼 인민을 지정해 주세요."

    // ### 1-3-2. RATING:RESPONSE (EMBED)

    override fun ratingEmbed() = TODO("Not yet implemented")
    override fun ratingEmbedDescription() = TODO("Not yet implemented")

    // # 2. SETTING

    // ## 2-1. LANG

    // ### 2-1-2. LANG:SUCCEED:CHANGED (MESSAGE)

    override fun languageUpdated() = "말씨 설정이 조선말:flag_kp:로 바뀌었소."

    // ## 2-2. STYLE

    // ### 2-2-1. STYLE (COMMAND)

    override fun styleCommand() = "스타일"
    override fun styleCommandDescription() = "이 봉사기에서 쓰이는 오목판 스타일을 바꿉니다."
    override fun styleCommandOptionCode() = "스타일"
    override fun styleCommandOptionCodeDescription() = "스타일 부호를 정하시오."

    // ### 2-2-2. STYLE:LIST (EMBED)

    override fun styleEmbedTitle() = "GomokuBot / 스타일"
    override fun styleEmbedDescription() =
        "이 봉사기에 적용된 기본 오목판 스타일(``스타일 A``)이 제대로 보이지 않을 수 있습니다." +
                " 준비된 네 가지 스타일 중 마음에 드는 스타일 하나를 선택해 주세요."
    override fun styleEmbedSuggestion(styleName: String) = "이 스타일을 사용하려면 ``/스타일`` $styleName 시킴말를 입력해 주세요."

    // ### 2-2-3. STYLE:ERROR:NOTFOUND (MESSAGE)

    override fun styleErrorNotfound() = "동무, 스타일 지정에 오류가 있습니다. ``/스타일`` ``스타일 부호`` 형식으로 입력해 주세요."

    // ### 2-2-4. STYLE:SUCCEED:CHANGED (MESSAGE)

    override fun styleUpdated(styleName: String) = "스타일 설정이 스타일 ``${styleName}``로 바뀌었습니다!"

    // ## 2-3. POLICY

    override fun settingApplied(kind: String, choice: String) = "$choice 설정이 이 봉사기에 적용되었습니다."

    // ### 2-3-1. STYLE

    override fun styleSelectImage() = "화상"
    override fun styleSelectImageDescription() =
        "오목 판을 화상으로 표시하오. 플래트홈 봉사기 꼴에 따라 약간의 지연이 생길 수 있습니다."

    override fun styleSelectText() = "글뭉치"
    override fun styleSelectTextDescription() = "오목 판을 글뭉치로 표시합니다. 가장 단순하지만 가장 빠릅니다."

    override fun styleSelectSolidText() = "점박이 글뭉치"
    override fun styleSelectSolidTextDescription() = "글뭉치와 거의 같습니다. 다만 빈 자리에 공백이 아닌 점을 표시합니다."

    override fun styleSelectUnicodeText() = "만능부호"
    override fun styleSelectUnicodeTextDescription() =
        "오목 판을 만능부호 이모지로 표시합니다. 서체 설정에 따라 완전히 망가져 보일 수 있습니다."

    // ### 2-3-2. FOCUS

    override fun focusEmbedTitle() = "GomokuBot / 확대"
    override fun focusEmbedDescription() =
        "GomokuBot은 직관적인 입력을 돕기 위해 작은 크기의 \"단추 판\"을 사용합니다. GomokuBot이 어떤 부분을 어떻게 확대할지 정해주세요."

    override fun focusSelectIntelligence() = "지능적"
    override fun focusSelectIntelligenceDescription() =
        "GomokuBot 추론 엔진으로 가장 적절한 위치를 분석해 확대합니다."

    override fun focusSelectFallowing() = "추종적"
    override fun focusSelectFallowingDescription() =
        "항상 마지막 수를 가운데 둡니다."

    // ### 2-3-3. SWEEP

    override fun sweepEmbedTitle() = "GomokuBot / 청소"
    override fun sweepEmbedDescription() =
        "GomokuBot은 백두대간의 먼지만큼 통보를 보내오. GomokuBot이 보낸 통보를 어떻게 처리할지 정하시오."

    override fun sweepSelectRelay() = "숙청"
    override fun sweepSelectRelayDescription() =
        "다음 수를 놓을 때 이전에 보낸 통보를 모두 삭제합니다."

    override fun sweepSelectLeave() = "놓아두기"
    override fun sweepSelectLeaveDescription() =
        "그 어떤 통보도 삭제하지 않습니다."

    // ### 2-3-4. ARCHIVE

    override fun archiveEmbedTitle() = "GomokuBot / 공유"
    override fun archiveEmbedDescription() =
        "GomokuBot은 몇몇 멋진 놀음 결과들을 GomokuBot 공식 채널에 공유합니다. " +
                "물론 GomokuBot은 개인정보를 매우 중요하게 생각합니다. 놀음 결과를 어떻게 공유할지 정해주세요."

    override fun archiveSelectByAnonymous() = "익명"
    override fun archiveSelectByAnonymousDescription() =
        "익명으로 놀음 결과를 공유합니다."

    override fun archiveSelectWithProfile() = "기명"
    override fun archiveSelectWithProfileDescription() =
        "프로필 사진 그리고 닉네임과 함께 놀음 결과를 공유합니다."

    override fun archiveSelectPrivacy() = "비밀"
    override fun archiveSelectPrivacyDescription() =
        "그 어디에도 놀음 결과를 공유하지 않습니다."

    // # 3. SESSION

    override fun sessionNotFound(): String =
        "진행 중인 놀음을 찾을 수 없습니다. 먼저 ``/시작`` 시킴말로 놀음을 시작해 주세요."

    // ## 3-1. START

    // ### 3-1-1. START (COMMAND)

    override fun startCommand() = "시작"
    override fun startCommandDescription() = "새 놀음을 시작합니다."
    override fun startCommandOptionOpponent() = "상대"
    override fun startCommandOptionOpponentDescription() = "함께 놀음을 시작할 유저를 지정해 주세요."

    // ### 3-1-2. START:ERROR:ALREADY (MESSAGE)

    override fun startErrorSessionAlready() =
        "이미 진행 중인 놀음이 있습니다. 진행 중인 놀음을 먼저 마무리해 주세요."
    override fun startErrorOpponentSessionAlready(opponent: String) =
        "$opponent 동무은 이미 다른 놀음을 진행 중 입니다. $opponent 동무의 놀음이 끝날 때까지 기다리시오."
    override fun startErrorRequestAlreadySent(opponent: String) =
        "$opponent 동무에게 보낸 쌈박질 요청이 아직 남아 있습니다. $opponent 동무의 응답을 기다리시오."
    override fun startErrorRequestAlready(opponent: String) =
        "$opponent 동무이 보낸 쌈박질 요청에 아직 응답하지 않았습니다. $opponent 동무의 쌈박질 요청에 먼저 대답하시오."
    override fun startErrorOpponentRequestAlready(opponent: String) =
        "$opponent 동무에게는 아직 응답하지 않은 다른 쌈박질 요청이 있소. $opponent 동무가 다른 쌈박질 요청에 대답 할 때 까지 가다리시오."

    // ## 3-2. SET

    // ### 3-2-1. SET (COMMAND)

    override fun setCommandDescription() = "원하는 좌표에 돌을 놓습니다."
    override fun setCommandOptionColumn() = "가로"
    override fun setCommandOptionColumnDescription() = "미제말"
    override fun setCommandOptionRow() = "세로"
    override fun setCommandOptionRowDescription() = "숫자"

    // ### 3-2-2. SET:ERROR:ARGUMENT (MESSAGE)

    override fun setErrorIllegalArgument() =
        "시킴말 꼴에 오류가 있소. ``/s`` ``미제말`` ``숫자`` 꼴로 립력하시오."

    // ### 3-2-3. SET:ERROR:EXIST (MESSAGE)

    override fun setErrorExist(move: String) =
        "${move}에는 이미 돌이 놓여있소. 다른 곳에 돌을 놓으시오."

    // ### 3-2-4. SET:ERROR:FORBIDDEN (MESSAGE)

    override fun setErrorForbidden(move: String, forbiddenKind: String) =
        "``${move}`` 자리는 ``${forbiddenKind}금수``요. 다른 곳에 돌을 놓으시오."

    // ## 3-3. RESIGN

    // ### 3-3-1. RESIGN (COMMAND)

    override fun resignCommand() = "항복"
    override fun resignCommandDescription() = "진행중인 놀음을 포기하오."

    // ### 3-3-2. RESIGN:ERROR:NOTFOUND (MESSAGE)

    // ## 3-4. REQUEST

    // ### 3-4-1. REQUEST:ABOUT (EMBED)

    override fun requestEmbedTitle() = "오목 한 판 어떻소?"
    override fun requestEmbedDescription(owner: String, opponent: String) =
        "$owner 동무가 $opponent 동무에게 대결 요청을 보냈소. 아래 단추을 눌러 대답해 주시오."
    override fun requestEmbedButtonAccept() = "수락"
    override fun requestEmbedButtonReject() = "거절"

    // ### 3-4-2. REQUEST:REJECTED

    override fun requestRejected(owner: String, opponent: String) =
        "$opponent 동무가 $owner 동무의 대결 료청을 거절했소."

    override fun requestExpired(owner: String, opponent: String) =
        "$owner 동무가 $opponent 동무에게 보낸 대결 료청이 박살났소. 아직도 $opponent 동무과 대결하고 싶다면, 새 대결 요청을 보내시오."

    override fun requestExpiredNewRequest() = "다시 제안하기"

    // # 4. GAME

    // ## 4-1. BEGIN

    // ### 4-1-1. BEGIN:PVP

    override fun beginPVP(blackPlayer: String, whitePlayer: String) =
        "$blackPlayer 동무과 $whitePlayer 동무의 놀음이 시작되었소. $blackPlayer 동무가 흑이오. $blackPlayer 동무가 첫 번째 수를 놓으시오."

    // ### 4-1-2. BEGIN:AI

    override fun beginPVEAiBlack(player: String) =
        "$player 동무와 콤퓨타의 놀음이 시작되었소. $player 동무가 백이오. 콤퓨타은 ``h8``에 두었소. 두 번째 수를 놓으시오."

    override fun beginPVEAiWhite(player: String) =
        "$player 동무과 콤퓨타의 놀음이 시작되었소. $player 동무가 흑이오. 첫 번째 수를 놓으시오."

    // ## 4-2. PROCESS

    // ### 4-2-1. PROCESS:NEXT (MESSAGE)

    override fun processNextPVE(lastMove: String) =
        "다음 수를 놓으시오. 콤퓨타는 ${lastMove}에 놓았습니다."

    override fun processNextPVP(priorPlayer: String, lastMove: String) =
        "다음 수를 놓으시오. $priorPlayer 동무는 ${lastMove}에 놓았소."

    // ### 4-2-2. PROCESS:ERROR:ORDER (MESSAGE)

    override fun processErrorOrder(player: String) =
        "지금은 $player 동무의 차례요. $player 동무이 다음 수를 놓을 때까지 기다리시오."

    // ## 4-3. END

    // ### 4-3-1. END:PVP (MESSAGE)

    override fun endPVPWin(winner: String, loser: String, lastMove: String) =
        "$winner 동무가 ${lastMove}에 돌을 놓음으로써 $loser 동무을 이겼습니다."
    override fun endPVPResign(winner: String, loser: String) =
        "$loser 동무이 항복을 선언 함으로써 $winner 동무을 이겼습니다."
    override fun endPVPTie(owner: String, opponent: String) =
        "이제 더 이상 돌을 놓을 공간이 없으므로, $opponent 동무과 $opponent 동무의 놀음은 무승부로 끝났습니다."
    override fun endPVPTimeOut(winner: String, loser: String) =
        "$loser 동무이 오랜 시간 동안 다음 수를 두지 않았기 때문에 $winner 동무이 $loser 동무을 이겼습니다."

    // ### 4-3-2. END:AI (MESSAGE)

    override fun endPVEWin(player: String, lastPos: String) =
        "$lastPos 에 돌을 놓음으로써 콤퓨타을 이겼습니다. 축하합니다, $player 동무!"
    override fun endPVELose(player: String, lastPos: String) =
        "$player 동무, 콤퓨타이 $lastPos 에 돌을 놓음으로써 콤퓨타에 패배했습니다. 언제든지 다시 도전해 주세요."
    override fun endPVEResign(player: String) =
        "$player 동무, 콤퓨타 상대로 항복을 선언 함으로써 콤퓨타에 패배하셨습니다. 언제든지 다시 도전해 주세요."
    override fun endPVETie(player: String) =
        "이제 더 이상 돌을 놓을 공간이 없으므로, $player 동무과 콤퓨타의 놀음은 무승부로 끝났습니다."
    override fun endPVETimeOut(player: String) =
        "$player 동무, 오랜 시간 동안 다음 수를 두지 않았기 때문에 콤퓨타에 패배했습니다."

    // # 5. BOARD

    override fun boardInProgress() = "쌈박질 중"
    override fun boardFinished() = "초토화됨"

    override fun boardMoves() = "진행도"
    override fun boardLastMove() = "마지막 위치"

    override fun boardResult() = "결과"

    override fun boardWinDescription(winner: String) = "$winner 승리"
    override fun boardTieDescription() = "무승부"

    override fun boardCommandGuide() = ":mag: 단추을 누르거나 ``/s`` ``미제말`` ``숫자`` 시킴말를 입력해 다음 수를 놓아주세요."

    // # 6. UTILS

    override fun somethingWrongEmbedTitle() = "뭔가 잘못됐습니다!"

    // ## 6-1. PERMISSION-NOT-GRANTED (EMBED)

    override fun permissionNotGrantedEmbedDescription(channelName: String) =
        "Gokomubot은 $channelName 채널에 통보를 보낼 권한이 없습니다! 역할 및 퍼미션 설정을 확인해 주세요."

    override fun permissionNotGrantedEmbedFooter() = "이 통보는 1분 뒤 지워집니다."

    // ## 6-2. NOT-YET-IMPLEMENTED (EMBED)

    override fun notYetImplementedEmbedDescription() = "이 기능은 아직 완성되지 않았습니다."

    override fun notYetImplementedEmbedFooter(officialChannel: String) =
        "지원 채널($officialChannel)에서 Gomokubot 갱신 소식을 받아볼 수 있습니다."

}
