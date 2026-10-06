package core.interact.i18n

import core.assets.UNICODE_RIGHT
import core.engine.EngineLevel
import renju.notation.ColorContainer

open class LanguagePRK : LanguageKOR() {

    override val languageCode = "PRK"

    override val languageName = "\uD83C\uDDF0\uD83C\uDDF5 조선말"
    override val languageSuggestion = "`/lang` `PRK` 시킴말을 써 주시오."

    override fun engineLevel(engine: EngineLevel) = when (engine) {
        EngineLevel.AMOEBA -> "미제놈"
        EngineLevel.APE -> "쪽바리"
        EngineLevel.BEGINNER -> "로동자"
        EngineLevel.MODERATE -> "로동당원"
        EngineLevel.EXPERT -> "중앙당원"
        EngineLevel.GURU -> "혁명가"
        EngineLevel.SAGE -> "령도자"
    }

    override val swapSelectYes = "그렇소"
    override val swapSelectNo = "아니오"

    override val branchSelectSwap = "맞바꾸기"
    override val branchSelectOffer = "후보 제시"

    override val ruleSelectRenju = "렌주 (기본)"
    override val ruleSelectTaraguchi10 = "Taraguchi-10"
    override val ruleSelectSoosyrv8 = "Soosyrv-8"

    override val helpCommand = "도움말"
    override val helpCommandDescription = "도움말을 알아보오."
    override val helpCommandOptionShortcut = "바로가기"
    override val helpCommandOptionShortcutDescription = "원하는 도움말 쪽을 곧바로 펼치오."
    override val helpCommandOptionAnnouncements = "공지"

    override val settingsCommand = "설정"
    override val settingsCommandDescription = "설정 화면을 표시하오."

    override val helpAboutEmbedTitle = "GomokuBot / 도움말"
    override fun helpAboutEmbedDescription(platform: String) =
        "**$platform**에서도 **오목** 놀음을 즐겨보시오. **GomokuBot** 동지가 함께하오." +
                " - GomokuBot은 ${platform}에서 오목([렌주](https://www.renju.net/rules/)) 놀음을 제공하는 오픈소스 전자계산기 오목 봇이오."
    override val helpAboutEmbedDeveloper = "개발자"
    override val helpAboutEmbedRepository = "Git 저장소"
    override val helpAboutEmbedVersion = "판올림"
    override val helpAboutEmbedSupport = "지원 채널"
    override val helpAboutEmbedInvite = "초대 련결"

    override val commandUsageEmbedTitle = "GomokuBot / 시킴말"
    override val commandUsageHelp = "도움말을 알아보오."
    override val commandUsageSettings = "설정 화면을 표시하오."
    override val commandUsageRankGlobal = "1위부터 10위까지의 GomokuBot 전체 계급을 알아보오."
    override val commandUsageRankServer = "이 봉사기 안에서의 계급을 알아보오."
    override val commandUsageRankUser = "지정한 인민 상대의 계급을 알아보오."
    override val commandUsageReplay = "최근에 끝난 놀음 다시보기 목록을 알아보오."
    override val commandUsageRating = "`GomokuBot ELO` 계급수를 알아보오."

    override val commandUsageLang =
        "이 봉사기에서 쓰이는 언어 설정을 바꾸오. Ex) `/lang` `PRK`"
    override val commandUsageStyle =
        "이 봉사기에서 쓰이는 오목판 생김새를 바꾸오. Ex) `/생김새` `A`"

    override val commandUsageStartEngine = "전자계산기와 함께 새 놀음을 시작하오."
    override val commandUsageStartPvp =
        "지정한 인민에게 새 놀음을 제안하오. Ex) `/시작` `@인민`"
    override val commandUsageResign = "진행 중인 놀음에서 백기를 들오."

    override val commandUsageBoard = "지금 진행 중인 놀음을 새 통보문으로 펼치오."

    override val replayCommand = "다시보기"
    override val replayCommandDescription = "최근에 끝낸 놀음을 돌아보오."

    override val rankCommand = "계급"
    override val rankCommandDescription = "1위부터 10위까지의 계급을 알아보오."
    override val rankCommandSubGlobal = "전체"
    override val rankCommandSubGlobalDescription = "GomokuBot 전체 계급을 알아보오."
    override val rankCommandSubServer = "봉사기"
    override val rankCommandSubServerDescription = "봉사기 내부 계급을 알아보오."
    override val rankCommandSubUser = "인민"
    override val rankCommandSubUserDescription = "인민-상대 계급을 알아보오."
    override val rankCommandOptionPlayer = "인민"
    override val rankCommandOptionPlayerDescription = "상대 계급을 알아볼 인민을 지정해 주시오."

    override val rankErrorNotFound = "인민 기록을 찾을 수 없소. GomokuBot PvP 놀음 기록이 있는 인민을 지정해 주시오."

    override val rankEmbedTitle = "GomokuBot / 계급"
    override val rankEmbedDescription = "1위부터 10위까지의 승리 계급을 확인해 보시오."
    override val rankEmbedWin = "승"
    override val rankEmbedLose = "패"
    override val rankEmbedDraw = "무"

    override val ratingCommand = "계급수"
    override val ratingCommandDescription = "계급수를 알아보오."
    override val ratingCommandOptionUser = "인민"
    override val ratingCommandOptionUserDescription = "계급수를 알아볼 인민을 지정해 주시오."

    override val ratingNoRecord = "기록을 찾을 수 없소."

    override val languageCommand = "lang"
    override val languageCommandDescription = "이 봉사기에서 쓰이는 언어 설정을 바꾸오."
    override val languageCommandOptionCode = "언어"
    override val languageCommandOptionCodeDescription = "언어 부호를 고르시오."

    override val languageUpdated = "언어 설정이 조선말:flag_kp:로 바뀌었소. 공화국에 온 걸 환영하오."

    override val styleEmbedTitle = "GomokuBot / 생김새"
    override val styleEmbedDescription =
        "이 봉사기에 적용된 기본 오목판 생김새(`스타일 A`)가 제대로 보이지 않을 수 있소." +
                " 준비된 네 가지 생김새 가운데 마음에 드는 하나를 고르시오."
    override fun styleEmbedSuggestion(styleName: String) = "이 생김새를 쓰려면 `/생김새` $styleName 시킴말을 쓰시오."

    override val styleErrorNotfound =
        "생김새 지정이 잘못되었소. `/생김새` `생김새 부호` 형식으로 쓰시오."

    override fun styleUpdated(styleName: String) =
        "생김새 설정이 `${styleName}`로 바뀌었소."

    override fun settingApplied(kind: String, choice: String) = "$kind 설정이 ${choice}로 바뀌었소."

    override val style = "생김새"

    override val styleSelectImage = "그림"
    override val styleSelectImageDescription =
        "오목판을 그림으로 표시하오. 플랫폼 봉사기 상태에 따라 조금 늦어질 수 있소."

    override val styleSelectText = "수자"
    override val styleSelectTextDescription = "오목판을 수자로 표시하오. 가장 단순하지만 가장 빠르오."

    override val styleSelectDottedText = "점박이 수자"
    override val styleSelectDottedTextDescription = "수자와 거의 같소. 다만 빈 자리에 공백이 아닌 점을 표시하오."

    override val focus = "확대"

    override val focusEmbedTitle = "GomokuBot / 확대"
    override val focusEmbedDescription =
        "GomokuBot은 직관적인 립력을 돕기 위해 작은 크기의 \"단추판\"을 쓰오. GomokuBot이 어느 부분을 어떻게 확대할지 정하시오."

    override val focusSelectIntelligence = "지능적"
    override val focusSelectIntelligenceDescription =
        "GomokuBot 추론 기관으로 가장 적절한 자리를 분석해 확대하오."

    override val focusSelectCenter = "수동적"
    override val focusSelectCenterDescription =
        "언제나 마지막 수를 가운데에 두오."

    override val hint = "힌트"

    override val hintEmbedTitle = "GomokuBot / 힌트"
    override val hintEmbedDescription =
        "오목에는 승패를 가르는 중요한 자리가 있소. GomokuBot이 중요한 자리를 어떻게 강조할지 정하시오."

    override val hintSelectFive = "승리"
    override val hintSelectFiveDescription = "오목을 만들어 이길 수 있는 자리를 강조하오."

    override val hintSelectOff = "꺼짐"
    override val hintSelectOffDescription = "그 어떤 자리도 강조하지 않소."

    override val mark = "표시"

    override val markEmbedTitle = "GomokuBot / 표시"
    override val markEmbedDescription =
        "수많은 돌 사이에서 마지막으로 둔 자리를 기억하기는 쉬운 일이 아니오. GomokuBot이 마지막에 둔 돌을 어떻게 표시할지 정하시오."

    override val markSelectLast = "마지막 자리"
    override val markSelectLastDescription =
        "마지막에 둔 자리에 작은 점 하나를 찍소."

    override val markSelectRecent = "마지막 차례"
    override val markSelectRecentDescription =
        "상대가 마지막에 둔 자리에 작은 점을, 자신이 마지막에 둔 자리에 얇은 십자를 표시하오."

    override val markSelectSequence = "순서"
    override val markSelectSequenceDescription =
        "돌을 놓은 순서를 모두 표시하오."

    override val archive = "공유"

    override val archiveEmbedTitle = "GomokuBot / 공유"
    override val archiveEmbedDescription =
        "GomokuBot은 몇몇 멋진 놀음 결과를 GomokuBot 공식 채널에 공유하오. " +
                "물론 GomokuBot은 개인정보를 매우 중요하게 여기오. 놀음 결과를 어떻게 공유할지 정하시오."

    override val archiveSelectByAnonymous = "익명"
    override val archiveSelectByAnonymousDescription =
        "익명으로 놀음 결과를 공유하오."

    override val archiveSelectWithProfile = "기명"
    override val archiveSelectWithProfileDescription =
        "프로필 사진과 닉네임을 함께 붙여 놀음 결과를 공유하오."

    override val archiveSelectPrivacy = "비밀"
    override val archiveSelectPrivacyDescription =
        "그 어디에도 놀음 결과를 공유하지 않소."

    override val sessionNotFound: String =
        "진행 중인 놀음을 찾을 수 없소. 먼저 `/시작` 시킴말로 놀음을 시작하시오."

    override val startCommand = "시작"
    override val startCommandDescription = "새 놀음을 시작하오."
    override val startCommandOptionOpponent = "상대"
    override val startCommandOptionOpponentDescription = "함께 놀음을 시작할 인민을 지정해 주시오."
    override val startCommandOptionRule = "규칙"
    override val startCommandOptionRuleDescription = "새로 시작할 놀음의 규칙을 정하시오."

    override val startErrorSessionAlready =
        "이미 진행 중인 놀음이 있소. 먼저 진행 중인 놀음을 마무리하시오."
    override fun startErrorOpponentSessionAlready(opponent: String) =
        "$opponent 동지는 이미 다른 놀음을 진행 중이오. $opponent 동지의 놀음이 끝날 때까지 기다리시오."
    override fun startErrorRequestAlreadySent(opponent: String) =
        "$opponent 동지에게 보낸 놀음 요청이 아직 남아 있소. $opponent 동지의 대답을 기다리시오."
    override fun startErrorRequestAlready(opponent: String) =
        "$opponent 동지가 보낸 놀음 요청에 아직 대답하지 않았소. $opponent 동지의 놀음 요청에 먼저 대답하시오."
    override fun startErrorOpponentRequestAlready(opponent: String) =
        "$opponent 동지에게는 아직 대답하지 않은 다른 놀음 요청 하나가 남아 있소. $opponent 동지가 그 요청에 대답할 때까지 기다리시오."

    override val setCommandDescription = "원하는 자리표에 돌을 놓소."
    override val setCommandOptionPosition = "position"
    override val setCommandOptionPositionDescription = "a1부터 o15까지의 자리표"

    override val setErrorIllegalArgument =
        "잘못된 시킴말 형식이오. `/s` `h8` 꼴로 쓰시오."

    override fun setErrorExist(move: String) =
        "${move}에는 이미 돌이 놓여 있소. 다른 곳에 돌을 놓으시오."

    override fun setErrorForbidden(move: String, forbiddenKind: String) =
        "${move}은(는) ${forbiddenKind}금수요. 다른 곳에 돌을 놓으시오."

    override val resignCommand = "백기"
    override val resignCommandDescription = "진행 중인 놀음에서 백기를 들오."

    override val undoCommand = "무르기"
    override val undoCommandDescription = "마지막 수에 대한 무르기를 요청하오."
    override val undoErrorOpening = "오프닝 도중에는 무르기를 쓸 수 없소."
    override val undoErrorNoMoves = "아직 무를 수 있는 착수가 없소."
    override val undoErrorLimit = "이 놀음의 무르기 두 번을 모두 썼소."
    override fun undoCompleted(remainingUndos: Int) = "무르기를 썼소. 남은 무르기 횟수는 ${remainingUndos}회요."
    override val undoPvpCompleted = "마지막 한 수를 무렀소."
    override val undoRequestEmbedTitle = "한 수 무르겠소?"
    override fun undoRequestEmbedDescription(requester: String, opponent: String) =
        "$requester 동지가 $opponent 동지에게 무르기를 요청했소. 아래 단추를 눌러 대답하시오. 다음 수를 두면 요청은 무효가 되오."
    override fun undoRequestRejected(requester: String, opponent: String) =
        "$opponent 동지가 $requester 동지의 무르기 요청을 거절했소."
    override fun undoRequestExpired(requester: String, opponent: String) =
        "$requester 동지가 $opponent 동지에게 보낸 무르기 요청이 만료되었소."

    override val boardCommand = "판"
    override val boardCommandDescription = "지금 진행 중인 놀음을 새 통보문으로 펼치오."

    override val requestEmbedTitle = "오목 한 판 어떻소?"
    override fun requestEmbedDescription(requester: String, opponent: String) =
        "$requester 동지가 $opponent 동지에게 놀음 요청을 보냈소. 아래 단추를 눌러 대답하시오."
    override val requestEmbedButtonAccept = "수락"
    override val requestEmbedButtonReject = "숙청"

    override fun requestRejected(requester: String, opponent: String) =
        "$opponent 동지가 $requester 동지의 놀음 요청을 숙청했소."

    override fun requestExpired(requester: String, opponent: String) =
        "$requester 동지가 $opponent 동지에게 보낸 놀음 요청이 만료되었소. 아직도 $opponent 동지와 대결하고 싶다면 새 놀음 요청을 보내시오."

    override val requestExpiredNewRequest =
        "다시 제안하기"

    override fun beginPvp(players: ColorContainer<String>) =
        "${players.black} 동지와 ${players.white} 동지의 놀음이 시작되었소. ${players.black} 동지가 흑이오. ${players.black} 동지가 첫 번째 수를 놓으시오."

    override fun beginOpening(players: ColorContainer<String>) =
        "${players.black} 동지와 ${players.white} 동지의 오프닝 놀음이 시작되었소. ${players.black} 동지가 흑이오. ${players.white} 동지는 흑으로 맞바꿀지, 그대로 둘지 정하시오."

    override fun beginEngineBlack(player: String, gomokubot: String) =
        "$player 동지와 ${gomokubot}의 놀음이 시작되었소. $player 동지는 백이오. 전자계산기는 `h8`에 두었소. 두 번째 수를 놓으시오."

    override fun beginEngineWhite(player: String, gomokubot: String) =
        "$player 동지와 ${gomokubot}의 놀음이 시작되었소. $player 동지가 흑이오. 첫 번째 수를 놓으시오."

    override fun processNextEngine(lastMove: String) =
        "다음 수를 놓으시오. 전자계산기는 ${lastMove}에 놓았소."

    override fun processNextPvp(opponent: String, lastMove: String) =
        "다음 수를 놓으시오. $opponent 동지는 ${lastMove}에 놓았소."

    override fun processNextOpening(lastMove: String) =
        "${lastMove}에 돌을 놓았소. 다음 오프닝 절차를 따르시오."

    override fun processErrorOrder(player: String) =
        "지금은 $player 동지의 차례요. $player 동지가 다음 수를 놓을 때까지 기다리시오."

    override fun gameResultFiveInRow(winner: String, loser: String) =
        "$winner 동지가 오목을 만들어 $loser 동지를 숙청했소."

    override fun gameResultResign(winner: String, loser: String) =
        "$loser 동지가 백기를 들어 $winner 동지가 숙청됐소."

    override val gameResultDraw =
        "더는 돌을 둘 곳이 없어 놀음은 무승부로 끝났소."

    override fun gameResultTimeout(winner: String, loser: String) =
        "$loser 동지가 제한 시간 안에 다음 수를 두지 않아 $winner 동지가 숙청됐소."

    override val gameResultEngineRating = "계급수"

    override val gameResultEngineRatingChange = "획득 계급수"

    override val boardInProgress = "진행 중"
    override val boardInOpening = "오프닝 중"
    override val boardFinished = "종료"

    override val boardMoves = "진행도"
    override val boardLastMove = "마지막 자리"

    override val boardResult = "결과"

    override fun boardWinDescription(winner: String) = "$winner 승리"
    override val boardTieDescription = "무승부"

    override val boardCommandGuide =
        ":mag: 단추를 누르거나 `/s` `자리표` 시킴말로 다음 수를 놓으시오."
    override val boardSwapGuide =
        ":arrows_counterclockwise: 단추를 눌러 흑과 백을 맞바꿀지 정하시오."
    override fun boardStatefulSwapGuide(offerCount: Int) =
        ":arrows_counterclockwise: 단추를 눌러 흑과 백을 맞바꿀지 정하시오. 흑이 내놓아야 할 5번째 수 후보는 `$offerCount`개요."
    override val boardBranchGuide =
        ":paperclips: 단추를 눌러 흑과 백을 맞바꿀 기회를 얻을지, 5번째 수 후보 10개를 상대에게 제안할지 정하시오."
    override val boardDeclareGuide =
        ":paperclips: 선택 차림표에서 5번째 수 후보를 몇 개 고를지 정하시오."
    override val boardSelectGuide =
        ":dart: 단추를 누르거나 `/s` `자리표` 시킴말로 5번째 수를 고르시오."
    override fun boardOfferGuide(remainingMoves: Int) =
        ":question: 단추를 누르거나 `/s` `position` 시킴말로 5번째 수 후보 ${remainingMoves}개를 정하시오."

    override val replayEmbedWin = "승"
    override val replayEmbedLose = "패"
    override val replayEmbedDraw = "무"
    override fun replayEmbedMatchInfo(totalMoves: Int) = "총 ${totalMoves}수."
    override val replayEmbedUnableToReplayDescription = "빈 놀음이라 보여줄 것이 없소. 다른 놀음을 고르시오."

    override fun announceWrittenOn(date: String) = "$date 에 쓰여짐"

    override val somethingWrongEmbedTitle = "무언가 잘못되었소."

    override fun permissionNotGrantedEmbedDescription(channelName: String) =
        "GomokuBot은 $channelName 채널에 통보문을 보낼 권한이 없소. 역할과 권한 설정을 확인하시오."

    override val permissionNotGrantedEmbedFooter = "이 통보문은 1분 뒤 지워지오."

    override val notYetImplementedEmbedDescription = "이 기능은 아직 완성되지 않았소."

    override val notYetImplementedEmbedFooter =
        "지원 채널(https://discord.gg/vq8pkfF)에서 GomokuBot 판올림 소식을 받아볼 수 있소."

    override val exploreAboutRenju = "렌주가 무엇인지 모르시오? $UNICODE_RIGHT 를 눌러 렌주에 대해 알아보시오."

}
