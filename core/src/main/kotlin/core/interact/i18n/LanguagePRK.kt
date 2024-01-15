package core.interact.i18n

import core.assets.UNICODE_RIGHT

open class LanguagePRK : LanguageKOR() {

    override fun languageCode() = "PRK"

    override fun languageName() = "\uD83C\uDDF0\uD83C\uDDF5 조선말"
    override fun languageSuggestion() = "``/lang`` ``PRK`` 시킴말을 써 주시오."

    override fun aiLevelAmoeba() = "미제놈"
    override fun aiLevelApe() = "쪽바리"
    override fun aiLevelBeginner() = "로동자"
    override fun aiLevelIntermediate() = "로동당원"
    override fun aiLevelAdvanced() = "중앙당원"
    override fun aiLevelExpert() = "혁명가"
    override fun aiLevelGuru() = "령도자"

    override fun helpCommand() = "도움말"
    override fun helpCommandDescription() = "도움말을 알아보오."

    override fun settingsCommand() = "설정"
    override fun settingsCommandDescription() = "설정 화면을 표시하오."

    override fun helpAboutEmbedTitle() = "GomokuBot / 도움말"
    override fun helpAboutEmbedDescription(platform: String) =
        "**$platform**에서도 **오목**놀음을 즐겨보시오. **GomokuBot** 동지가 함께하오." +
                " ― GomokuBot은 ${platform}에서 오목([렌주](https://www.renju.net/rules/)) 놀음을 제공하는 오픈소스 봇이오. "
    override fun helpAboutEmbedDeveloper() = "개발자"
    override fun helpAboutEmbedRepository() = "Git 저장소"
    override fun helpAboutEmbedVersion() = "판올림"
    override fun helpAboutEmbedSupport() = "지원 당사"
    override fun helpAboutEmbedInvite() = "입당 신청서"

    override fun commandUsageEmbedTitle() = "GomokuBot / 시킴말"
    override fun commandUsageHelp() = "도움말을 알아보오."
    override fun commandUsageSettings() = "설정 화면을 표시하오."
    override fun commandUsageRankGlobal() = "1위부터 10위까지의 GomokuBot 전체 계급을 알아보오."
    override fun commandUsageRankServer() = "이 봉사기 안에서의 계급를 알아보오."
    override fun commandUsageRankUser() = "동지의 계급를 알아보오."
    override fun commandUsageRating() = "``GomokuBot ELO`` 계급수를 알아보오."

    override fun commandUsageLang(langList: String) =
        "이 봉사기에서 쓰이는 언어 설정을 바꾸오. Ex) ``/lang`` ``ENG``"
    override fun commandUsageStyle() =
        "이 봉사기에서 쓰이는 오목판 모양을 바꾸오. Ex) ``/스타일`` ``A``"

    override fun commandUsageStartPVE() = "전자계산기과 함께 새 놀음을 시작하오."
    override fun commandUsageStartPVP() =
        "동지에게 새 놀음을 제안하오. Ex) ``/시작`` ``@인민``"
    override fun commandUsageResign() = "지속 중인 놀음을 포기하오."

    override fun rankCommand() = "계급"
    override fun rankCommandDescription() = "1위부터 10위까지의 계급를 알아보오."
    override fun rankCommandSubGlobal() = "전체"
    override fun rankCommandSubGlobalDescription() = "GomokuBot 전체 계급을 알아보오."
    override fun rankCommandSubServer() = "봉사기"
    override fun rankCommandSubServerDescription() = "봉사기 내부 계급를 알아보오."
    override fun rankCommandSubUser() = "인민"
    override fun rankCommandSubUserDescription() = "동무-상대 계급를 알아보오."
    override fun rankCommandOptionPlayer() = "인민"
    override fun rankCommandOptionPlayerDescription() = "상대 계급를 알아볼 인민을 지정해 주시오."

    override fun rankErrorNotFound() = "인민을 찾을 수 없소. GomokuBot 쌈박질 기록이 있는 인민을 지정해 주시오."

    override fun rankEmbedTitle() = "GomokuBot / 계급"
    override fun rankEmbedDescription() = "1위부터 10위까지의 승리 계급를 확인 해보시오."
    override fun rankEmbedWin() = "승"
    override fun rankEmbedLose() = "패"
    override fun rankEmbedDraw() = "무"

    override fun ratingCommand() = "계급수"
    override fun ratingCommandDescription() = "계급수를 알아보오."
    override fun ratingCommandOptionUser() = "인민"
    override fun ratingCommandOptionUserDescription() = "계급수를 알아볼 인민을 지정해 주시오."

    override fun ratingEmbed() = TODO("Not yet implemented")
    override fun ratingEmbedDescription() = TODO("Not yet implemented")

    override fun languageUpdated() = "언어 설정이 조선말:flag_kp:로 바뀌었소. 공화국에 온 걸 환영하오."

    override fun styleCommand() = "생김새"
    override fun styleCommandDescription() = "이 봉사기에서 쓰이는 오목판 생김새를 바꾸오."
    override fun styleCommandOptionCode() = "생김새"
    override fun styleCommandOptionCodeDescription() = "생김새 꼴을 정하시오."

    override fun styleEmbedTitle() = "GomokuBot / 생김새"
    override fun styleEmbedDescription() =
        "이 봉사기에 적용된 기본 오목판 스타일(``스타일 A``)이 제대로 보이지 않을 수 있습니다." +
                " 준비된 네 가지 스타일 중 마음에 드는 스타일 하나를 선택해 주세요."
    override fun styleEmbedSuggestion(styleName: String) = "이 스타일을 사용하려면 ``/스타일`` $styleName 시킴말를 해 주세요."

    override fun styleErrorNotfound() = "스타일 지정이 잘못됐습니다. ``/스타일`` ``스타일 코드`` 형식으로 해 주세요."

    override fun styleUpdated(styleName: String) = "스타일 설정이 스타일 ``${styleName}``로 바뀌었습니다."

    override fun settingApplied(kind: String, choice: String) = "$kind 설정이 ${choice}로 바뀌었습니다."

    override fun style() = "생김새"

    override fun styleSelectImage() = "사진"
    override fun styleSelectImageDescription() =
        "오목 판을 이미지로 표시합니다. 플랫폼 봉사기 상태에 따라 약간의 지연이 생길 수 있습니다."

    override fun styleSelectText() = "수자"
    override fun styleSelectTextDescription() = "오목 판을 텍스트로 표시합니다. 가장 단순하지만 가장 빠릅니다."

    override fun styleSelectDottedText() = "점박이 수자"
    override fun styleSelectDottedTextDescription() = "텍스트와 거의 같습니다. 다만 빈 자리에 공백이 아닌 점을 표시합니다."

    override fun styleSelectUnicodeText() = "통용수자"
    override fun styleSelectUnicodeTextDescription() =
        "오목 판을 유니코드 이모지로 표시합니다. 폰트 설정에 따라 완전히 망가져 보일 수 있습니다."

    override fun focus() = "확대"

    override fun focusEmbedTitle() = "GomokuBot / 확대"
    override fun focusEmbedDescription() =
        "GomokuBot은 직관적인 립력을 돕기 위해 작은 크기의 \"단추 판\"을 사용합니다. GomokuBot이 어떤 부분을 어떻게 확대할지 정하시오."

    override fun focusSelectIntelligence() = "지능적"
    override fun focusSelectIntelligenceDescription() =
        "GomokuBot 추론 엔진으로 가장 적절한 위치를 분석해 확대하오."

    override fun focusSelectFallowing() = "수동적"
    override fun focusSelectFallowingDescription() =
        "항상 마지막 수를 가운데 둡니다."

    override fun hint() = "힌트"

    override fun hintEmbedTitle()= "GomokuBot / 힌트"
    override fun hintEmbedDescription() =
        "오목에는 승패를 가르는 중요한 자리가 있습니다. GomokuBot이 중요한 자리를 어떻게 강조할지 정해주세요."

    override fun hintSelectFive() = "승리"
    override fun hintSelectFiveDescription() = "오목을 만들어 이길 수 있는 자리를 강조합니다."

    override fun hintSelectOff() = "꺼짐"
    override fun hintSelectOffDescription() = "그 어떤 자리도 강조하지 않습니다."

    override fun mark() = "표시"

    override fun markEmbedTitle() = "Gomokubot / 표시"
    override fun markEmbedDescription() =
        "수많은 돌 사이에서 마지막으로 둔 위치를 기억하기는 쉬운 일이 아닙니다. GomokuBot이 마지막에 둔 돌을 어떻게 표시할지 정해주세요."

    override fun markSelectLast() = "마지막 위치"
    override fun markSelectLastDescription() =
        "마지막에 둔 위치에 작은 점 하나를 찍습니다."

    override fun markSelectRecent() = "마지막 차례"
    override fun markSelectRecentDescription() =
        "상대가 마지막에 둔 위치에 작은 점을, 자신이 마지막에 둔 위치에 얇은 십자를 표시합니다."

    override fun markSelectSequence() = "순서"
    override fun markSelectSequenceDescription() =
        "돌을 놓은 순서를 모두 표시합니다."

    override fun swap() = "청소"

    override fun swapEmbedTitle() = "GomokuBot / 청소"
    override fun swapEmbedDescription() =
        "GomokuBot은 정말 많은 양의 메시지를 보냅니다. GomokuBot이 보낸 메시지를 어떻게 처리할지 정해주세요."

    override fun swapSelectRelay() = "이어가기"
    override fun swapSelectRelayDescription() =
        "다음 수를 놓을 때 이전에 보낸 메시지를 모두 삭제합니다."

    override fun swapSelectArchive() = "놓아두기"
    override fun swapSelectArchiveDescription() =
        "그 어떤 메시지도 삭제하지 않습니다."

    override fun swapSelectEdit() = "편집하기"
    override fun swapSelectEditDescription() =
        "처음 보낸 메시지를 편집합니다."

    override fun archive() = "공유"

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

    override fun sessionNotFound(): String =
        "지속 중인 놀음이 없소. 먼저 ``/시작`` 시킴말로 놀음을 시작하시오."

    override fun startCommand() = "시작"
    override fun startCommandDescription() = "새 놀음을 시작하오."
    override fun startCommandOptionOpponent() = "상대"
    override fun startCommandOptionOpponentDescription() = "덤빌 인민을 정하시오."

    override fun startErrorSessionAlready() =
        "지속 중인 놀음이 있소. 먼저 끝내시오."
    override fun startErrorOpponentSessionAlready(opponent: String) =
        "$opponent 동지는 이미 다른 놀음을 지속하고 있소. $opponent 동지의 놀음이 끝날 때까지 기다리시오."
    override fun startErrorRequestAlreadySent(opponent: String) =
        "$opponent 동지에게 보낸 놀음 요청이 아직 남아있소. $opponent 동지의 대답을 기다리시오."
    override fun startErrorRequestAlready(opponent: String) =
        "$opponent 동지가 보낸 놀음 요청이 남아있소. $opponent 동지의 놀음 요청에 먼저 대답하시오."
    override fun startErrorOpponentRequestAlready(opponent: String) =
        "$opponent 동지에게는 아직 대답하지 않은 다른 놀음 요청 하나가 남아있소. $opponent 동지를 기다리시오."

    override fun setCommandDescription() = "원하는 자리표에 돌을 놓소."
    override fun setCommandOptionColumn() = "x"
    override fun setCommandOptionColumnDescription() = "미제말"
    override fun setCommandOptionRow() = "y"
    override fun setCommandOptionRowDescription() = "숫자"

    override fun setErrorIllegalArgument() =
        "잘못된 시킴말 입구형식이오. ``/s`` ``미제말`` ``숫자`` 꼴로 입구형식을 지키시오."

    override fun setErrorExist(move: String) =
        "${move}에는 이미 돌이 놓여있소. 다른 곳에 놓으시오."

    override fun setErrorForbidden(move: String, forbiddenKind: String) =
        "${move}은(는) ${forbiddenKind}금수요. 다른 곳에 놓으시오."

    override fun resignCommand() = "백기"
    override fun resignCommandDescription() = "지속중인 놀음을 포기하오."

    override fun requestEmbedTitle() = "오목 한 판 괜찮겠습니까?"
    override fun requestEmbedDescription(owner: String, opponent: String) =
        "$owner 동지가 $opponent 동지에게 놀음 요청을 보냈소. 아래 단추을 눌러 대답하시오."
    override fun requestEmbedButtonAccept() = "평양"
    override fun requestEmbedButtonReject() = "아오지"

    override fun requestRejected(owner: String, opponent: String) =
        "$opponent 동지가 $owner 동지의 대결 요청을 숙청했소."

    override fun requestExpired(owner: String, opponent: String) =
        "$owner 동지가 $opponent 동지에게 보낸 놀음 요청은 숙청되었소. 아직도 $opponent 동지와 대결하고 싶다면, 새 놀음 요청을 보내시오."

    override fun requestExpiredNewRequest() = "다시 제안하기"

    override fun beginPVP(blackPlayer: String, whitePlayer: String) =
        "$blackPlayer 동지와 $whitePlayer 동지의 놀음이 시작되었소. $blackPlayer 동지가 흑이요. $blackPlayer 동지가 첫 번째 수를 놓으시오."

    override fun beginPVEAiBlack(player: String) =
        "$player 동지와 전자계산기의 놀음이 시작되었소. $player 동지는 백이요. 전자계산기는 ``h8``에 두었소. 두 번째 수를 놓으시오."

    override fun beginPVEAiWhite(player: String) =
        "$player 동지와 전자계산기의 놀음이 시작되었소. $player 동지가 흑이요. 첫 번째 수를 놓으시오."

    override fun processNextPVE(lastMove: String) =
        "다음 수를 놓으시오. 전자계산기는 ${lastMove}에 놓았소."

    override fun processNextPVP(priorPlayer: String, lastMove: String) =
        "다음 수를 놓으시오. $priorPlayer 동지는 ${lastMove}에 놓았소."

    override fun processErrorOrder(player: String) =
        "이번은 $player 동지의 차례요. $player 동지가 다음 수를 놓을 때까지 기다리시오."

    override fun endPVPWin(winner: String, loser: String, lastMove: String) =
        "$winner 동지는 ${lastMove}에 돌을 놓음으로써 $loser 동지를 죽탕쳤소."
    override fun endPVPResign(winner: String, loser: String) =
        "$loser 동지가 백기을 선언 함으로써 $winner 동지가 $loser 동지를 죽탕쳤소."
    override fun endPVPTie(owner: String, opponent: String) =
        "이제 더 이상 돌을 놓을 공간이 없으므로, $opponent 동지와 $opponent 동지의 놀음은 무승부로 끝났소."
    override fun endPVPTimeOut(winner: String, loser: String) =
        "$loser 동지는 오랜 시간 동안 다음 수를 두지 않았기 때문에 $winner 동지는 $loser 동지를 견뎠소."

    override fun endPVEWin(player: String, lastPos: String) =
        "$player 동지는 $lastPos 에 돌을 놓음으로써 전자계산기을 견뎠소."
    override fun endPVELose(player: String, lastPos: String) =
        "전자계산기가 $lastPos 에 돌을 놓음으로써 전자계산기가 $player 동지를 죽탕쳤소."
    override fun endPVEResign(player: String) =
        "$player 동지는 전자계산기 상대로 백기을 선언 함으로써 전자계산기에 죽탕당했소."
    override fun endPVETie(player: String) =
        "이제 더 이상 돌을 놓을 공간이 없으므로, $player 동지와 전자계산기의 놀음은 무승부로 끝났소."
    override fun endPVETimeOut(player: String) =
        "$player 동지는 오랜 시간 동안 다음 수를 두지 않았기 때문에 전자계산기에 죽탕당했소."

    override fun boardInProgress() = "진행 중"
    override fun boardFinished() = "종료"

    override fun boardMoves() = "진행도"
    override fun boardLastMove() = "마지막 위치"

    override fun boardResult() = "결과"

    override fun boardWinDescription(winner: String) = "$winner 승리"
    override fun boardTieDescription() = "무승부"

    override fun boardCommandGuide() = ":mag: 단추를 누르거나 ``/s`` ``미제말`` ``숫자`` 시킴말을 똑바로 맞춰 원하는 자리표에 놓으시오."

    override fun announceWrittenOn(date: String) = "$date 에 쓰여짐"

    override fun somethingWrongEmbedTitle() = "무언가 잘못되었소."

    override fun permissionNotGrantedEmbedDescription(channelName: String) =
        "Gokomubot은 $channelName 채널에 메시지를 보낼 권한이 없습니다! 역할 및 퍼미션 설정을 확인해 주세요."

    override fun permissionNotGrantedEmbedFooter() = "이 메시지는 1분 뒤 지워집니다."

    override fun notYetImplementedEmbedDescription() = "이 기능은 아직 완성되지 않았습니다."

    override fun notYetImplementedEmbedFooter() =
        "지원 채널(https://discord.gg/vq8pkfF)에서 Gomokubot 업데이트 소식을 받아볼 수 있습니다."

    override fun exploreAboutRenju() = "렌주가 무엇인지 모르시오? $UNICODE_RIGHT 를 눌러 렌주에 대해 알아보시오. 남조선에서 만든 자료가 있소."

}
