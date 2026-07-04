package core.interact.i18n

import core.assets.UNICODE_RIGHT
import renju.notation.ColorContainer

open class LanguagePRK : LanguageKOR() {

    override fun languageCode() = "PRK"

    override fun languageName() = "\uD83C\uDDF0\uD83C\uDDF5 조선말"
    override fun languageSuggestion() = "``/lang`` ``PRK`` 시킴말을 써 주시오."

    override fun engineLevelAmoeba() = "미제놈"
    override fun engineLevelApe() = "쪽바리"
    override fun engineLevelBeginner() = "로동자"
    override fun aiLevelIntermediate() = "로동당원"
    override fun engineLevelAdvanced() = "중앙당원"
    override fun engineLevelExpert() = "혁명가"
    override fun engineLevelGuru() = "령도자"

    override fun swapSelectYes() = "그렇소"
    override fun swapSelectNo() = "아니오"

    override fun branchSelectSwap() = "맞바꾸기"
    override fun branchSelectOffer() = "후보 제시"

    override fun ruleSelectRenju() = "렌주 (기본)"
    override fun ruleSelectTaraguchi10() = "Taraguchi-10"
    override fun ruleSelectSoosyrv8() = "Soosyrv-8"

    override fun helpCommand() = "도움말"
    override fun helpCommandDescription() = "도움말을 알아보오."
    override fun helpCommandOptionShortcut() = "바로가기"
    override fun helpCommandOptionShortcutDescription() = "원하는 도움말 쪽을 곧바로 펼치오."
    override fun helpCommandOptionAnnouncements() = "공지"

    override fun settingsCommand() = "설정"
    override fun settingsCommandDescription() = "설정 화면을 표시하오."

    override fun helpAboutEmbedTitle() = "GomokuBot / 도움말"
    override fun helpAboutEmbedDescription(platform: String) =
        "**$platform**에서도 **오목** 놀음을 즐겨보시오. **GomokuBot** 동지가 함께하오." +
                " - GomokuBot은 ${platform}에서 오목([렌주](https://www.renju.net/rules/)) 놀음을 제공하는 오픈소스 전자계산기 오목 봇이오."
    override fun helpAboutEmbedDeveloper() = "개발자"
    override fun helpAboutEmbedRepository() = "Git 저장소"
    override fun helpAboutEmbedVersion() = "판올림"
    override fun helpAboutEmbedSupport() = "지원 채널"
    override fun helpAboutEmbedInvite() = "초대 련결"

    override fun commandUsageEmbedTitle() = "GomokuBot / 시킴말"
    override fun commandUsageHelp() = "도움말을 알아보오."
    override fun commandUsageSettings() = "설정 화면을 표시하오."
    override fun commandUsageRankGlobal() = "1위부터 10위까지의 GomokuBot 전체 계급을 알아보오."
    override fun commandUsageRankServer() = "이 봉사기 안에서의 계급을 알아보오."
    override fun commandUsageRankUser() = "지정한 인민 상대의 계급을 알아보오."
    override fun commandUsageReplay() = "최근에 끝난 놀음 다시보기 목록을 알아보오."
    override fun commandUsageRating() = "``GomokuBot ELO`` 계급수를 알아보오."

    override fun commandUsageLang(langList: String) =
        "이 봉사기에서 쓰이는 언어 설정을 바꾸오. Ex) ``/lang`` ``PRK``"
    override fun commandUsageStyle() =
        "이 봉사기에서 쓰이는 오목판 생김새를 바꾸오. Ex) ``/생김새`` ``A``"

    override fun commandUsageStartEngine() = "전자계산기와 함께 새 놀음을 시작하오."
    override fun commandUsageStartPvp() =
        "지정한 인민에게 새 놀음을 제안하오. Ex) ``/시작`` ``@인민``"
    override fun commandUsageResign() = "진행 중인 놀음에서 백기를 들오."

    override fun commandUsageBoard() = "지금 진행 중인 놀음을 새 통보문으로 펼치오."

    override fun replayCommand() = "다시보기"
    override fun replayCommandDescription() = "최근에 끝낸 놀음을 돌아보오."

    override fun rankCommand() = "계급"
    override fun rankCommandDescription() = "1위부터 10위까지의 계급을 알아보오."
    override fun rankCommandSubGlobal() = "전체"
    override fun rankCommandSubGlobalDescription() = "GomokuBot 전체 계급을 알아보오."
    override fun rankCommandSubServer() = "봉사기"
    override fun rankCommandSubServerDescription() = "봉사기 내부 계급을 알아보오."
    override fun rankCommandSubUser() = "인민"
    override fun rankCommandSubUserDescription() = "인민-상대 계급을 알아보오."
    override fun rankCommandOptionPlayer() = "인민"
    override fun rankCommandOptionPlayerDescription() = "상대 계급을 알아볼 인민을 지정해 주시오."

    override fun rankErrorNotFound() = "인민 기록을 찾을 수 없소. GomokuBot PvP 놀음 기록이 있는 인민을 지정해 주시오."

    override fun rankEmbedTitle() = "GomokuBot / 계급"
    override fun rankEmbedDescription() = "1위부터 10위까지의 승리 계급을 확인해 보시오."
    override fun rankEmbedWin() = "승"
    override fun rankEmbedLose() = "패"
    override fun rankEmbedDraw() = "무"

    override fun ratingCommand() = "계급수"
    override fun ratingCommandDescription() = "계급수를 알아보오."
    override fun ratingCommandOptionUser() = "인민"
    override fun ratingCommandOptionUserDescription() = "계급수를 알아볼 인민을 지정해 주시오."

    override fun ratingNoRecord() = "기록을 찾을 수 없소."

    override fun languageCommand() = "lang"
    override fun languageCommandDescription() = "이 봉사기에서 쓰이는 언어 설정을 바꾸오."
    override fun languageCommandOptionCode() = "언어"
    override fun languageCommandOptionCodeDescription() = "언어 부호를 고르시오."

    override fun languageUpdated() = "언어 설정이 조선말:flag_kp:로 바뀌었소. 공화국에 온 걸 환영하오."

    override fun styleEmbedTitle() = "GomokuBot / 생김새"
    override fun styleEmbedDescription() =
        "이 봉사기에 적용된 기본 오목판 생김새(``스타일 A``)가 제대로 보이지 않을 수 있소." +
                " 준비된 네 가지 생김새 가운데 마음에 드는 하나를 고르시오."
    override fun styleEmbedSuggestion(styleName: String) = "이 생김새를 쓰려면 ``/생김새`` $styleName 시킴말을 쓰시오."

    override fun styleErrorNotfound() =
        "생김새 지정이 잘못되었소. ``/생김새`` ``생김새 부호`` 형식으로 쓰시오."

    override fun styleUpdated(styleName: String) =
        "생김새 설정이 ``${styleName}``로 바뀌었소."

    override fun settingApplied(kind: String, choice: String) = "$kind 설정이 ${choice}로 바뀌었소."

    override fun style() = "생김새"

    override fun styleSelectImage() = "그림"
    override fun styleSelectImageDescription() =
        "오목판을 그림으로 표시하오. 플랫폼 봉사기 상태에 따라 조금 늦어질 수 있소."

    override fun styleSelectText() = "수자"
    override fun styleSelectTextDescription() = "오목판을 수자로 표시하오. 가장 단순하지만 가장 빠르오."

    override fun styleSelectDottedText() = "점박이 수자"
    override fun styleSelectDottedTextDescription() = "수자와 거의 같소. 다만 빈 자리에 공백이 아닌 점을 표시하오."

    override fun focus() = "확대"

    override fun focusEmbedTitle() = "GomokuBot / 확대"
    override fun focusEmbedDescription() =
        "GomokuBot은 직관적인 립력을 돕기 위해 작은 크기의 \"단추판\"을 쓰오. GomokuBot이 어느 부분을 어떻게 확대할지 정하시오."

    override fun focusSelectIntelligence() = "지능적"
    override fun focusSelectIntelligenceDescription() =
        "GomokuBot 추론 기관으로 가장 적절한 자리를 분석해 확대하오."

    override fun focusSelectCenter() = "수동적"
    override fun focusSelectCenterDescription() =
        "언제나 마지막 수를 가운데에 두오."

    override fun hint() = "힌트"

    override fun hintEmbedTitle()= "GomokuBot / 힌트"
    override fun hintEmbedDescription() =
        "오목에는 승패를 가르는 중요한 자리가 있소. GomokuBot이 중요한 자리를 어떻게 강조할지 정하시오."

    override fun hintSelectFive() = "승리"
    override fun hintSelectFiveDescription() = "오목을 만들어 이길 수 있는 자리를 강조하오."

    override fun hintSelectOff() = "꺼짐"
    override fun hintSelectOffDescription() = "그 어떤 자리도 강조하지 않소."

    override fun mark() = "표시"

    override fun markEmbedTitle() = "GomokuBot / 표시"
    override fun markEmbedDescription() =
        "수많은 돌 사이에서 마지막으로 둔 자리를 기억하기는 쉬운 일이 아니오. GomokuBot이 마지막에 둔 돌을 어떻게 표시할지 정하시오."

    override fun markSelectLast() = "마지막 자리"
    override fun markSelectLastDescription() =
        "마지막에 둔 자리에 작은 점 하나를 찍소."

    override fun markSelectRecent() = "마지막 차례"
    override fun markSelectRecentDescription() =
        "상대가 마지막에 둔 자리에 작은 점을, 자신이 마지막에 둔 자리에 얇은 십자를 표시하오."

    override fun markSelectSequence() = "순서"
    override fun markSelectSequenceDescription() =
        "돌을 놓은 순서를 모두 표시하오."

    override fun swap() = "청소"

    override fun swapEmbedTitle() = "GomokuBot / 청소"
    override fun swapEmbedDescription() =
        "GomokuBot은 많은 통보문을 보내오. GomokuBot이 보낸 통보문을 어떻게 처리할지 정하시오."

    override fun swapSelectRelay() = "이어가기"
    override fun swapSelectRelayDescription() =
        "다음 수를 놓을 때 이전에 보낸 통보문을 모두 지우오."

    override fun swapSelectArchive() = "놓아두기"
    override fun swapSelectArchiveDescription() =
        "그 어떤 통보문도 지우지 않소."

    override fun swapSelectEdit() = "편집하기"
    override fun swapSelectEditDescription() =
        "처음 보낸 통보문을 편집하오."

    override fun archive() = "공유"

    override fun archiveEmbedTitle() = "GomokuBot / 공유"
    override fun archiveEmbedDescription() =
        "GomokuBot은 몇몇 멋진 놀음 결과를 GomokuBot 공식 채널에 공유하오. " +
                "물론 GomokuBot은 개인정보를 매우 중요하게 여기오. 놀음 결과를 어떻게 공유할지 정하시오."

    override fun archiveSelectByAnonymous() = "익명"
    override fun archiveSelectByAnonymousDescription() =
        "익명으로 놀음 결과를 공유하오."

    override fun archiveSelectWithProfile() = "기명"
    override fun archiveSelectWithProfileDescription() =
        "프로필 사진과 닉네임을 함께 붙여 놀음 결과를 공유하오."

    override fun archiveSelectPrivacy() = "비밀"
    override fun archiveSelectPrivacyDescription() =
        "그 어디에도 놀음 결과를 공유하지 않소."

    override fun sessionNotFound(): String =
        "진행 중인 놀음을 찾을 수 없소. 먼저 ``/시작`` 시킴말로 놀음을 시작하시오."

    override fun startCommand() = "시작"
    override fun startCommandDescription() = "새 놀음을 시작하오."
    override fun startCommandOptionOpponent() = "상대"
    override fun startCommandOptionOpponentDescription() = "함께 놀음을 시작할 인민을 지정해 주시오."
    override fun startCommandOptionRule() = "규칙"
    override fun startCommandOptionRuleDescription() = "새로 시작할 놀음의 규칙을 정하시오."

    override fun startErrorSessionAlready() =
        "이미 진행 중인 놀음이 있소. 먼저 진행 중인 놀음을 마무리하시오."
    override fun startErrorOpponentSessionAlready(opponent: String) =
        "$opponent 동지는 이미 다른 놀음을 진행 중이오. $opponent 동지의 놀음이 끝날 때까지 기다리시오."
    override fun startErrorRequestAlreadySent(opponent: String) =
        "$opponent 동지에게 보낸 놀음 요청이 아직 남아 있소. $opponent 동지의 대답을 기다리시오."
    override fun startErrorRequestAlready(opponent: String) =
        "$opponent 동지가 보낸 놀음 요청에 아직 대답하지 않았소. $opponent 동지의 놀음 요청에 먼저 대답하시오."
    override fun startErrorOpponentRequestAlready(opponent: String) =
        "$opponent 동지에게는 아직 대답하지 않은 다른 놀음 요청 하나가 남아 있소. $opponent 동지가 그 요청에 대답할 때까지 기다리시오."

    override fun setCommandDescription() = "원하는 자리표에 돌을 놓소."
    override fun setCommandOptionPosition() = "position"
    override fun setCommandOptionPositionDescription() = "a1부터 o15까지의 자리표"

    override fun setErrorIllegalArgument() =
        "잘못된 시킴말 형식이오. ``/s`` ``h8`` 꼴로 쓰시오."

    override fun setErrorExist(move: String) =
        "${move}에는 이미 돌이 놓여 있소. 다른 곳에 돌을 놓으시오."

    override fun setErrorForbidden(move: String, forbiddenKind: String) =
        "${move}은(는) ${forbiddenKind}금수요. 다른 곳에 돌을 놓으시오."

    override fun resignCommand() = "백기"
    override fun resignCommandDescription() = "진행 중인 놀음에서 백기를 들오."

    override fun boardCommand() = "판"
    override fun boardCommandDescription() = "지금 진행 중인 놀음을 새 통보문으로 펼치오."

    override fun requestEmbedTitle() = "오목 한 판 어떻소?"
    override fun requestEmbedDescription(requester: String, opponent: String) =
        "$requester 동지가 $opponent 동지에게 놀음 요청을 보냈소. 아래 단추를 눌러 대답하시오."
    override fun requestEmbedButtonAccept() = "수락"
    override fun requestEmbedButtonReject() = "숙청"

    override fun requestRejected(requester: String, opponent: String) =
        "$opponent 동지가 $requester 동지의 놀음 요청을 숙청했소."

    override fun requestExpired(requester: String, opponent: String) =
        "$requester 동지가 $opponent 동지에게 보낸 놀음 요청이 만료되었소. 아직도 $opponent 동지와 대결하고 싶다면 새 놀음 요청을 보내시오."

    override fun requestExpiredNewRequest() =
        "다시 제안하기"

    override fun beginPvp(players: ColorContainer<String>) =
        "${players.black} 동지와 ${players.white} 동지의 놀음이 시작되었소. ${players.black} 동지가 흑이오. ${players.black} 동지가 첫 번째 수를 놓으시오."

    override fun beginOpening(players: ColorContainer<String>) =
        "${players.black} 동지와 ${players.white} 동지의 오프닝 놀음이 시작되었소. ${players.black} 동지가 흑이오. ${players.white} 동지는 흑으로 맞바꿀지, 그대로 둘지 정하시오."

    override fun beginEngineBlack(player: String, gomokubot: String) =
        "$player 동지와 ${gomokubot}의 놀음이 시작되었소. $player 동지는 백이오. 전자계산기는 ``h8``에 두었소. 두 번째 수를 놓으시오."

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

    override fun gameResultDraw() =
        "더는 돌을 둘 곳이 없어 놀음은 무승부로 끝났소."

    override fun gameResultTimeout(winner: String, loser: String) =
        "$loser 동지가 제한 시간 안에 다음 수를 두지 않아 $winner 동지가 숙청됐소."

    override fun gameResultEngineRating() = "계급수"

    override fun gameResultEngineRatingChange() = "획득 계급수"

    override fun boardInProgress() = "진행 중"
    override fun boardInOpening() = "오프닝 중"
    override fun boardFinished() = "종료"

    override fun boardMoves() = "진행도"
    override fun boardLastMove() = "마지막 자리"

    override fun boardResult() = "결과"

    override fun boardWinDescription(winner: String) = "$winner 승리"
    override fun boardTieDescription() = "무승부"

    override fun boardCommandGuide() =
        ":mag: 단추를 누르거나 ``/s`` ``자리표`` 시킴말로 다음 수를 놓으시오."
    override fun boardSwapGuide() =
        ":arrows_counterclockwise: 단추를 눌러 흑과 백을 맞바꿀지 정하시오."
    override fun boardStatefulSwapGuide(offerCount: Int) =
        ":arrows_counterclockwise: 단추를 눌러 흑과 백을 맞바꿀지 정하시오. 흑이 내놓아야 할 5번째 수 후보는 ``$offerCount``개요."
    override fun boardBranchGuide() =
        ":paperclips: 단추를 눌러 흑과 백을 맞바꿀 기회를 얻을지, 5번째 수 후보 10개를 상대에게 제안할지 정하시오."
    override fun boardDeclareGuide() =
        ":paperclips: 선택 차림표에서 5번째 수 후보를 몇 개 고를지 정하시오."
    override fun boardSelectGuide() =
        ":dart: 단추를 누르거나 ``/s`` ``자리표`` 시킴말로 5번째 수를 고르시오."
    override fun boardOfferGuide(remainingMoves: Int) =
        ":question: 단추를 누르거나 ``/s`` ``position`` 시킴말로 5번째 수 후보 ${remainingMoves}개를 정하시오."

    override fun replayEmbedWin() = "승"
    override fun replayEmbedLose() = "패"
    override fun replayEmbedDraw() = "무"
    override fun replayEmbedMatchInfo(totalMoves: Int) = "총 ${totalMoves}수."
    override fun replayEmbedUnableToReplayDescription() = "빈 놀음이라 보여줄 것이 없소. 다른 놀음을 고르시오."

    override fun announceWrittenOn(date: String) = "$date 에 쓰여짐"

    override fun somethingWrongEmbedTitle() = "무언가 잘못되었소."

    override fun permissionNotGrantedEmbedDescription(channelName: String) =
        "GomokuBot은 $channelName 채널에 통보문을 보낼 권한이 없소. 역할과 권한 설정을 확인하시오."

    override fun permissionNotGrantedEmbedFooter() = "이 통보문은 1분 뒤 지워지오."

    override fun notYetImplementedEmbedDescription() = "이 기능은 아직 완성되지 않았소."

    override fun notYetImplementedEmbedFooter() =
        "지원 채널(https://discord.gg/vq8pkfF)에서 GomokuBot 판올림 소식을 받아볼 수 있소."

    override fun exploreAboutRenju() = "렌주가 무엇인지 모르시오? $UNICODE_RIGHT 를 눌러 렌주에 대해 알아보시오."

}
