package core.interact.i18n

open class LanguageKOR : LanguageENG() {

    override fun languageCode() = "KOR"

    override fun languageName() = "\uD83C\uDDF0\uD83C\uDDF7 한국어"
    override fun languageSuggestion() = "``/lang`` ``KOR`` 명령어를 사용해주세요."

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
    override fun helpCommandDescription() = "도움말을 알아봅니다."

    override fun settingsCommand() = "설정"
    override fun settingsCommandDescription() = "설정 화면을 표시합니다."

    // ### 1-1-2. HELP:ABOUT (EMBED)

    override fun helpAboutEmbedTitle() = "GomokuBot / 도움말"
    override fun helpAboutEmbedDescription(platform: String) =
        "이제 **$platform**에서도 **오목**을 즐겨 보세요. **GomokuBot**이 함께 하겠습니다." +
                " ― GomokuBot은 ${platform}에서 오목([렌주](https://www.renju.net/rules/)) 기능을 제공하는 오픈소스 인공지능 오목 봇입니다. " +
                "수집된 기보 데이터는 강화학습 모델 훈련에 사용됩니다."
    override fun helpAboutEmbedDeveloper() = "개발자"
    override fun helpAboutEmbedRepository() = "Git 저장소"
    override fun helpAboutEmbedVersion() = "버전"
    override fun helpAboutEmbedSupport() = "지원 채널"
    override fun helpAboutEmbedInvite() = "초대 링크"

    // ### 1-1-3. HELP:COMMAND (EMBED)

    override fun helpCommandEmbedTitle() = "GomokuBot / 명령어"
    override fun helpCommandEmbedHelp() = "도움말을 알아봅니다."
    override fun helpCommandEmbedSettings() = "설정 화면을 표시합니다."
    override fun helpCommandEmbedRank() = "1위부터 10위까지의 순위를 알아봅니다."
    override fun helpCommandEmbedRating() = "``GomokuBot ELO`` 레이팅을 알아봅니다."

    override fun helpCommandEmbedLang(langList: String) =
        "이 서버에서 쓰이는 언어 설정을 바꿉니다. Ex) ``/lang`` ``ENG``"
    override fun helpCommandEmbedStyle() =
        "이 서버에서 쓰이는 오목판 모양을 바꿉니다. Ex) ``/스타일`` ``A``"

    override fun helpCommandEmbedStartPVE() = "인공지능과 함께 새 게임을 시작합니다."
    override fun helpCommandEmbedStartPVP() =
        "멘션 된 유저에게 새 게임을 제안합니다. Ex) ``/시작`` ``@유저``"
    override fun helpCommandEmbedResign() = "진행 중인 게임을 포기합니다."

    // ## 1-2. RANK

    // ### 1-2-1. RANK (COMMAND)

    override fun rankCommand() = "순위"
    override fun rankCommandDescription() = "1위부터 10위까지의 순위를 알아봅니다."

    // ### 1-2-2. RANK:LIST (EMBED)

    override fun rankEmbedTitle() = "GomokuBot / 순위"
    override fun rankEmbedDescription() = "1위부터 10위까지의 승리 순위"
    override fun rankEmbedWin() = "승"
    override fun rankEmbedLose() = "패"
    override fun rankEmbedDraw() = "무"

    // ## 1-3. RATING

    // ### 1-3-1. RATING (COMMAND)

    override fun ratingCommand() = "레이팅"
    override fun ratingCommandDescription() = "레이팅을 알아봅니다."
    override fun ratingCommandOptionUser() = "유저"
    override fun ratingCommandOptionUserDescription() = "레이팅을 알아볼 유저를 지정해 주세요."

    // ### 1-3-2. RATING:RESPONSE (EMBED)

    override fun ratingEmbed() = TODO("Not yet implemented")
    override fun ratingEmbedDescription() = TODO("Not yet implemented")

    // # 2. SETTING

    // ## 2-1. LANG

    // ### 2-1-2. LANG:SUCCEED:CHANGED (MESSAGE)

    override fun languageUpdated() = "언어 설정이 한국어:flag_kr:로 바뀌었습니다!"

    // ## 2-2. STYLE

    // ### 2-2-1. STYLE (COMMAND)

    override fun styleCommand() = "스타일"
    override fun styleCommandDescription() = "이 서버에서 쓰이는 오목판 스타일을 바꿉니다."
    override fun styleCommandOptionCode() = "스타일"
    override fun styleCommandOptionCodeDescription() = "스타일 코드를 지정해 주세요."

    // ### 2-2-2. STYLE:LIST (EMBED)

    override fun styleEmbedTitle() = "GomokuBot / 스타일"
    override fun styleEmbedDescription() =
        "이 서버에 적용된 기본 오목판 스타일(``스타일 A``)이 제대로 보이지 않을 수 있습니다." +
                " 준비된 네 가지 스타일 중 마음에 드는 스타일 하나를 선택해 주세요."
    override fun styleEmbedSuggestion(styleName: String) = "이 스타일을 사용하려면 ``/스타일`` $styleName 명령어를 입력해 주세요."

    // ### 2-2-3. STYLE:ERROR:NOTFOUND (MESSAGE)

    override fun styleErrorNotfound(user: String) = "$user 님, 스타일 지정에 오류가 있습니다. ``/스타일`` ``스타일 코드`` 형식으로 입력해 주세요."

    // ### 2-2-4. STYLE:SUCCEED:CHANGED (MESSAGE)

    override fun styleUpdated(styleName: String) = "스타일 설정이 스타일 ``${styleName}``로 바뀌었습니다!"

    // ## 2-3. POLICY

    override fun settingApplied(choice: String) = "$choice 설정이 이 서버에 적용되었습니다."

    // ### 2-3-1. STYLE

    override fun styleSelectImage() = "이미지"
    override fun styleSelectImageDescription() =
        "오목 판을 이미지로 표시합니다. 플랫폼 서버 상태에 따라 약간의 지연이 생길 수 있습니다."

    override fun styleSelectText() = "텍스트"
    override fun styleSelectTextDescription() = "오목 판을 텍스트로 표시합니다. 가장 단순하지만 가장 빠릅니다."

    override fun styleSelectSolidText() = "점박이 텍스트"
    override fun styleSelectSolidTextDescription() = "텍스트와 거의 같습니다. 다만 빈 자리에 공백이 아닌 점을 표시합니다."

    override fun styleSelectUnicodeText() = "유니코드"
    override fun styleSelectUnicodeTextDescription() =
        "오목 판을 유니코드 이모지로 표시합니다. 폰트 설정에 따라 완전히 망가져 보일 수 있습니다."

    // ### 2-3-2. FOCUS

    override fun focusEmbedTitle() = "GomokuBot / 확대"
    override fun focusEmbedDescription() =
        "GomokuBot은 직관적인 입력을 돕기 위해 작은 크기의 \"버튼 판\"을 사용합니다. GomokuBot이 어떤 부분을 어떻게 확대할지 정해주세요."

    override fun focusSelectIntelligence() = "지능적"
    override fun focusSelectIntelligenceDescription() =
        "GomokuBot 추론 엔진으로 가장 적절한 위치를 분석해 확대합니다."

    override fun focusSelectFallowing() = "추종적"
    override fun focusSelectFallowingDescription() =
        "항상 마지막 수를 가운데 둡니다."

    // ### 2-3-3. SWEEP

    override fun sweepEmbedTitle() = "GomokuBot / 청소"
    override fun sweepEmbedDescription() =
        "GomokuBot은 아주, 아주 많은 양의 메시지를 보냅니다. GomokuBot이 보낸 메시지를 어떻게 처리할지 정해주세요."

    override fun sweepSelectRelay() = "이어가기"
    override fun sweepSelectRelayDescription() =
        "다음 수를 놓을 때 이전에 보낸 메시지를 모두 삭제합니다."

    override fun sweepSelectLeave() = "놓아두기"
    override fun sweepSelectLeaveDescription() =
        "그 어떤 메시지도 삭제하지 않습니다."

    // ### 2-3-4. ARCHIVE

    override fun archiveEmbedTitle() = "GomokuBot / 공유"
    override fun archiveEmbedDescription() =
        "GomokuBot은 몇몇 멋진 게임 결과들을 GomokuBot 공식 채널에 공유합니다. " +
                "물론 GomokuBot은 개인정보를 매우 중요하게 생각합니다. 게임 결과를 어떻게 공유할지 정해주세요."

    override fun archiveSelectByAnonymous() = "익명"
    override fun archiveSelectByAnonymousDescription() =
        "익명으로 게임 결과를 공유합니다."

    override fun archiveSelectWithProfile() = "기명"
    override fun archiveSelectWithProfileDescription() =
        "프로필 사진 그리고 닉네임과 함께 게임 결과를 공유합니다."

    override fun archiveSelectPrivacy() = "비밀"
    override fun archiveSelectPrivacyDescription() =
        "그 어디에도 게임 결과를 공유하지 않습니다."

    // # 3. SESSION

    override fun sessionNotFound(user: String): String =
        "$user 님, 진행 중인 게임을 찾을 수 없습니다. 먼저 ``/시작`` 명령어로 게임을 시작해 주세요."

    // ## 3-1. START

    // ### 3-1-1. START (COMMAND)

    override fun startCommand() = "시작"
    override fun startCommandDescription() = "새 게임을 시작합니다."
    override fun startCommandOptionOpponent() = "상대"
    override fun startCommandOptionOpponentDescription() = "함께 게임을 시작할 유저를 지정해 주세요."

    // ### 3-1-2. START:ERROR:ALREADY (MESSAGE)

    override fun startErrorSessionAlready(user: String) =
        "$user 님, 이미 진행 중인 게임이 있습니다. 진행 중인 게임을 먼저 마무리해 주세요."
    override fun startErrorOpponentSessionAlready(owner: String, opponent: String) =
        "$owner 님, $opponent 님은 이미 다른 게임을 진행 중 입니다. $opponent 님이 진행 중인 게임이 끝날 때까지 기다려 주세요."
    override fun startErrorRequestAlreadySent(owner: String, opponent: String) =
        "$owner 님, $opponent 님에게 보낸 대결 요청이 아직 남아 있습니다. $opponent 님의 응답을 기다려 주세요."
    override fun startErrorRequestAlready(user: String, opponent: String) =
        "$user 님, $opponent 님이 보낸 대결 요청에 아직 응답하지 않았습니다. $opponent 님의 대결 요청에 먼저 응답해 주세요."
    override fun startErrorOpponentRequestAlready(owner: String, opponent: String) =
        "$owner 님, $opponent 님에게는 아직 응답하지 않은 다른 대결 요청 하나가 남아 있습니다. $opponent 님이 다른 대결 요청에 응답할 때까지 기다려 주세요."

    // ## 3-2. SET

    // ### 3-2-1. SET (COMMAND)

    override fun setCommandDescription() = "원하는 좌표에 돌을 놓습니다."
    override fun setCommandOptionColumn() = "x"
    override fun setCommandOptionColumnDescription() = "알파벳"
    override fun setCommandOptionRow() = "y"
    override fun setCommandOptionRowDescription() = "숫자"

    // ### 3-2-2. SET:ERROR:ARGUMENT (MESSAGE)

    override fun setErrorIllegalArgument(player: String) =
        "$player 님, 명령어 형식에 오류가 있습니다. ``/s`` ``알파벳`` ``숫자`` 꼴로 입력해 주세요."

    // ### 3-2-3. SET:ERROR:EXIST (MESSAGE)

    override fun setErrorExist(player: String, move: String) =
        "$player 님, ${move}에는 이미 돌이 놓여 있습니다. 다른 곳에 돌을 놓아주세요."

    // ### 3-2-4. SET:ERROR:FORBIDDEN (MESSAGE)

    override fun setErrorForbidden(player: String, move: String, forbiddenKind: String) =
        "$player 님, ``${move}``은(는) ``${forbiddenKind}금수`` 입니다. 다른 곳에 돌을 놓아주세요."

    // ## 3-3. RESIGN

    // ### 3-3-1. RESIGN (COMMAND)

    override fun resignCommand() = "항복"
    override fun resignCommandDescription() = "진행중인 게임을 포기합니다."

    // ### 3-3-2. RESIGN:ERROR:NOTFOUND (MESSAGE)

    // ## 3-4. REQUEST

    // ### 3-4-1. REQUEST:ABOUT (EMBED)

    override fun requestEmbedTitle() = "오목 한 판 괜찮겠습니까?"
    override fun requestEmbedDescription(owner: String, opponent: String) =
        "$owner 님이 $opponent 님에게 대결 요청을 보냈습니다. 아래 버튼을 눌러 대답해 주세요."
    override fun requestEmbedButtonAccept() = "수락"
    override fun requestEmbedButtonReject() = "거절"

    // ### 3-4-2. REQUEST:REJECTED

    override fun requestRejected(owner: String, opponent: String) =
        "$opponent 님이 $owner 님의 대결 요청을 거절했습니다."

    override fun requestExpired(owner: String, opponent: String) =
        "$owner 님이 $opponent 님에게 보낸 대결 요청이 만료되었습니다. 아직도 $opponent 님과 대결하고 싶다면, 새 대결 요청을 보내주세요."

    override fun requestExpiredNewRequest() = "다시 제안하기"

    // # 4. GAME

    // ## 4-1. BEGIN

    // ### 4-1-1. BEGIN:PVP

    override fun beginPVP(blackPlayer: String, whitePlayer: String) =
        "$blackPlayer 님과 $whitePlayer 님의 게임이 시작되었습니다! $blackPlayer 님이 흑입니다. $blackPlayer 님이 첫 번째 수를 놓아주세요."

    // ### 4-1-2. BEGIN:AI

    override fun beginPVEAiBlack(player: String) =
        "$player 님과 인공지능의 게임이 시작되었습니다! $player 님은 백입니다. 인공지능은 ``h8``에 두었습니다. 두 번째 수를 놓아주세요."

    override fun beginPVEAiWhite(player: String) =
        "$player 님과 인공지능의 게임이 시작되었습니다! $player 님이 흑입니다. 첫 번째 수를 놓아주세요."

    // ## 4-2. PROCESS

    // ### 4-2-1. PROCES:NEXT (MESSAGE)

    override fun processNextPVE(owner: String, latestMove: String) =
        "$owner 님, 다음 수를 놓아주세요. AI는 ${latestMove}에 놓았습니다."

    override fun processNextPVP(player: String, priorPlayer: String, latestMove: String) =
        "$player 님, 다음 수를 놓아주세요. $priorPlayer 님은 ${latestMove}에 놓았습니다."

    // ### 4-2-2. PROCESS:ERROR:ORDER (MESSAGE)

    override fun processErrorOrder(user: String, player: String) =
        "$user 님, 지금은 $player 님의 차례입니다. $player 님이 다음 수를 놓을 때까지 기다려 주세요."

    // ## 4-3. END

    // ### 4-3-1. END:PVP (MESSAGE)

    override fun endPVPWin(winner: String, looser: String, latestMove: String) =
        "$winner 님이 ${latestMove}에 돌을 놓음으로써 $looser 님을 이겼습니다."
    override fun endPVPResign(winner: String, looser: String) =
        "$looser 님이 항복을 선언 함으로써 $winner 님을 이겼습니다."
    override fun endPVPTie(owner: String, opponent: String) =
        "이제 더 이상 돌을 놓을 공간이 없으므로, $opponent 님과 $opponent 님의 게임은 무승부로 끝났습니다."
    override fun endPVPTimeOut(winner: String, looser: String) =
        "$looser 님이 오랜 시간 동안 다음 수를 두지 않았기 때문에 $winner 님이 $looser 님을 이겼습니다."

    // ### 4-3-2. END:AI (MESSAGE)

    override fun endPVEWin(player: String, latestPos: String) =
        "$latestPos 에 돌을 놓음으로써 인공지능을 이겼습니다. 축하합니다, $player 님!"
    override fun endPVELose(player: String, latestPos: String) =
        "$player 님, 인공지능이 $latestPos 에 돌을 놓음으로써 인공지능에 패배했습니다. 언제든지 다시 도전해 주세요."
    override fun endPVEResign(player: String) =
        "$player 님, 인공지능 상대로 항복을 선언 함으로써 인공지능에 패배하셨습니다. 언제든지 다시 도전해 주세요."
    override fun endPVETie(player: String) =
        "이제 더 이상 돌을 놓을 공간이 없으므로, $player 님과 인공지능의 게임은 무승부로 끝났습니다."
    override fun endPVETimeOut(player: String) =
        "$player 님, 오랜 시간 동안 다음 수를 두지 않았기 때문에 인공지능에 패배했습니다."

    // # 5. BOARD

    override fun boardInProgress() = "진행 중"
    override fun boardFinished() = "종료"

    override fun boardMoves() = "진행도"
    override fun boardLatestMove() = "마지막 위치"

    override fun boardResult() = "결과"

    override fun boardWinDescription(winner: String) = "$winner 승리"
    override fun boardTieDescription() = "무승부"

    override fun boardCommandGuide() = ":mag: 버튼을 누르거나 ``/s`` ``알파벳`` ``숫자`` 명령어를 입력해 다음 수를 놓아주세요."

    // # 6. UTILS

    override fun announceWrittenOn(date: String) = "$date 에 쓰여짐"

    override fun somethingWrongEmbedTitle() = "뭔가 잘못됐습니다!"

    // ## 6-1. PERMISSION-NOT-GRANTED (EMBED)

    override fun permissionNotGrantedEmbedDescription(channelName: String) =
        "Gokomubot은 $channelName 채널에 메시지를 보낼 권한이 없습니다! 역할 및 퍼미션 설정을 확인해 주세요."

    override fun permissionNotGrantedEmbedFooter() = "이 메시지는 1분 뒤 지워집니다."

    // ## 6-2. NOT-YET-IMPLEMENTED (EMBED)

    override fun notYetImplementedEmbedDescription() = "이 기능은 아직 완성되지 않았습니다."

    override fun notYetImplementedEmbedFooter(officialChannel: String) =
        "지원 채널($officialChannel)에서 Gomokubot 업데이트 소식을 받아볼 수 있습니다."

}
