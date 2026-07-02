package core.interact.i18n

import core.assets.UNICODE_RIGHT
import renju.notation.ColorContainer

class LanguageJPN : LanguageENG() {

    override fun languageCode() = "JPN"

    override fun languageName() = "\uD83C\uDDEF\uD83C\uDDF5 日本語"
    override fun languageSuggestion() = "``/lang`` ``JPN`` コマンドを使用してください。"

    override fun engineLevelAmoeba() = "アメーバ"
    override fun engineLevelApe() = "類人猿"
    override fun engineLevelBeginner() = "初心者"
    override fun aiLevelIntermediate() = "中級者"
    override fun engineLevelAdvanced() = "上級者"
    override fun engineLevelExpert() = "達人"
    override fun engineLevelGuru() = "賢者"

    override fun swapSelectYes() = "はい"
    override fun swapSelectNo() = "いいえ"

    override fun branchSelectSwap() = "スワップ"
    override fun branchSelectOffer() = "候補提示"

    override fun ruleSelectRenju() = "連珠 (デフォルト)"
    override fun ruleSelectTaraguchi10() = "Taraguchi-10"
    override fun ruleSelectSoosyrv8() = "Soosyrv-8"

    override fun helpCommand() = "ヘルプ"
    override fun helpCommandDescription() = "ヘルプを表示します。"
    override fun helpCommandOptionShortcut() = "ショートカット"
    override fun helpCommandOptionShortcutDescription() = "指定したヘルプページをすぐに表示します。"
    override fun helpCommandOptionAnnouncements() = "お知らせ"

    override fun settingsCommand() = "設定"
    override fun settingsCommandDescription() = "設定画面を表示します。"

    override fun helpAboutEmbedTitle() = "GomokuBot / ヘルプ"
    override fun helpAboutEmbedDescription(platform: String) =
        "**$platform** でも **五目並べ** を楽しめるようになりました。**GomokuBot** が一緒に遊びます。" +
                " - GomokuBot は $platform で五目並べ([連珠](https://www.renju.net/rules/))を提供する、オープンソースのAI五目並べボットです。"
    override fun helpAboutEmbedDeveloper() = "開発者"
    override fun helpAboutEmbedRepository() = "Gitリポジトリ"
    override fun helpAboutEmbedVersion() = "バージョン"
    override fun helpAboutEmbedSupport() = "サポートチャンネル"
    override fun helpAboutEmbedInvite() = "招待リンク"

    override fun commandUsageEmbedTitle() = "GomokuBot / コマンド"
    override fun commandUsageHelp() = "ヘルプを表示します。"
    override fun commandUsageSettings() = "設定画面を表示します。"
    override fun commandUsageRankGlobal() = "1位から10位までのGomokuBot全体ランキングを表示します。"
    override fun commandUsageRankServer() = "このサーバー内のランキングを表示します。"
    override fun commandUsageRankUser() = "メンションしたユーザーとの対戦ランキングを表示します。"
    override fun commandUsageReplay() = "最近プレイされたゲームのリプレイ一覧を表示します。"
    override fun commandUsageRating() = "``GomokuBot ELO`` レーティングを表示します。"

    override fun commandUsageLang(langList: String) =
        "このサーバーで使用する言語設定を変更します。例) ``/lang`` ``JPN``"
    override fun commandUsageStyle() =
        "このサーバーで使用する五目並べ盤のスタイルを変更します。例) ``/スタイル`` ``A``"

    override fun commandUsageStartEngine() = "AIと新しいゲームを開始します。"
    override fun commandUsageStartPvp() =
        "メンションしたユーザーに新しいゲームを提案します。例) ``/開始`` ``@ユーザー``"
    override fun commandUsageResign() = "進行中のゲームを投了します。"

    override fun commandUsageBoard() = "現在進行中のゲームを新しいメッセージとして開きます。"

    override fun replayCommand() = "リプレイ"
    override fun replayCommandDescription() = "最近終了したゲームを振り返ります。"

    override fun rankCommand() = "ランキング"
    override fun rankCommandDescription() = "1位から10位までのランキングを表示します。"
    override fun rankCommandSubGlobal() = "全体"
    override fun rankCommandSubGlobalDescription() = "GomokuBot全体ランキングを表示します。"
    override fun rankCommandSubServer() = "サーバー"
    override fun rankCommandSubServerDescription() = "サーバー内ランキングを表示します。"
    override fun rankCommandSubUser() = "ユーザー"
    override fun rankCommandSubUserDescription() = "ユーザー別の対戦ランキングを表示します。"
    override fun rankCommandOptionPlayer() = "ユーザー"
    override fun rankCommandOptionPlayerDescription() = "対戦ランキングを確認するユーザーを指定してください。"

    override fun rankErrorNotFound() = "ユーザー記録が見つかりません。GomokuBot PvPのプレイ記録があるユーザーを指定してください。"

    override fun rankEmbedTitle() = "GomokuBot / ランキング"
    override fun rankEmbedDescription() = "1位から10位までの勝利ランキングを確認できます。"
    override fun rankEmbedWin() = "勝"
    override fun rankEmbedLose() = "敗"
    override fun rankEmbedDraw() = "分"

    override fun ratingCommand() = "レーティング"
    override fun ratingCommandDescription() = "レーティングを表示します。"
    override fun ratingCommandOptionUser() = "ユーザー"
    override fun ratingCommandOptionUserDescription() = "レーティングを確認するユーザーを指定してください。"

    override fun ratingNoRecord() = "記録が見つかりません。"

    override fun languageCommand() = "lang"
    override fun languageCommandDescription() = "このサーバーで使用する言語設定を変更します。"
    override fun languageCommandOptionCode() = "言語"
    override fun languageCommandOptionCodeDescription() = "言語コードを選択してください。"

    override fun languageUpdated() = "言語設定が日本語:flag_jp:に変更されました。"

    override fun styleCommand() = "スタイル"
    override fun styleCommandDescription() = "このサーバーで使用する五目並べ盤のスタイルを変更します。"
    override fun styleCommandOptionCode() = "スタイル"
    override fun styleCommandOptionCodeDescription() = "スタイルコードを選択してください。"

    override fun styleEmbedTitle() = "GomokuBot / スタイル"
    override fun styleEmbedDescription() =
        "このサーバーに適用されているデフォルトの五目並べ盤スタイル(``スタイル A``)は、正しく表示されない場合があります。" +
                "用意された4つのスタイルから好きなものを選んでください。"
    override fun styleEmbedSuggestion(styleName: String) = "このスタイルを使用するには ``/スタイル`` $styleName コマンドを入力してください。"

    override fun styleErrorNotfound() =
        "スタイルの指定が正しくありません。``/スタイル`` ``スタイルコード`` の形式で入力してください。"

    override fun styleUpdated(styleName: String) =
        "スタイル設定が ``${styleName}`` に変更されました。"

    override fun settingApplied(kind: String, choice: String) = "$kind 設定が $choice に変更されました。"

    override fun style() = "スタイル"

    override fun styleSelectImage() = "画像"
    override fun styleSelectImageDescription() =
        "五目並べ盤を画像で表示します。プラットフォームサーバーの状態によっては少し遅延する場合があります。"

    override fun styleSelectText() = "テキスト"
    override fun styleSelectTextDescription() = "五目並べ盤をテキストで表示します。最も単純で高速です。"

    override fun styleSelectDottedText() = "ドット付きテキスト"
    override fun styleSelectDottedTextDescription() = "テキストとほぼ同じですが、空白の代わりにドットを表示します。"

    override fun focus() = "拡大"

    override fun focusEmbedTitle() = "GomokuBot / 拡大"
    override fun focusEmbedDescription() =
        "GomokuBotは直感的な入力を助けるために小さな「ボタン盤」を使用します。GomokuBotが盤面のどこをどのように拡大するかを設定してください。"

    override fun focusSelectIntelligence() = "自動"
    override fun focusSelectIntelligenceDescription() =
        "GomokuBotの推論エンジンが最適な位置を分析して拡大します。"

    override fun focusSelectCenter() = "手動"
    override fun focusSelectCenterDescription() =
        "常に最後の手を中央に表示します。"

    override fun hint() = "ヒント"

    override fun hintEmbedTitle()= "GomokuBot / ヒント"
    override fun hintEmbedDescription() =
        "五目並べには勝敗を分ける重要な場所があります。GomokuBotが重要な場所をどのように強調するかを設定してください。"

    override fun hintSelectFive() = "勝利"
    override fun hintSelectFiveDescription() = "五目を作って勝てる場所を強調します。"

    override fun hintSelectOff() = "オフ"
    override fun hintSelectOffDescription() = "どの場所も強調しません。"

    override fun mark() = "マーク"

    override fun markEmbedTitle() = "GomokuBot / マーク"
    override fun markEmbedDescription() =
        "たくさんの石の中から最後に打たれた場所を覚えるのは簡単ではありません。GomokuBotが最後に打たれた石をどのように表示するかを設定してください。"

    override fun markSelectLast() = "最後の手"
    override fun markSelectLastDescription() =
        "最後に打たれた場所に小さな点を表示します。"

    override fun markSelectRecent() = "直近の手"
    override fun markSelectRecentDescription() =
        "相手が最後に打った場所に小さな点を、自分が最後に打った場所に細い十字を表示します。"

    override fun markSelectSequence() = "手順"
    override fun markSelectSequenceDescription() =
        "石が打たれた順番をすべて表示します。"

    override fun swap() = "整理"

    override fun swapEmbedTitle() = "GomokuBot / 整理"
    override fun swapEmbedDescription() =
        "GomokuBotはとても多くのメッセージを送信します。GomokuBotが送信したメッセージをどのように扱うかを設定してください。"

    override fun swapSelectRelay() = "更新"
    override fun swapSelectRelayDescription() =
        "プレイヤーが新しい手を打つたびに、以前送信したメッセージをすべて削除します。"

    override fun swapSelectArchive() = "残す"
    override fun swapSelectArchiveDescription() =
        "ナビゲーターを除き、メッセージを削除しません。"

    override fun swapSelectEdit() = "編集"
    override fun swapSelectEditDescription() =
        "新しいメッセージを送信せず、最初に送信したメッセージを編集します。"

    override fun archive() = "共有"

    override fun archiveEmbedTitle() = "GomokuBot / 共有"
    override fun archiveEmbedDescription() =
        "GomokuBotはいくつかの素晴らしいゲーム結果をGomokuBot公式チャンネルに共有します。" +
                "もちろん、GomokuBotはプレイヤーのプライバシーを非常に重視しています。ゲーム結果をどのように共有するかを設定してください。"

    override fun archiveSelectByAnonymous() = "匿名"
    override fun archiveSelectByAnonymousDescription() =
        "ゲーム結果を匿名で共有します。"

    override fun archiveSelectWithProfile() = "プロフィール付き"
    override fun archiveSelectWithProfileDescription() =
        "プロフィール画像と名前を添えてゲーム結果を共有します。"

    override fun archiveSelectPrivacy() = "非公開"
    override fun archiveSelectPrivacyDescription() =
        "ゲーム結果を誰にも共有しません。"

    override fun sessionNotFound(): String =
        "進行中のゲームが見つかりません。まず ``/開始`` コマンドでゲームを開始してください。"

    override fun startCommand() = "開始"
    override fun startCommandDescription() = "新しいゲームを開始します。"
    override fun startCommandOptionOpponent() = "相手"
    override fun startCommandOptionOpponentDescription() = "一緒にゲームを開始するユーザーを指定してください。"
    override fun startCommandOptionRule() = "ルール"
    override fun startCommandOptionRuleDescription() = "新しく開始するゲームのルールを指定してください。"

    override fun startErrorSessionAlready() =
        "すでに進行中のゲームがあります。先に現在のゲームを終了してください。"
    override fun startErrorOpponentSessionAlready(opponent: String) =
        "$opponent さんはすでに別のゲームをプレイ中です。$opponent さんのゲームが終わるまでお待ちください。"
    override fun startErrorRequestAlreadySent(opponent: String) =
        "$opponent さんに送信した対戦リクエストがまだ残っています。$opponent さんの応答をお待ちください。"
    override fun startErrorRequestAlready(opponent: String) =
        "$opponent さんから届いた対戦リクエストにまだ応答していません。先に $opponent さんの対戦リクエストへ応答してください。"
    override fun startErrorOpponentRequestAlready(opponent: String) =
        "$opponent さんにはまだ応答していない別の対戦リクエストが1件あります。$opponent さんが別の対戦リクエストへ応答するまでお待ちください。"

    override fun setCommandDescription() = "指定した座標に石を置きます。"
    override fun setCommandOptionPosition() = "position"
    override fun setCommandOptionPositionDescription() = "a1からo15までの座標"

    override fun setErrorIllegalArgument() =
        "コマンド形式が正しくありません。``/s`` ``h8`` のように入力してください。"

    override fun setErrorExist(move: String) =
        "${move}にはすでに石が置かれています。別の場所に石を置いてください。"

    override fun setErrorForbidden(move: String, forbiddenKind: String) =
        "${move}は${forbiddenKind}禁手です。別の場所に石を置いてください。"

    override fun resignCommand() = "投了"
    override fun resignCommandDescription() = "進行中のゲームを投了します。"

    override fun boardCommand() = "盤面"
    override fun boardCommandDescription() = "現在進行中のゲームを新しいメッセージとして開きます。"

    override fun requestEmbedTitle() = "五目並べを一局いかがですか？"
    override fun requestEmbedDescription(requester: String, opponent: String) =
        "$requester さんが $opponent さんに対戦リクエストを送りました。下のボタンを押して応答してください。"
    override fun requestEmbedButtonAccept() = "承諾"
    override fun requestEmbedButtonReject() = "拒否"

    override fun requestRejected(requester: String, opponent: String) =
        "$opponent さんが $requester さんの対戦リクエストを拒否しました。"

    override fun requestExpired(requester: String, opponent: String) =
        "$requester さんが $opponent さんに送信した対戦リクエストは期限切れになりました。まだ $opponent さんと対戦したい場合は、新しい対戦リクエストを送信してください。"

    override fun requestExpiredNewRequest() =
        "もう一度提案する"

    override fun beginPvp(players: ColorContainer<String>) =
        "${players.black} さんと ${players.white} さんのゲームが始まりました。${players.black} さんが黒です。${players.black} さんは最初の手を打ってください。"

    override fun beginOpening(players: ColorContainer<String>) =
        "${players.black} さんと ${players.white} さんのオープニングゲームが始まりました。${players.black} さんが黒です。${players.white} さんは黒へスワップするか、そのままプレイするかを選んでください。"

    override fun beginEngineBlack(player: String, gomokubot: String) =
        "$player さんと${gomokubot}のゲームが始まりました。$player さんは白です。AIは ``h8`` に打ちました。2手目を打ってください。"

    override fun beginEngineWhite(player: String, gomokubot: String) =
        "$player さんと${gomokubot}のゲームが始まりました。$player さんが黒です。最初の手を打ってください。"

    override fun processNextEngine(lastMove: String) =
        "次の手を打ってください。AIは ${lastMove}に打ちました。"

    override fun processNextPvp(opponent: String, lastMove: String) =
        "次の手を打ってください。$opponent さんは ${lastMove}に打ちました。"

    override fun processNextOpening(lastMove: String) =
        "${lastMove}に石を置きました。次のオープニング手順に進んでください。"

    override fun processErrorOrder(player: String) =
        "今は $player さんの番です。$player さんが次の手を打つまでお待ちください。"

    override fun gameResultFiveInRow(winner: String, loser: String) =
        "$winner さんが五目を作り、$loser さんに勝ちました。"

    override fun gameResultResign(winner: String, loser: String) =
        "$loser さんが投了したため、$winner さんが勝ちました。"

    override fun gameResultDraw() =
        "もう石を置ける場所がないため、引き分けになりました。"

    override fun gameResultTimeout(winner: String, loser: String) =
        "$loser さんが制限時間内に次の手を打たなかったため、$winner さんが勝ちました。"

    override fun gameResultEngineRating() = "レーティング"

    override fun gameResultEngineRatingChange() = "獲得レーティング"

    override fun boardInProgress() = "進行中"
    override fun boardInOpening() = "オープニング中"
    override fun boardFinished() = "終了"

    override fun boardMoves() = "進行度"
    override fun boardLastMove() = "最後の手"

    override fun boardResult() = "結果"

    override fun boardWinDescription(winner: String) = "$winner 勝利"
    override fun boardTieDescription() = "引き分け"

    override fun boardCommandGuide() =
        ":mag: ボタンを押すか ``/s`` ``座標`` コマンドを入力して次の手を打ってください。"
    override fun boardSwapGuide() =
        ":arrows_counterclockwise: ボタンを押して、黒と白をスワップするか選んでください。"
    override fun boardStatefulSwapGuide(offerCount: Int) =
        ":arrows_counterclockwise: ボタンを押して、黒と白をスワップするか選んでください。黒が提示すべき5手目候補は ``$offerCount`` 個です。"
    override fun boardBranchGuide() =
        ":paperclips: ボタンを押して、黒と白をスワップする機会を得るか、5手目候補10個を相手に提示するか選んでください。"
    override fun boardDeclareGuide() =
        ":paperclips: セレクトメニューで5手目候補をいくつ選ぶか指定してください。"
    override fun boardSelectGuide() =
        ":dart: ボタンを押すか ``/s`` ``座標`` コマンドを入力して5手目を選んでください。"
    override fun boardOfferGuide(remainingMoves: Int) =
        ":question: ボタンを押すか ``/s`` ``position`` コマンドを入力して、5手目候補をあと${remainingMoves}個選んでください。"

    override fun replayEmbedWin() = "勝"
    override fun replayEmbedLose() = "敗"
    override fun replayEmbedDraw() = "分"
    override fun replayEmbedMatchInfo(totalMoves: Int) = "全${totalMoves}手。"
    override fun replayEmbedUnableToReplayDescription() = "このゲームは空のゲームのため、リプレイできません。別のゲームを選択してください。"

    override fun announceWrittenOn(date: String) = "$date に作成"

    override fun somethingWrongEmbedTitle() = "問題が発生しました"

    override fun permissionNotGrantedEmbedDescription(channelName: String) =
        "GomokuBotには $channelName チャンネルへメッセージを送信する権限がありません。ロールと権限設定を確認してください。"

    override fun permissionNotGrantedEmbedFooter() = "このメッセージは1分後に削除されます。"

    override fun notYetImplementedEmbedDescription() = "この機能はまだ実装されていません。"

    override fun notYetImplementedEmbedFooter() =
        "サポートチャンネル(https://discord.gg/vq8pkfF)でGomokuBotの更新情報を受け取れます。"

    override fun exploreAboutRenju() = "連珠が何かわかりませんか？$UNICODE_RIGHT を押して連珠について学びましょう。"

}
