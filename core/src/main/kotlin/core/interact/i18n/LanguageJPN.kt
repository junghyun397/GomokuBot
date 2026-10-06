package core.interact.i18n

import core.assets.UNICODE_RIGHT
import core.engine.EngineLevel
import renju.notation.ColorContainer

class LanguageJPN : LanguageENG() {

    override val languageCode = "JPN"

    override val languageName = "\uD83C\uDDEF\uD83C\uDDF5 日本語"
    override val languageSuggestion = "`/lang` `JPN` コマンドを使用してください。"

    override fun engineLevel(engine: EngineLevel) = when (engine) {
        EngineLevel.AMOEBA -> "アメーバ"
        EngineLevel.APE -> "類人猿"
        EngineLevel.BEGINNER -> "初心者"
        EngineLevel.MODERATE -> "中級者"
        EngineLevel.EXPERT -> "上級者"
        EngineLevel.GURU -> "達人"
        EngineLevel.SAGE -> "賢者"
    }

    override val swapSelectYes = "はい"
    override val swapSelectNo = "いいえ"

    override val branchSelectSwap = "スワップ"
    override val branchSelectOffer = "候補提示"

    override val ruleSelectRenju = "連珠 (デフォルト)"
    override val ruleSelectTaraguchi10 = "Taraguchi-10"
    override val ruleSelectSoosyrv8 = "Soosyrv-8"

    override val helpCommand = "ヘルプ"
    override val helpCommandDescription = "ヘルプを表示します。"
    override val helpCommandOptionShortcut = "ショートカット"
    override val helpCommandOptionShortcutDescription = "指定したヘルプページをすぐに表示します。"
    override val helpCommandOptionAnnouncements = "お知らせ"

    override val settingsCommand = "設定"
    override val settingsCommandDescription = "設定画面を表示します。"

    override val helpAboutEmbedTitle = "GomokuBot / ヘルプ"
    override fun helpAboutEmbedDescription(platform: String) =
        "**$platform** でも **五目並べ** を楽しめるようになりました。**GomokuBot** が一緒に遊びます。" +
                " - GomokuBot は $platform で五目並べ([連珠](https://www.renju.net/rules/))を提供する、オープンソースのAI五目並べボットです。"
    override val helpAboutEmbedDeveloper = "開発者"
    override val helpAboutEmbedRepository = "Gitリポジトリ"
    override val helpAboutEmbedVersion = "バージョン"
    override val helpAboutEmbedSupport = "サポートチャンネル"
    override val helpAboutEmbedInvite = "招待リンク"

    override val commandUsageEmbedTitle = "GomokuBot / コマンド"
    override val commandUsageHelp = "ヘルプを表示します。"
    override val commandUsageSettings = "設定画面を表示します。"
    override val commandUsageRankGlobal = "1位から10位までのGomokuBot全体ランキングを表示します。"
    override val commandUsageRankServer = "このサーバー内のランキングを表示します。"
    override val commandUsageRankUser = "メンションしたユーザーとの対戦ランキングを表示します。"
    override val commandUsageReplay = "最近プレイされたゲームのリプレイ一覧を表示します。"
    override val commandUsageRating = "`GomokuBot ELO` レーティングを表示します。"

    override val commandUsageLang =
        "このサーバーで使用する言語設定を変更します。例) `/lang` `JPN`"
    override val commandUsageStyle =
        "このサーバーで使用する五目並べ盤のスタイルを変更します。例) `/スタイル` `A`"

    override val commandUsageStartEngine = "AIと新しいゲームを開始します。"
    override val commandUsageStartPvp =
        "メンションしたユーザーに新しいゲームを提案します。例) `/開始` `@ユーザー`"
    override val commandUsageResign = "進行中のゲームを投了します。"

    override val commandUsageBoard = "現在進行中のゲームを新しいメッセージとして開きます。"

    override val replayCommand = "リプレイ"
    override val replayCommandDescription = "最近終了したゲームを振り返ります。"

    override val rankCommand = "ランキング"
    override val rankCommandDescription = "1位から10位までのランキングを表示します。"
    override val rankCommandSubGlobal = "全体"
    override val rankCommandSubGlobalDescription = "GomokuBot全体ランキングを表示します。"
    override val rankCommandSubServer = "サーバー"
    override val rankCommandSubServerDescription = "サーバー内ランキングを表示します。"
    override val rankCommandSubUser = "ユーザー"
    override val rankCommandSubUserDescription = "ユーザー別の対戦ランキングを表示します。"
    override val rankCommandOptionPlayer = "ユーザー"
    override val rankCommandOptionPlayerDescription = "対戦ランキングを確認するユーザーを指定してください。"

    override val rankErrorNotFound = "ユーザー記録が見つかりません。GomokuBot PvPのプレイ記録があるユーザーを指定してください。"

    override val rankEmbedTitle = "GomokuBot / ランキング"
    override val rankEmbedDescription = "1位から10位までの勝利ランキングを確認できます。"
    override val rankEmbedWin = "勝"
    override val rankEmbedLose = "敗"
    override val rankEmbedDraw = "分"

    override val ratingCommand = "レーティング"
    override val ratingCommandDescription = "レーティングを表示します。"
    override val ratingCommandOptionUser = "ユーザー"
    override val ratingCommandOptionUserDescription = "レーティングを確認するユーザーを指定してください。"

    override val ratingNoRecord = "記録が見つかりません。"

    override val languageCommand = "lang"
    override val languageCommandDescription = "このサーバーで使用する言語設定を変更します。"
    override val languageCommandOptionCode = "言語"
    override val languageCommandOptionCodeDescription = "言語コードを選択してください。"

    override val languageUpdated = "言語設定が日本語:flag_jp:に変更されました。"

    override val styleEmbedTitle = "GomokuBot / スタイル"
    override val styleEmbedDescription =
        "このサーバーに適用されているデフォルトの五目並べ盤スタイル(`スタイル A`)は、正しく表示されない場合があります。" +
                "用意された4つのスタイルから好きなものを選んでください。"
    override fun styleEmbedSuggestion(styleName: String) = "このスタイルを使用するには `/スタイル` $styleName コマンドを入力してください。"

    override val styleErrorNotfound =
        "スタイルの指定が正しくありません。`/スタイル` `スタイルコード` の形式で入力してください。"

    override fun styleUpdated(styleName: String) =
        "スタイル設定が `${styleName}` に変更されました。"

    override fun settingApplied(kind: String, choice: String) = "$kind 設定が $choice に変更されました。"

    override val style = "スタイル"

    override val styleSelectImage = "画像"
    override val styleSelectImageDescription =
        "五目並べ盤を画像で表示します。プラットフォームサーバーの状態によっては少し遅延する場合があります。"

    override val styleSelectText = "テキスト"
    override val styleSelectTextDescription = "五目並べ盤をテキストで表示します。最も単純で高速です。"

    override val styleSelectDottedText = "ドット付きテキスト"
    override val styleSelectDottedTextDescription = "テキストとほぼ同じですが、空白の代わりにドットを表示します。"

    override val focus = "拡大"

    override val focusEmbedTitle = "GomokuBot / 拡大"
    override val focusEmbedDescription =
        "GomokuBotは直感的な入力を助けるために小さな「ボタン盤」を使用します。GomokuBotが盤面のどこをどのように拡大するかを設定してください。"

    override val focusSelectIntelligence = "自動"
    override val focusSelectIntelligenceDescription =
        "GomokuBotの推論エンジンが最適な位置を分析して拡大します。"

    override val focusSelectCenter = "手動"
    override val focusSelectCenterDescription =
        "常に最後の手を中央に表示します。"

    override val hint = "ヒント"

    override val hintEmbedTitle = "GomokuBot / ヒント"
    override val hintEmbedDescription =
        "五目並べには勝敗を分ける重要な場所があります。GomokuBotが重要な場所をどのように強調するかを設定してください。"

    override val hintSelectFive = "勝利"
    override val hintSelectFiveDescription = "五目を作って勝てる場所を強調します。"

    override val hintSelectOff = "オフ"
    override val hintSelectOffDescription = "どの場所も強調しません。"

    override val mark = "マーク"

    override val markEmbedTitle = "GomokuBot / マーク"
    override val markEmbedDescription =
        "たくさんの石の中から最後に打たれた場所を覚えるのは簡単ではありません。GomokuBotが最後に打たれた石をどのように表示するかを設定してください。"

    override val markSelectLast = "最後の手"
    override val markSelectLastDescription =
        "最後に打たれた場所に小さな点を表示します。"

    override val markSelectRecent = "直近の手"
    override val markSelectRecentDescription =
        "相手が最後に打った場所に小さな点を、自分が最後に打った場所に細い十字を表示します。"

    override val markSelectSequence = "手順"
    override val markSelectSequenceDescription =
        "石が打たれた順番をすべて表示します。"

    override val archive = "共有"

    override val archiveEmbedTitle = "GomokuBot / 共有"
    override val archiveEmbedDescription =
        "GomokuBotはいくつかの素晴らしいゲーム結果をGomokuBot公式チャンネルに共有します。" +
                "もちろん、GomokuBotはプレイヤーのプライバシーを非常に重視しています。ゲーム結果をどのように共有するかを設定してください。"

    override val archiveSelectByAnonymous = "匿名"
    override val archiveSelectByAnonymousDescription =
        "ゲーム結果を匿名で共有します。"

    override val archiveSelectWithProfile = "プロフィール付き"
    override val archiveSelectWithProfileDescription =
        "プロフィール画像と名前を添えてゲーム結果を共有します。"

    override val archiveSelectPrivacy = "非公開"
    override val archiveSelectPrivacyDescription =
        "ゲーム結果を誰にも共有しません。"

    override val sessionNotFound: String =
        "進行中のゲームが見つかりません。まず `/開始` コマンドでゲームを開始してください。"

    override val startCommand = "開始"
    override val startCommandDescription = "新しいゲームを開始します。"
    override val startCommandOptionOpponent = "相手"
    override val startCommandOptionOpponentDescription = "一緒にゲームを開始するユーザーを指定してください。"
    override val startCommandOptionRule = "ルール"
    override val startCommandOptionRuleDescription = "新しく開始するゲームのルールを指定してください。"

    override val startErrorSessionAlready =
        "すでに進行中のゲームがあります。先に現在のゲームを終了してください。"
    override fun startErrorOpponentSessionAlready(opponent: String) =
        "$opponent さんはすでに別のゲームをプレイ中です。$opponent さんのゲームが終わるまでお待ちください。"
    override fun startErrorRequestAlreadySent(opponent: String) =
        "$opponent さんに送信した対戦リクエストがまだ残っています。$opponent さんの応答をお待ちください。"
    override fun startErrorRequestAlready(opponent: String) =
        "$opponent さんから届いた対戦リクエストにまだ応答していません。先に $opponent さんの対戦リクエストへ応答してください。"
    override fun startErrorOpponentRequestAlready(opponent: String) =
        "$opponent さんにはまだ応答していない別の対戦リクエストが1件あります。$opponent さんが別の対戦リクエストへ応答するまでお待ちください。"

    override val setCommandDescription = "指定した座標に石を置きます。"
    override val setCommandOptionPosition = "position"
    override val setCommandOptionPositionDescription = "a1からo15までの座標"

    override val setErrorIllegalArgument =
        "コマンド形式が正しくありません。`/s` `h8` のように入力してください。"

    override fun setErrorExist(move: String) =
        "${move}にはすでに石が置かれています。別の場所に石を置いてください。"

    override fun setErrorForbidden(move: String, forbiddenKind: String) =
        "${move}は${forbiddenKind}禁手です。別の場所に石を置いてください。"

    override val resignCommand = "投了"
    override val resignCommandDescription = "進行中のゲームを投了します。"

    override val undoCommand = "待った"
    override val undoCommandDescription = "直前の一手の待ったを申し込みます。"
    override val undoErrorOpening = "オープニング中は待ったを使えません。"
    override val undoErrorNoMoves = "まだ戻せる着手がありません。"
    override val undoErrorLimit = "この対局では待ったをすでに2回使っています。"
    override fun undoCompleted(remainingUndos: Int) = "待ったを使いました。残りの待った: ${remainingUndos}回。"
    override val undoPvpCompleted = "直前の1手を戻しました。"
    override val undoRequestEmbedTitle = "1手戻しませんか？"
    override fun undoRequestEmbedDescription(requester: String, opponent: String) =
        "$requester さんが $opponent さんに待ったを申し込みました。下のボタンで返答してください。次の着手で申し込みは無効になります。"
    override fun undoRequestRejected(requester: String, opponent: String) =
        "$opponent さんが $requester さんの待ったを断りました。"
    override fun undoRequestExpired(requester: String, opponent: String) =
        "$requester さんから $opponent さんへの待ったの申し込みは期限切れになりました。"

    override val boardCommand = "盤面"
    override val boardCommandDescription = "現在進行中のゲームを新しいメッセージとして開きます。"

    override val requestEmbedTitle = "五目並べを一局いかがですか？"
    override fun requestEmbedDescription(requester: String, opponent: String) =
        "$requester さんが $opponent さんに対戦リクエストを送りました。下のボタンを押して応答してください。"
    override val requestEmbedButtonAccept = "承諾"
    override val requestEmbedButtonReject = "拒否"

    override fun requestRejected(requester: String, opponent: String) =
        "$opponent さんが $requester さんの対戦リクエストを拒否しました。"

    override fun requestExpired(requester: String, opponent: String) =
        "$requester さんが $opponent さんに送信した対戦リクエストは期限切れになりました。まだ $opponent さんと対戦したい場合は、新しい対戦リクエストを送信してください。"

    override val requestExpiredNewRequest =
        "もう一度提案する"

    override fun beginPvp(players: ColorContainer<String>) =
        "${players.black} さんと ${players.white} さんのゲームが始まりました。${players.black} さんが黒です。${players.black} さんは最初の手を打ってください。"

    override fun beginOpening(players: ColorContainer<String>) =
        "${players.black} さんと ${players.white} さんのオープニングゲームが始まりました。${players.black} さんが黒です。${players.white} さんは黒へスワップするか、そのままプレイするかを選んでください。"

    override fun beginEngineBlack(player: String, gomokubot: String) =
        "$player さんと${gomokubot}のゲームが始まりました。$player さんは白です。AIは `h8` に打ちました。2手目を打ってください。"

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

    override val gameResultDraw =
        "もう石を置ける場所がないため、引き分けになりました。"

    override fun gameResultTimeout(winner: String, loser: String) =
        "$loser さんが制限時間内に次の手を打たなかったため、$winner さんが勝ちました。"

    override val gameResultEngineRating = "レーティング"

    override val gameResultEngineRatingChange = "獲得レーティング"

    override val boardInProgress = "進行中"
    override val boardInOpening = "オープニング中"
    override val boardFinished = "終了"

    override val boardMoves = "進行度"
    override val boardLastMove = "最後の手"

    override val boardResult = "結果"

    override fun boardWinDescription(winner: String) = "$winner 勝利"
    override val boardTieDescription = "引き分け"

    override val boardCommandGuide =
        ":mag: ボタンを押すか `/s` `座標` コマンドを入力して次の手を打ってください。"
    override val boardSwapGuide =
        ":arrows_counterclockwise: ボタンを押して、黒と白をスワップするか選んでください。"
    override fun boardStatefulSwapGuide(offerCount: Int) =
        ":arrows_counterclockwise: ボタンを押して、黒と白をスワップするか選んでください。黒が提示すべき5手目候補は `$offerCount` 個です。"
    override val boardBranchGuide =
        ":paperclips: ボタンを押して、黒と白をスワップする機会を得るか、5手目候補10個を相手に提示するか選んでください。"
    override val boardDeclareGuide =
        ":paperclips: セレクトメニューで5手目候補をいくつ選ぶか指定してください。"
    override val boardSelectGuide =
        ":dart: ボタンを押すか `/s` `座標` コマンドを入力して5手目を選んでください。"
    override fun boardOfferGuide(remainingMoves: Int) =
        ":question: ボタンを押すか `/s` `position` コマンドを入力して、5手目候補をあと${remainingMoves}個選んでください。"

    override val replayEmbedWin = "勝"
    override val replayEmbedLose = "敗"
    override val replayEmbedDraw = "分"
    override fun replayEmbedMatchInfo(totalMoves: Int) = "全${totalMoves}手。"
    override val replayEmbedUnableToReplayDescription = "このゲームは空のゲームのため、リプレイできません。別のゲームを選択してください。"

    override fun announceWrittenOn(date: String) = "$date に作成"

    override val somethingWrongEmbedTitle = "問題が発生しました"

    override fun permissionNotGrantedEmbedDescription(channelName: String) =
        "GomokuBotには $channelName チャンネルへメッセージを送信する権限がありません。ロールと権限設定を確認してください。"

    override val permissionNotGrantedEmbedFooter = "このメッセージは1分後に削除されます。"

    override val notYetImplementedEmbedDescription = "この機能はまだ実装されていません。"

    override val notYetImplementedEmbedFooter =
        "サポートチャンネル(https://discord.gg/vq8pkfF)でGomokuBotの更新情報を受け取れます。"

    override val exploreAboutRenju = "連珠が何かわかりませんか？$UNICODE_RIGHT を押して連珠について学びましょう。"

}
