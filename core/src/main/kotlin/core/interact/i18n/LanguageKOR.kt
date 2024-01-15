package core.interact.i18n

import core.assets.UNICODE_RIGHT

open class LanguageKOR : LanguageENG() {

    override fun languageCode() = "KOR"

    override fun languageName() = "\uD83C\uDDF0\uD83C\uDDF7 한국어"
    override fun languageSuggestion() = "``/lang`` ``KOR`` 명령어를 사용해주세요."

    override fun aiLevelAmoeba() = "아메바"
    override fun aiLevelApe() = "유인원"
    override fun aiLevelBeginner() = "초심자"
    override fun aiLevelIntermediate() = "중급자"
    override fun aiLevelAdvanced() = "숙련자"
    override fun aiLevelExpert() = "전문가"
    override fun aiLevelGuru() = "현자"

    override fun helpCommand() = "도움말"
    override fun helpCommandDescription() = "도움말을 알아봅니다."
    override fun helpCommandOptionShortcut() = "바로가기"
    override fun helpCommandOptionShortcutDescription() = "원하는 도움말 페이지를 바로 표시합니다."
    override fun helpCommandOptionAnnouncements() = "공지"

    override fun settingsCommand() = "설정"
    override fun settingsCommandDescription() = "설정 화면을 표시합니다."

    override fun helpAboutEmbedTitle() = "GomokuBot / 도움말"
    override fun helpAboutEmbedDescription(platform: String) =
        "이제 **$platform**에서도 **오목**을 즐겨 보세요. **GomokuBot**이 함께 하겠습니다." +
                " ― GomokuBot은 ${platform}에서 오목([렌주](https://www.renju.net/rules/)) 기능을 제공하는 오픈소스 인공지능 오목 봇입니다. " +
                "수집된 기보 데이터는 강화학습 인공지능 훈련에 사용됩니다."
    override fun helpAboutEmbedDeveloper() = "개발자"
    override fun helpAboutEmbedRepository() = "Git 저장소"
    override fun helpAboutEmbedVersion() = "버전"
    override fun helpAboutEmbedSupport() = "지원 채널"
    override fun helpAboutEmbedInvite() = "초대 링크"

    override fun commandUsageEmbedTitle() = "GomokuBot / 명령어"
    override fun commandUsageHelp() = "도움말을 알아봅니다."
    override fun commandUsageSettings() = "설정 화면을 표시합니다."
    override fun commandUsageRankGlobal() = "1위부터 10위까지의 GomokuBot 전체 순위를 알아봅니다."
    override fun commandUsageRankServer() = "이 서버 안에서의 순위를 알아봅니다."
    override fun commandUsageRankUser() = "멘션 된 유저 상대의 순위를 알아봅니다."
    override fun commandUsageRating() = "``GomokuBot ELO`` 레이팅을 알아봅니다."

    override fun commandUsageLang(langList: String) =
        "이 서버에서 쓰이는 언어 설정을 바꿉니다. Ex) ``/lang`` ``ENG``"
    override fun commandUsageStyle() =
        "이 서버에서 쓰이는 오목판 모양을 바꿉니다. Ex) ``/스타일`` ``A``"

    override fun commandUsageStartPVE() = "인공지능과 함께 새 게임을 시작합니다."
    override fun commandUsageStartPVP() =
        "멘션 된 유저에게 새 게임을 제안합니다. Ex) ``/시작`` ``@유저``"
    override fun commandUsageResign() = "진행 중인 게임을 포기합니다."

    override fun rankCommand() = "순위"
    override fun rankCommandDescription() = "1위부터 10위까지의 순위를 알아봅니다."
    override fun rankCommandSubGlobal() = "전체"
    override fun rankCommandSubGlobalDescription() = "GomokuBot 전체 순위을 알아봅니다."
    override fun rankCommandSubServer() = "서버"
    override fun rankCommandSubServerDescription() = "서버 내부 순위를 알아봅니다."
    override fun rankCommandSubUser() = "유저"
    override fun rankCommandSubUserDescription() = "유저-상대 순위를 알아봅니다."
    override fun rankCommandOptionPlayer() = "유저"
    override fun rankCommandOptionPlayerDescription() = "상대 순위를 알아볼 유저를 지정해 주세요."

    override fun rankErrorNotFound() = "유저 기록을 찾을 수 없습니다. GomokuBot PvP 플레이 기록이 있는 유저를 지정해 주세요."

    override fun rankEmbedTitle() = "GomokuBot / 순위"
    override fun rankEmbedDescription() = "1위부터 10위까지의 승리 순위를 확인 해보세요."
    override fun rankEmbedWin() = "승"
    override fun rankEmbedLose() = "패"
    override fun rankEmbedDraw() = "무"

    override fun ratingCommand() = "레이팅"
    override fun ratingCommandDescription() = "레이팅을 알아봅니다."
    override fun ratingCommandOptionUser() = "유저"
    override fun ratingCommandOptionUserDescription() = "레이팅을 알아볼 유저를 지정해 주세요."

    override fun ratingEmbed() = TODO("Not yet implemented")
    override fun ratingEmbedDescription() = TODO("Not yet implemented")

    override fun languageUpdated() = "언어 설정이 한국어:flag_kr:로 바뀌었습니다!"

    override fun styleCommand() = "스타일"
    override fun styleCommandDescription() = "이 서버에서 쓰이는 오목판 스타일을 바꿉니다."
    override fun styleCommandOptionCode() = "스타일"
    override fun styleCommandOptionCodeDescription() = "스타일 코드를 지정해 주세요."

    override fun styleEmbedTitle() = "GomokuBot / 스타일"
    override fun styleEmbedDescription() =
        "이 서버에 적용된 기본 오목판 스타일(``스타일 A``)이 제대로 보이지 않을 수 있습니다." +
                " 준비된 네 가지 스타일 중 마음에 드는 스타일 하나를 선택해 주세요."
    override fun styleEmbedSuggestion(styleName: String) = "이 스타일을 사용하려면 ``/스타일`` $styleName 명령어를 입력해 주세요."

    override fun styleErrorNotfound() = "스타일 지정이 잘못됐습니다. ``/스타일`` ``스타일 코드`` 형식으로 입력해 주세요."

    override fun styleUpdated(styleName: String) = "스타일 설정이 스타일 ``${styleName}``로 바뀌었습니다."

    override fun settingApplied(kind: String, choice: String) = "$kind 설정이 ${choice}로 바뀌었습니다."

    override fun style() = "스타일"

    override fun styleSelectImage() = "이미지"
    override fun styleSelectImageDescription() =
        "오목 판을 이미지로 표시합니다. 플랫폼 서버 상태에 따라 약간의 지연이 생길 수 있습니다."

    override fun styleSelectText() = "텍스트"
    override fun styleSelectTextDescription() = "오목 판을 텍스트로 표시합니다. 가장 단순하지만 가장 빠릅니다."

    override fun styleSelectDottedText() = "점박이 텍스트"
    override fun styleSelectDottedTextDescription() = "텍스트와 거의 같습니다. 다만 빈 자리에 공백이 아닌 점을 표시합니다."

    override fun styleSelectUnicodeText() = "유니코드"
    override fun styleSelectUnicodeTextDescription() =
        "오목 판을 유니코드 이모지로 표시합니다. 폰트 설정에 따라 완전히 망가져 보일 수 있습니다."

    override fun focus() = "확대"

    override fun focusEmbedTitle() = "GomokuBot / 확대"
    override fun focusEmbedDescription() =
        "GomokuBot은 직관적인 입력을 돕기 위해 작은 크기의 \"버튼 판\"을 사용합니다. GomokuBot이 어떤 부분을 어떻게 확대할지 정해주세요."

    override fun focusSelectIntelligence() = "지능적"
    override fun focusSelectIntelligenceDescription() =
        "GomokuBot 추론 엔진으로 가장 적절한 위치를 분석해 확대합니다."

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

    override fun sessionNotFound(): String =
        "진행 중인 게임을 찾을 수 없습니다. 먼저 ``/시작`` 명령어로 게임을 시작해 주세요."

    override fun startCommand() = "시작"
    override fun startCommandDescription() = "새 게임을 시작합니다."
    override fun startCommandOptionOpponent() = "상대"
    override fun startCommandOptionOpponentDescription() = "함께 게임을 시작할 유저를 지정해 주세요."
    override fun startCommandOptionRule() = "규칙"
    override fun startCommandOptionRuleDescription() = "새로 시작할 게임의 규칙을 정해주세요."

    override fun startErrorSessionAlready() =
        "이미 진행 중인 게임이 있습니다. 진행 중인 게임을 먼저 마무리해 주세요."
    override fun startErrorOpponentSessionAlready(opponent: String) =
        "$opponent 님은 이미 다른 게임을 진행 중 입니다. $opponent 님이 진행 중인 게임이 끝날 때까지 기다려 주세요."
    override fun startErrorRequestAlreadySent(opponent: String) =
        "$opponent 님에게 보낸 대결 요청이 아직 남아 있습니다. $opponent 님의 응답을 기다려 주세요."
    override fun startErrorRequestAlready(opponent: String) =
        "$opponent 님이 보낸 대결 요청에 아직 응답하지 않았습니다. $opponent 님의 대결 요청에 먼저 응답해 주세요."
    override fun startErrorOpponentRequestAlready(opponent: String) =
        "$opponent 님에게는 아직 응답하지 않은 다른 대결 요청 하나가 남아 있습니다. $opponent 님이 다른 대결 요청에 응답할 때까지 기다려 주세요."

    override fun setCommandDescription() = "원하는 좌표에 돌을 놓습니다."
    override fun setCommandOptionColumn() = "x"
    override fun setCommandOptionColumnDescription() = "알파벳"
    override fun setCommandOptionRow() = "y"
    override fun setCommandOptionRowDescription() = "숫자"

    override fun setErrorIllegalArgument() =
        "잘못된 명령어 형식입니다. ``/s`` ``알파벳`` ``숫자`` 꼴로 입력해 주세요."

    override fun setErrorExist(move: String) =
        "${move}에는 이미 돌이 놓여 있습니다. 다른 곳에 돌을 놓아주세요."

    override fun setErrorForbidden(move: String, forbiddenKind: String) =
        "${move}은(는) ${forbiddenKind}금수 입니다. 다른 곳에 돌을 놓아주세요."

    override fun resignCommand() = "항복"
    override fun resignCommandDescription() = "진행중인 게임을 포기합니다."

    override fun requestEmbedTitle() = "오목 한 판 괜찮겠습니까?"
    override fun requestEmbedDescription(owner: String, opponent: String) =
        "$owner 님이 $opponent 님에게 대결 요청을 보냈습니다. 아래 버튼을 눌러 대답해 주세요."
    override fun requestEmbedButtonAccept() = "수락"
    override fun requestEmbedButtonReject() = "거절"

    override fun requestRejected(owner: String, opponent: String) =
        "$opponent 님이 $owner 님의 대결 요청을 거절했습니다."

    override fun requestExpired(owner: String, opponent: String) =
        "$owner 님이 $opponent 님에게 보낸 대결 요청이 만료되었습니다. 아직도 $opponent 님과 대결하고 싶다면, 새 대결 요청을 보내주세요."

    override fun requestExpiredNewRequest() = "다시 제안하기"

    override fun beginPVP(blackPlayer: String, whitePlayer: String) =
        "$blackPlayer 님과 $whitePlayer 님의 게임이 시작되었습니다! $blackPlayer 님이 흑입니다. $blackPlayer 님이 첫 번째 수를 놓아주세요."

    override fun beginOpening(blackPlayer: String, whitePlayer: String) =
        "$blackPlayer 님과 $whitePlayer 님의 오프닝 게임이 시작되었습니다! $blackPlayer 님이 흑입니다. $whitePlayer 님은 흑으로 스왑할지, 그대로 플레이 할지 정해주세요."

    override fun beginPVEAiBlack(player: String) =
        "$player 님과 인공지능의 게임이 시작되었습니다! $player 님은 백입니다. 인공지능은 ``h8``에 두었습니다. 두 번째 수를 놓아주세요."

    override fun beginPVEAiWhite(player: String) =
        "$player 님과 인공지능의 게임이 시작되었습니다! $player 님이 흑입니다. 첫 번째 수를 놓아주세요."

    override fun processNextPVE(lastMove: String) =
        "다음 수를 놓아주세요. AI는 ${lastMove}에 놓았습니다."

    override fun processNextPVP(priorPlayer: String, lastMove: String) =
        "다음 수를 놓아주세요. $priorPlayer 님은 ${lastMove}에 놓았습니다."

    override fun processErrorOrder(player: String) =
        "지금은 $player 님의 차례입니다. $player 님이 다음 수를 놓을 때까지 기다려 주세요."

    override fun endPVPWin(winner: String, loser: String, lastMove: String) =
        "$winner 님이 ${lastMove}에 돌을 놓음으로써 $loser 님을 이겼습니다."
    override fun endPVPResign(winner: String, loser: String) =
        "$loser 님이 항복을 선언 함으로써 $winner 님이 이겼습니다."
    override fun endPVPTie(owner: String, opponent: String) =
        "이제 더 이상 돌을 놓을 공간이 없으므로, $opponent 님과 $opponent 님의 게임은 무승부로 끝났습니다."
    override fun endPVPTimeOut(winner: String, loser: String) =
        "$loser 님이 오랜 시간 동안 다음 수를 두지 않았기 때문에 $winner 님이 $loser 님을 이겼습니다."

    override fun endPVEWin(player: String, lastPos: String) =
        "$lastPos 에 돌을 놓음으로써 인공지능을 이겼습니다. 축하합니다, $player 님."
    override fun endPVELose(player: String, lastPos: String) =
        "$player 님, 인공지능이 $lastPos 에 돌을 놓음으로써 인공지능에 패배했습니다. 언제든지 다시 도전해 주세요."
    override fun endPVEResign(player: String) =
        "$player 님, 인공지능 상대로 항복을 선언 함으로써 인공지능에 패배하셨습니다. 언제든지 다시 도전해 주세요."
    override fun endPVETie(player: String) =
        "이제 더 이상 돌을 놓을 공간이 없으므로, $player 님과 인공지능의 게임은 무승부로 끝났습니다."
    override fun endPVETimeOut(player: String) =
        "$player 님, 오랜 시간 동안 다음 수를 두지 않았기 때문에 인공지능에 패배했습니다."

    override fun boardInProgress() = "진행 중"
    override fun boardInOpening() = "오프닝 중"
    override fun boardFinished() = "종료"

    override fun boardMoves() = "진행도"
    override fun boardLastMove() = "마지막 위치"

    override fun boardResult() = "결과"

    override fun boardWinDescription(winner: String) = "$winner 승리"
    override fun boardTieDescription() = "무승부"

    override fun boardCommandGuide() = ":mag: 버튼을 누르거나 ``/s`` ``알파벳`` ``숫자`` 명령어를 입력해 다음 수를 놓아주세요."
    override fun boardSwapGuide() =
        ":arrows_counterclockwise: 버튼을 눌러 흑과 백을 바꿀지 선택해주세요."
    override fun boardBranchGuide() =
        ":paperclips: 버튼을 눌러 흑과 백을 바꿀 기회를 얻을지, 5번째 수 후보 10개를 상대에게 제안할지 선택해주세요."
    override fun boardSelectGuide() =
        ":dart: 버튼을 누르거나 ``/s`` ``알파벳`` ``숫자`` 명령어를 입력해 5번째 수를 선택해주세요."
    override fun boardOfferGuide(remainingMoves: Int) =
        ":question: 버튼을 누르거나 ``/s`` ``알파벳`` ``숫자`` 명령어를 입력해 5번째 수 후보 ${remainingMoves}개를 정해주세요."

    override fun announceWrittenOn(date: String) = "$date 에 쓰여짐"

    override fun somethingWrongEmbedTitle() = "뭔가 잘못됐습니다!"

    override fun permissionNotGrantedEmbedDescription(channelName: String) =
        "Gokomubot은 $channelName 채널에 메시지를 보낼 권한이 없습니다! 역할 및 퍼미션 설정을 확인해 주세요."

    override fun permissionNotGrantedEmbedFooter() = "이 메시지는 1분 뒤 지워집니다."

    override fun notYetImplementedEmbedDescription() = "이 기능은 아직 완성되지 않았습니다."

    override fun notYetImplementedEmbedFooter() =
        "지원 채널(https://discord.gg/vq8pkfF)에서 Gomokubot 업데이트 소식을 받아볼 수 있습니다."

    override fun exploreAboutRenju() = "렌주가 무엇인지 모르시나요? $UNICODE_RIGHT 를 눌러 렌주에 대해 알아보세요."

    override fun aboutRenjuDocument() = """
## 렌주란 무엇인가요? {#렌주에-관하여}

Q. *오목봇에 렌주라니, 이게 무슨 말인가요?* 

A. 오목은 매우 단순합니다. 하지만 그만큼 한계 역시 명확합니다. 그러므로 GomokuBot은 단순한 오목이 아닌, 아주 간단한 규칙 몇 개가 추가된 렌주를 사용합니다. 

하지만 걱정하지 마세요. 렌주는 오목과 정말로 비슷합니다. 초심자들 사이의 승부에서는 렌주가 무엇인지 모르더라도 아무 영향이 없을 정도로 똑같습니다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/intro.png)

*흑의 승리입니다!*

렌주는 오목에서 흑에만 적용되는 **금수** 규칙을 추가한 변형 게임입니다. 렌주와 금수가 무엇인지 알아보기 전에, 왜 금수를 흑에게만 적용하는지, 단순한 오목이 얼마나 불공평한 게임인지 알아볼 필요가 있습니다.

### 오목은 풀린 게임입니다.

오목에 어느정도 능숙하다면, 다른 제약이 없는 오목에서는 먼저 두기 시작하는 흑이 매우 유리하다는 것을 어느정도 파악하고 있었을 것입니다. 그렇다면 흑이 어느 정도로 유리한 것일까요? 흑과 백 모두 최선의 수를 둔다면 어떤 결과가 나타나게 되는 것일까요?

아무런 추가 규칙이 없는 단순한 오목은 1980년 Stefan Reisch에 의해 흑과 백 모두 최선의 수를 둔다고 해도 흑은 항상 필승 전략을 찾아낼 수 있음이 증명됐습니다.[*](https://doi.org/10.1007/bf00288536)

즉, 단순한 오목에서는 흑과 백 모두 최선의 수를 둔다고 해도 흑이 **항상** 승리하게 됩니다. 두 플레이어의 수준이 높아질수록 동전 던지기에 가까워지는 셈입니다. 동전 던지기에서 벗어나기 위해서는 흑 선공의 압도적 유리함을 해결할 수 있는 특별한 규칙이 꼭 필요합니다.

### 렌주에는 "금수"가 있습니다.

렌주는 흑 선공의 압도적 유리함을 해결하기 위해 **금수** 규칙을 선택했습니다. 금수는 특별한 조건을 만족하는 자리에 흑이 돌을 두지 못 하는 규칙으로, 3-3 금지와 4-4 금지, 그리고 6목 금지까지 총 3가지의 금지 규칙이 있습니다.

복잡해 보인다고 해도 걱정할 필요 없습니다. 초심자들의 게임에서 금수는 꽤 드물게 등장하며, 금수가 무엇인지 모른다고 해도 금수로 승부가 뒤집히는 일은 적을 것입니다.

금수를 정확히 이해하기 위해서는, 오목에서 3과 4를 어떻게 정의하고 있는지부터, 어떤 상황에서 금수가 성립하는지를 이해해야만 합니다. 여기서는 4의 정의부터 시작합니다.

## 4에 대해 알아봅시다. {#4란}

4는 공백 하나를 포함한 채 일렬로 배열된 돌 네 개로, **한 수 더 두어 승리할 수 있는 모양**을 뜻합니다. 한 수 더 두어 승리할 수 있기에, 내가 4를 가지고 있지 않을 때 상대가 4를 만들었다면 곧바로 대응해야 하는 강력한 모양입니다.

일직선으로 배열된 돌 네 개는 4입니다. 한쪽이 막힌 돌 네 개도 4입니다. 한 칸 떨어진 채 배열된 돌 네 개 역시 4입니다. 한 수를 더 두어 5를 만들 수 있다면 모두 4입니다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/four.png)

*한 수를 더 둔다면...*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/four-expanded.png)
*...이길 수 있습니다!*

## 4-4 금수에 대해 알아봅시다. {#44금수}

4-4 금수란 **한 수를 두어 4를 두 개 이상 만들 수 있는 자리**를 뜻합니다. 한 수를 두어 4를 세 개 만들 수 있더라도 4-4 금수입니다.

기억해 주세요: 금수는 흑의 압도적 유리함을 해결하기 위해 만들어진 규칙입니다. 따라서 모든 금수는 흑에게만 적용됩니다. 백은 자유롭게 4-4를 만들어 승리할 수 있습니다!

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/double-four-forbid.png)

*GomokuBot은 금수를 빨간 점으로 표시합니다. 흑은 빨간 점에 돌을 놓을 수 없지만 백은 놓을 수 있습니다.*

흔한 상황은 아니지만, 같은 줄에서 4-4금수가 한 개 이상 등장할 수 있습니다. 같은 줄 이라도 한 수를 두어 4를 두 개 이상 만들 수 있다면 4-4금수입니다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/double-four-forbid-in-a-line.png)

## 3에 대해 알아봅시다. {#33금수}

3은 조금 특별합니다. 오목의 직접적인 승리 조건과는 조금 동떨어져 있기 때문입니다. 오목에서는 3을 **한 수를 더 두어 열린 4를 만들 수 있는 모양**으로 정의합니다. 오목에서 정의하는 열린 4란 도대체 무엇일까요?

### 열린 4 – 어떤 4는 다른 4보다 더 강력합니다.

여기 2페이지 전에 알아본 5가지의 4가 있습니다. 사실 이 중 하나의 4는 나머지 4와 다른 점 하나를 가지고 있습니다. 8열에 늘어선 4는 열린 4이기 때문입니다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/straight-four.png)

*즉시 방어하지 않는다면 즉시 패배하기에, 4를 즉시 막아야만 합니다. 한 번 막아 봅시다.*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/straight-four-had-blocked.png)

다른 4는 모두 막을 수 있었지만, 8열에 늘어선 4는 한 수로는 막을 수 없었습니다. 두 수를 들여 막아야 하지만, 오목에서는 한 턴에 한 수밖에 두지 못 함으로 결코 방어할 수 없습니다.

이와 같이 한 수로 방어할 수 없는 강력한 4, **양 옆이 빈 채 연속적으로 배열된 돌 4개**를 **열린 4**라고 부릅니다.

### 한 수를 더 두어 열린 4를 만들 수 있다면 3입니다.

오목에서는 **한 수를 더 두어 열린 4를 만들 수 있는 모양**을 **3**으로 정의합니다. 3은 4보다는 약한 모양이지만, 내가 3 또는 4를 가지고 있지 않을 때 상대가 3을 만들었다면 곧바로 대응해야 하는 강력한 모양입니다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/three.png)

*한 수를 더 둔다면...*

*...열린 4가 만들어 집니다!*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/three-expanded.png)

## 3-3 금수에 대해 알아봅시다. {#33금수}

이제 오목에서 정의하는 3이 무엇인지 알았으므로, 3-3금수를 명확하게 정의할 수 있습니다. **3-3금수**란, **한 수를 더 두어 열린 4를 만들 수 있는 3을 두 개 이상 만들 수 있는 자리**를 뜻합니다. 한 수를 두어 3을 세 개 만들 수 있더라도 3-3금수입니다.

*다시 기억해 주세요. 모든 금수는 흑에만 적용됩니다. 백은 자유롭게 3-3을 만들어 승리할 수 있습니다.*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/dobule-three-forbid.png)

지금까지는 매우 단순한 모양들을 살펴봤지만, 실전에서는 간혹 복잡하고 미묘한 모양이 등장하기도 합니다. 여기 간단한 예시들이 있습니다. 아래 모양들에는 3-3금수가 단 하나도 포함되지 않습니다. 

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/dobule-three-forbid-pseudo.png)

이 모양들은 모두 3 두 개가 중첩된 모양이 아닌, 사실은 3 하나만으로 이루어진 모양이기 때문입니다. 3처럼 보인다고 해도 열린 4를 만들 수 없다면 3이 아닙니다. 한 번에 3을 두 개 이상 만들 수 없다면 3-3금수가 아닙니다.

## 6목 금수에 대해 알아봅시다. {#6목금수}

거의 다 왔습니다. **6목** 금수란, **한 수를 더 두어 연속적으로 배열된 6개 이상의 돌을 만들 수 있는 자리**를 뜻합니다. 

한 수를 더 두어 7목을 만들 수 있다고 해도 6목 금수입니다. 8목도, 9목도 마찬가지입니다. 다만 10목은 예외입니다. 누군가 10목을 만들었다면 즉시 모든 전자기기를 끄고 그 장소를 떠나야만 합니다.

*마지막으로 기억해 주세요. 6목 금수 역시 흑에게만 적용되는 규칙으로, 백은 자유롭게 6목 이상을 만들어 승리할 수 있습니다.*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/overline-forbid.png)

## 오목은 모든 금수를 무시할 수 있습니다.

좋은 소식이 있습니다(아쉽게도 흑에만요). 오목으로 승리할 수 있는 자리에 금수가 만들어진다고 해도, 오목으로 승리할 수 있다면 그 어떤 금수도 무시한 채 돌을 둘 수 있습니다. 뒷목 잡을 일은 없겠군요!

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/five-in-a-row-and-forbid.png)

## 훌륭합니다!

더 공평한 오목의 세계에 도착하신 것을 정말 환영합니다! 여기까지 따라왔다면 기본적인 렌주 규칙들을 모두 아셨습니다. 이제는 금수가 등장하더라도, 당황하지 않은 채 금수를 풀거나 다른 전략을 사용해 게임을 이어나갈 수 있을 것입니다.

이제 ``/시작 @멘션`` 명령어로 친구들과 게임을 시작해 보세요. 만약 친구가 없더라도 GomokuBot 인공지능이 언제나 함께 할 것입니다. ``/설정`` 명령어로 입맛에 맞게 GomokuBot의 행동을 바꾸는 것 역시 잊지 말아 주세요.

다음 장부터는 매우 복잡한 상황에서 금수를 정확히 판단하는 방법과 금수를 이용한 공격과 방어에 대해 알아봅니다. 모두 렌주에서만 가능한, 렌주를 위한 전략들입니다. 초심자에게는 조금 어려울 수 있습니다.

## 금수 같지만, 금수가 아닐 수 있습니다. {#의사금수}

아래와 같은 상황에 대해 생각해 봅시다. 흑은 과연 ``h9``에 둘 수 있을까요? 얼핏 보기에는 ``h9``는 h행에 세로로 배열된 돌 두 개와 9열에 가로로 배열된 돌 두 개에 의해 만들어지는 3-3금수로, 흑은 ``h9``에 둘 수 없어야만 할 것 같습니다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-simple.png)

이런 상황들이 흔하게 등장하는 것은 아니지만(사실 실전에서는 정말로, 매우 드물게 등장합니다.), 금수가 무엇인지 완벽히 파악하기 위해서는 꼭 알아 둘 필요가 있습니다.

### 한 수 뒤를 생각해 보세요.

복잡한 상황에서 금수를 판단하기 좋은 방법은 하나씩 놓아 보는 것입니다. 지금 판단하고자 하는 ``h9``에 돌 하나를 놓아 봅시다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-simple-s1.png)

h행에서는 4-4 금수에 막혀 열린 4를 만들 수 없습니다. h행의 돌 두 개는 한 수를 둠으로써 열린 4를 만들 수 있는 3이 이 아니었던 것입니다. 다시 기억해 봅시다. 3또는 4가 성립되지 않는다면, 금수 또한 성립되지 않습니다. 그러므로 3을 하나만 만들 수 있는 ``h9``는 3-3금수가 아닙니다.

## 금수가 아닌 것 같지만 금수일 수 있습니다. {#복잡한-의사금수}

``g10``은 3-3금수같아 보입니다. 하지만 ``i8`` 또한 금수이기에, ``g10``은 3-3금수가 아닌 것 같기도 합니다. 이런 복잡한 상황에서 어떻게 흑이 ``g10``에 둘 수 있는지 판단할 수 있을까요?

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-complex.png)

### 여러 수 뒤를 생각해 보세요.

매우 복잡한 금수를 판단하기 가장 좋은 방법 역시 하나씩 놓아 보는 것입니다. 지금 판단하고자 하는 ``g10``에 돌 하나를 놓아 봅시다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-complex-s1.png)

얼핏 보기에는 ``i8``은 i행에 세로로 놓인 돌 두 개와 8열에 가로로 놓인 돌 두 개가 합쳐지는 곳으로 3-3금수인 것 같습니다. 그렇다면 대각선으로 열린 4를 만들 수 없을테니, ``g10``은 금수가 아닐까요?

그렇다기에는 f행에 세로로 배열된 돌 3개가 꺼림칙하군요. 판단하기에는 아직 이른 것 같습니다. ``i8``에 돌 하나를 더 놓아봅시다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-complex-s2.png)

``i8``에 돌을 놓아 보니, 8열에서는 4-4금수에 막혀 열린 4를 만들 수 없었습니다. ``g10``에 돌 하나를 둔 뒤의 ``i8``은 3-3 금수가 아니었습니다!

이제 ``g10``에 돌 하나를 둔 뒤의 ``i8``은 금수가 아님을 알았으므로, 한 수를 놓아 열린 4 두 개를 만들 수 있는 ``g10``은 다시 3-3금수가 맞다고 판단할 수 있습니다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-complex-s3.png)

같은 방법으로 ``i10`` 역시 3-3금수임을 판단해 낼 수 있습니다.(이쪽은 조금 더 복잡합니다.) 천천히, 하나씩 판단해 본다면 어렵지 않습니다.

## 금수를 노려 공격할 수 있습니다. {#금수유도}

렌주의 금수는 흑에 있어 장애물일 뿐이지만, 백에 있어서는 전략이자 기회입니다. 금수의 정의에 다시 집중해 보세요. 흑은 오목을 제외한 그 어떤 경우에도 금수에 둘 수 없습니다. 백이 금수에 두어 승리할 수 있더라도 예외가 될 수 없습니다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-simple.png)

여기 재미있는 상황이 하나 있습니다. 흑은 3-3 금수를 하나 가지고 있습니다. 백은 흑의 3-3 금수를 사이에 두고 돌 3개를 늘어놓아 4공격을 위한 준비를 끝냈습니다. 여기서 백이 흑의 금수를 끼고 4를 만들어 공격한다면, 흑은 어떻게 대처할 수 있을까요? 

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-simple-s1.png)

흑이 3-3 금수를 해제하려면 한 수를 더 놓아 3 하나를 제거해야만 합니다. 하지만 그러기에는 늦은 것 같군요. 매우 안타깝게도, 흑은 백의 4 공격을 막을 방법이 없습니다. 흑은 백이 오목을 만들어 승리하는 것을 보고 있어야만 합니다. 

## 금수를 유도해 공격할 수 있습니다.

이제 백이 금수를 끼고 공격한다면, 흑은 백이 이기는 것을 보고 있을 수밖에 없음을 알았습니다. 하지만 언제까지나 우연이나 실수에 기댈 수는 없는 법입니다. 적절한 상황만 주어진다면, 적극적인 공격을 통해 흑이 금수를 만들도록 유도해 승리할 수 있습니다.

여기 백에게 정말 안 좋아 보이는 상황이 하나 있습니다. 흑은 ``f6``으로 3을 만들어 공격했습니다. 얼핏 보기에는 백은 흑의 3을 막아야만 하는 것처럼 보입니다. 왼쪽 아래로 탄탄히 늘어선 흑돌들도 매우 강해 보입니다. 백은 이대로 흑의 공격에 말려들어 패배해야만 할까요?

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-complex.png)

다시 상황을 자세히 살펴봅시다. 백에게는 대각선으로 배열된 돌 3개, 가로로 배열된 돌 3개가 있습니다. 4공격을 두 번 이어나갈 수 있겠군요. 하지만 이것만으로는 의미없는 발작이 될 뿐입니다. 

이 상황에서 주목해야 할 것은 백은 4를 만들어 흑이 ``g9``에 두어야만 하게 만들 수 있다는 점과, 흑이 ``g9``에 둔다면 3-3금수가 생긴다는 점입니다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-complex-s1.png)

*백이 4를 만든다면, 흑은 4를 방어해야만 합니다.*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-complex-s2.png)

*흑은 4를 방어할 수 있었지만, 8열에 가로로 배열된 돌 두 개와 g행에 세로로 배열된 돌 두 개로 3-3금수가 생기고 말았습니다.*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-complex-s3.png)

*``g8``은 3-3금수이기에, 흑은 이어지는 백의 대각선 4공격을 막을 방법이 없습니다. 백의 승리입니다!*

백은 상황을 완전히 뒤집어 버릴 수 있었습니다! 백은 3-3 금수뿐만 아니라 4-4 금수, 6목 금수 역시 같은 방법으로 유도해 내 이길 수 있습니다. 흑은 상황을 잘 읽어, 금수 유도에 빠져 게임을 망치지 않도록 특별히 주의해야만 합니다.

## 금수가 아닌 것으로 만들어 빠져나갈 수 있습니다.

여기 반전 하나가 있습니다. 흑은 금수 유도에 걸리지 않은 채 이길 수 있었습니다. ``f6``은 흑에게 정말로 좋은 자리입니다. 하지만 앞서 확인해 봤듯이, ``f6``에 둔다면 흑은 백의 금수 유도에 걸려 패배하게 될 것입니다. 흑은 어떻게 해야 안전하게 ``f6``에 둘 수 있을까요?

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/counter-forbid-trap.png)

여기서는 금수의 조건을 다시 기억해 볼 필요가 있습니다. 3 또는 4가 성립되지 않는다면, 금수 역시 성립되지 않습니다. 흑은 미리 한 수를 더 두어 ``g9``에 둠으로써 생길 금수를 풀 수 있습니다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/counter-forbid-trap-s1.png)

*백은 흑의 4를 방어해야만 합니다.*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/counter-forbid-trap-s2.png)

이게 전부입니다! 이제 흑은 부담 없이 ``f6``에 둘 수 있습니다. 대각선으로 이어지는 4를 만들 수 있는 자리를 미리 만들었으므로, 백의 금수유도로 만들어질 가로와 세로 두 개의 3 중 세로로 이어지는 3은 이제 더 이상 3이 아닙니다.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/counter-forbid-trap-s4.png)

항상 가능한 것은 아니지만(실전에서는 정말 드물게 등장하는 상황입니다.), 흑은 금수의 조건을 미리 해제하는 노림수를 놓음으로써 금수 유도에서 빠져나갈 수 있습니다. 포기하기 전에 마지막으로 한번 다시 생각해 볼 가치는 충분합니다!

## 완벽합니다!

이제 렌주에 대해 꼭 필요한 만큼 모두 아셨습니다. 오목에 어떤 문제가 있는지 알았으며, 렌주가 오목의 문제를 어떻게 해결했는지 알았습니다. 매우 복잡한 상황에서 렌주를 똑바로 적용하는 법을 알았으며, 렌주를 이용해 공격하는 법도 알았습니다.

렌주는 매우 간단하지만 흥미롭고 무궁무진한 전략을 가진 아주 매력적인 게임입니다. GomokuBot과 함께, 여러분의 친구와 함께 더 복잡한 문제에 도전해 보세요. 분명 재미있을 것입니다.

더 궁금한 것이 있다면, [지원 채널](https://discord.gg/vq8pkfF)에 방문해 부담 없이 질문해 주세요. GomokuBot과 함께 즐거운 시간 보내시기를 바랍니다. — *GomokuBot 개발자 junghyun397 드림.*

## 부록: Taraguchi-10 {#taraguchi-10}

1. 흑은 오목판 중앙에 첫수를 둡니다.
2. 백은 스왑\*할 수 있습니다.
3. 백은 2번째 수를 중앙으로부터 3x3 범위 안에 둡니다.
4. 흑은 스왑할 수 있습니다.
5. 흑은 3번째 수를 중앙으로부터 5x5 범위 안에 둡니다.
6. 백은 스왑할 수 있습니다.
7. 백은 4번째 수를 중앙으로부터 7x7 범위 안에 둡니다.
8. 흑은 둘 중 하나를 고릅니다.
    1. 스왑할 기회를 얻기.
        1. 흑은 스왑할 수 있습니다.
        2. 흑은 5번째 수를 중앙으로부터 9x9 범위 안에 둡니다.
        3. 백은 스왑할 수 있습니다.
        4. 백은 6번째 수를 오목판 아무데나 둡니다.
    2. 5번째 수 10개 고르기.
        1. 흑은 5번째 수 후보 10개를 오목판 아무 데서나 고릅니다. 대칭되는 수\*\*는 고를 수 없습니다.
        2. 백은 흑이 고른 후보 중 하나를 선택해 5번째 수를 둡니다.
        3. 백은 6번째 수를 오목판 아무 데나 둡니다.

\***스왑**: 두 플레이어가 흑과 백을 서로 맞바꿉니다. 스왑을 선택한다면, 상대에게 차례를 넘기게 됩니다. 스왑하지 않는다면, 다음 차례도 자신의 턴입니다.

\*\***대칭되는 수**: 돌리거나 전치했을 때 똑같은 모양이 만들어지는 수.

## 부록: Soosyrv-8 {#soosyrv-8}

1. 흑은 오목판 중앙에 첫 수를 둡니다.
2. 흑은 2번째 수를 중앙으로부터 3x3 범위 안에 둡니다.
3. 흑은 3번째 수를 중앙으로부터 5x5 범위 안에 둡니다.
4. 백은 스왑\*할 수 있습니다.
5. 백은 4번째 수를 오목판 아무 데나 둡니다.
6. 백은 흑이 고를 5번째 수 후보 개수를 선언합니다. 적어도 1개에서 최대 8개까지 선언할 수 있습니다.
7. 흑은 스왑할 수 있습니다.
8. 흑은 5번째 수 후보를 백이 선언한 개수만큼 고릅니다. 대칭되는 수\*\*는 고를 수 없습니다.
9. 백은 흑이 고른 후보 중 하나를 선택해 5번째 수를 둡니다.
10. 백은 6번째 수를 오목판 아무 데나 둡니다.

\***스왑**: 두 플레이어가 흑과 백을 서로 맞바꿉니다. 스왑을 선택한다면, 상대에게 차례를 넘기게 됩니다. 스왑하지 않는다면, 다음 차례도 자신의 턴입니다.

\*\***대칭되는 수**: 돌리거나 전치했을 때 똑같은 모양이 만들어지는 수.
""".trimIndent()
}