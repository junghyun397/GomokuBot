package core.interact.i18n

import core.assets.UNICODE_RIGHT

class LanguageJPN : LanguageENG() {

    override fun languageCode() = "JPN"

    override fun languageName() = "\uD83C\uDDEF\uD83C\uDDF5 日本語"
    override fun languageSuggestion() = "「/lang JPN」と入力して、日本語を使用してください。"

    override fun aiLevelAmoeba() = "アメーバ"
    override fun aiLevelApe() = "サル"
    override fun aiLevelBeginner() = "初心者"
    override fun aiLevelIntermediate() = "中級者"
    override fun aiLevelAdvanced() = "上級者"
    override fun aiLevelExpert() = "エキスパート"
    override fun aiLevelGuru() = "グル"

    override fun helpCommand() = "help"
    override fun helpCommandDescription() = "ヘルプを表示します。"

    override fun settingsCommand() = "settings"
    override fun settingsCommandDescription() = "設定パネルを表示します。"

    override fun helpAboutEmbedTitle() = "GomokuBot / ヘルプ"
    override fun helpAboutEmbedDescription(platform: String) =
        "今、 **$platform** で **五目並べ** をプレイできます。 **GomokuBot** ができます。" +
                " ― GomokuBotは、 $platform で五目並べ（[連珠](https://www.renju.net/rules/)）機能を提供するAIボットです。" +
                "収集されたデータは、強化学習モデルのトレーニングに使用されます。"
    override fun helpAboutEmbedDeveloper() = "開発者"
    override fun helpAboutEmbedRepository() = "Gitリポジトリ"
    override fun helpAboutEmbedVersion() = "バージョン"
    override fun helpAboutEmbedSupport() = "サポートチャンネル"
    override fun helpAboutEmbedInvite() = "招待リンク"

    override fun commandUsageEmbedTitle() = "GomokuBot / コマンド"
    override fun commandUsageHelp() = "ヘルプを表示します。"
    override fun commandUsageSettings() = "設定画面を表示します。"
    override fun commandUsageRankGlobal() = "GomokuBotの全体ランキングを1位から10位まで表示します。"
    override fun commandUsageRankServer() = "このサーバー内でのランキングを表示します。"
    override fun commandUsageRankUser() = "メンションしたユーザーの対戦相手のランキングを表示します。"
    override fun commandUsageRating() = "``GomokuBot ELO`` レーティングを表示します。"

    override fun commandUsageLang(langList: String) =
        "このサーバーで使用される言語設定を変更します。例) ``/lang`` ``JPN``"
    override fun commandUsageStyle() =
        "このサーバーで使用される五目並べの盤面スタイルを変更します。例) ``/style`` ``A``"

    override fun commandUsageStartPVE() = "AIとの新しいゲームを開始します。"
    override fun commandUsageStartPVP() =
        "メンションしたユーザーに対してゲームリクエストを送信します。例) ``/start`` ``@user``"
    override fun commandUsageResign() = "途中でゲームを降参します。"

    override fun rankCommand() = "rank"
    override fun rankCommandDescription() = "1位から10位のランキングを取得します。"
    override fun rankCommandSubGlobal() = "global"
    override fun rankCommandSubGlobalDescription() = "Gomokubotの全体ランキングを取得します。"
    override fun rankCommandSubServer() = "server"
    override fun rankCommandSubServerDescription() = "内部サーバーランキングを取得します。"
    override fun rankCommandSubUser() = "user"
    override fun rankCommandSubUserDescription() = "対戦相手のランキングを取得します。"
    override fun rankCommandOptionPlayer() = "player"
    override fun rankCommandOptionPlayerDescription() = "対戦相手のランキングを確認するプレイヤーを指定します。"

    override fun rankErrorNotFound() = "ユーザーレコードが見つかりません。GomokuBot PvPをプレイしたユーザーを指定してください。"

    override fun rankEmbedTitle() = "GomokuBot / ランキング"
    override fun rankEmbedDescription() = "1位から10位までのランキング。"
    override fun rankEmbedWin() = "勝利数"
    override fun rankEmbedLose() = "敗北数"
    override fun rankEmbedDraw() = "引き分け数"

    override fun ratingCommand() = "rating"
    override fun ratingCommandDescription() = "レーティングを取得します。"
    override fun ratingCommandOptionUser() = "user"
    override fun ratingCommandOptionUserDescription() = "レーティングを確認するユーザーを指定します。"

    override fun ratingEmbed() = TODO()
    override fun ratingEmbedDescription() = TODO()

    override fun languageCommand() = "lang"
    override fun languageCommandDescription() = "このサーバーで使用される言語設定を変更します。"
    override fun languageCommandOptionCode() = "language"
    override fun languageCommandOptionCodeDescription() = "言語コードを選択してください。"

    override fun languageUpdated() = "言語設定が日本語:flag_jp:になりました。"

    override fun styleCommand() = "style"
    override fun styleCommandDescription() = "サーバーで使用する五目並べのボードスタイルを変更します。"
    override fun styleCommandOptionCode() = "style"
    override fun styleCommandOptionCodeDescription() = "スタイルコードを選択してください。"

    override fun styleEmbedTitle() = "GomokuBot / スタイル"
    override fun styleEmbedDescription() =
        "このサーバーに適用されているデフォルトの五目並べのボードスタイル(``スタイルA``)は、正しく表示されない場合があります。" +
                "お好みの4つのスタイルから1つ選んでください。"
    override fun styleEmbedSuggestion(styleName: String) = "このスタイルを使用するには ``/style`` $styleName を入力してください。"

    override fun styleErrorNotfound() =
        "スタイルコードの指定にエラーがあります。 ``/style`` ``スタイルコード``の形式で入力してください。"

    override fun styleUpdated(styleName: String) =
        "スタイル設定が ``$styleName`` に変更されました！"

    override fun settingApplied(kind: String, choice: String) = "$kind の設定が $choice に変更されました。"

    override fun style() = "スタイル"

    override fun styleSelectImage() = "イメージ"
    override fun styleSelectImageDescription() =
        "イメージとしてレンダリングします。プラットフォームサーバーの状態によっては、遅延が生じる場合があります。"

    override fun styleSelectText() = "テキスト"
    override fun styleSelectTextDescription() = "等幅フォントのテキストとしてレンダリングします。最も高速です。"

    override fun styleSelectDottedText() = "ドット入りテキスト"
    override fun styleSelectDottedTextDescription() = "ブランクの代わりにドットを使ってレンダリングします。"

    override fun styleSelectUnicodeText() = "ユニコード"
    override fun styleSelectUnicodeTextDescription() =
        "ユニコード文字としてレンダリングします。フォント設定によっては、壊れた表示になる場合があります。"

    override fun focus() = "フォーカス"

    override fun focusEmbedTitle() = "GomokuBot / フォーカス"
    override fun focusEmbedDescription() =
        "GomokuBotは直感的な入力のために小型の「ボタン盤」を使用しています。GomokuBotがどのようにボードにズームインするかを設定してください。"

    override fun focusSelectIntelligence() = "インテリジェンス"
    override fun focusSelectIntelligenceDescription() =
        "GomokuBot推論エンジンは、最適な場所に焦点を当てます。"

    override fun focusSelectFallowing() = "フォロー"
    override fun focusSelectFallowingDescription() =
        "常に最後の手に焦点を当てます。"

    override fun hint() = "ヒント"

    override fun hintEmbedTitle()= "GomokuBot / ヒント"
    override fun hintEmbedDescription() =
        "Gomokuには、負けるかどうかを決定する重要な手があります。GomokuBotが重要な手をどのように強調するかを設定してください。"

    override fun hintSelectFive() = "五目"
    override fun hintSelectFiveDescription() = "五目並べになる手をハイライト表示します。"

    override fun hintSelectOff() = "オフ"
    override fun hintSelectOffDescription() = "どの手もハイライト表示しません。"

    override fun mark() = "マーク"

    override fun markEmbedTitle() = "Gomokubot / マーク"
    override fun markEmbedDescription() =
        "多くの石の中で、相手の最後の手を覚えるのは簡単なことではありません。Gomokubotが最後の手をどのように表示するかを設定してください。"

    override fun markSelectLast() = "最後の手"
    override fun markSelectLastDescription() =
        "相手が最後に移動した場所に小さな点を描画します。"

    override fun markSelectRecent() = "最近の手"
    override fun markSelectRecentDescription() =
        "相手が最後に移動した場所に小さな点を描画し、自分の最後の手には細い十字を描画します。"

    override fun markSelectSequence() = "シーケンス"
    override fun markSelectSequenceDescription() =
        "移動された順序ですべての石にマークを付けます。"

    override fun swap() = "スワップ"

    override fun swapEmbedTitle() = "GomokuBot / スワップ"
    override fun swapEmbedDescription() =
        "GomokuBotは非常にたくさんのメッセージを送信します。GomokuBotが送信するメッセージに対してどのように処理するかを設定してください。"

    override fun swapSelectRelay() = "中継"
    override fun swapSelectRelayDescription() =
        "プレーヤーが新しい手を打つと、以前に送信されたすべてのメッセージをクリアします。"

    override fun swapSelectArchive() = "アーカイブ"
    override fun swapSelectArchiveDescription() =
        "ナビゲーター以外のメッセージを削除しないでください。"

    override fun swapSelectEdit() = "編集"
    override fun swapSelectEditDescription() =
        "もうメッセージを送信しないでください。最初に送信されたメッセージを編集してください。"

    override fun archive() = "アーカイブ"

    override fun archiveEmbedTitle() = "GomokuBot / アーカイブ"
    override fun archiveEmbedDescription() =
        "GomokuBot は、プレイヤーの素晴らしいゲーム結果を GomokuBot の公式チャンネルにアーカイブしています。" +
                "もちろん、GomokuBot はプレイヤーのプライバシーに重視を置いています。ゲームの結果をどのようにアーカイブするかを設定してください。"

    override fun archiveSelectByAnonymous() = "匿名で共有"
    override fun archiveSelectByAnonymousDescription() =
        "プレイヤーのゲーム結果を匿名で共有します。"

    override fun archiveSelectWithProfile() = "プロフィールで共有"
    override fun archiveSelectWithProfileDescription() =
        "プレイヤーのプロフィール写真と名前でゲーム結果を共有します。"

    override fun archiveSelectPrivacy() = "プライバシーを守る"
    override fun archiveSelectPrivacyDescription() =
        "プレイヤーのゲーム結果を誰とも共有しません。"

    override fun sessionNotFound(): String =
        "進行中のゲームはありません。 ``/start`` コマンドで新しいゲームを開始してください。"

    override fun startCommand() = "start"
    override fun startCommandDescription() = "新しいゲームを開始します。"
    override fun startCommandOptionOpponent() = "opponent"
    override fun startCommandOptionOpponentDescription() = "ゲーム相手のユーザーを指定します。"

    override fun startErrorSessionAlready() =
        "すでにゲームが進行中です。現在進行中のゲームを終了してください。"
    override fun startErrorOpponentSessionAlready(opponent: String) =
        "$opponent はすでに別のゲームをプレイ中です。$opponent のゲームが終了するまでお待ちください。"
    override fun startErrorRequestAlreadySent(opponent: String) =
        "$opponent へのゲームリクエストはまだ保留中です。$opponent の返答をお待ちください。"
    override fun startErrorRequestAlready(opponent: String) =
        "$opponent からのゲームリクエストにまだ返答していません。先に $opponent のゲームリクエストに返答してください。"
    override fun startErrorOpponentRequestAlready(opponent: String) =
        "$opponent がまだ返答していない別のゲームリクエストがあります。$opponent が他のゲームリクエストに返答するまでお待ちください。"

    override fun setCommandDescription() = "石を置く。"
    override fun setCommandOptionColumn() = "列"
    override fun setCommandOptionColumnDescription() = "アルファベット"
    override fun setCommandOptionRow() = "行"
    override fun setCommandOptionRowDescription() = "数字"

    override fun setErrorIllegalArgument() =
        "コマンド形式にエラーがあります。 ``/s`` ``アルファベット`` ``数字`` の形式で入力してください。"

    override fun setErrorExist(move: String) =
        "$move にはすでに石があります。他の場所に移動してください。"

    override fun setErrorForbidden(move: String, forbiddenKind: String) =
        "$move は $forbiddenKind 禁止された手です。他の場所に移動してください。"

    override fun resignCommand() = "resign"
    override fun resignCommandDescription() = "進行中のゲームから投了します。"

    override fun requestEmbedTitle() = "五目並べで遊びませんか？"
    override fun requestEmbedDescription(owner: String, opponent: String) =
        "$owner さんが $opponent さんに対してゲームのリクエストを送りました。ボタンを押して応答してください。"
    override fun requestEmbedButtonAccept() = "承諾"
    override fun requestEmbedButtonReject() = "拒否"

    override fun requestRejected(owner: String, opponent: String) =
        "$opponent さんが $owner さんのゲームリクエストを拒否しました。"

    override fun requestExpired(owner: String, opponent: String) =
        "$owner さんが $opponent さんに送信したゲームリクエストが期限切れになりました。もし $opponent さんとゲームを続けたい場合は、新しいリクエストを送信してください。"

    override fun requestExpiredNewRequest() =
        "新しいリクエストを送信する"

    override fun beginPVP(blackPlayer: String, whitePlayer: String) =
        "$blackPlayer vs $whitePlayer のゲームが開始されました！$blackPlayer が先手です。最初の手を打ってください。"

    override fun beginPVEAiBlack(player: String) =
        "$player さんとAIの対戦が始まりました！$player さんは後手です。AIが ``h8`` に置きました。次の手を打ってください。"

    override fun beginPVEAiWhite(player: String) =
        "$player さんとAIの対戦が始まりました！$player さんは先手です。最初の手を打ってください。"

    override fun processNextPVE(lastMove: String) =
        "次の手を打ってください。AIは $lastMove に置きました。"

    override fun processNextPVP(priorPlayer: String, lastMove: String) =
        "次の手を打ってください。$priorPlayer が $lastMove に置きました。"

    override fun processErrorOrder(player: String) =
        "$player さんの番です。$player さんが次の手を打つまでお待ちください。"

    override fun endPVPWin(winner: String, loser: String, lastMove: String) =
        "$loser さんが $lastMove に置いたため、$winner さんが勝ちました！"
    override fun endPVPResign(winner: String, loser: String) =
        "$loser さんが降参したため、$winner さんが勝ちました！"
    override fun endPVPTie(owner: String, opponent: String) =
        "$owner さんと $opponent さんのゲームは引き分けになりました。もう打てる場所がなかったためです。"
    override fun endPVPTimeOut(winner: String, loser: String) =
        "$loser さんが長時間次の手を打たなかったため、$winner さんが勝ちました！"

    override fun endPVEWin(player: String, lastPos: String) =
        "AI に $lastPos に置かれなかったため、$player さんが勝ちました。"
    override fun endPVELose(player: String, lastPos: String) =
        "AI に $lastPos に置かれたため、$player さんが負けました。"
    override fun endPVEResign(player: String) =
        "$player さんが降参したため、AI が勝ちました。"
    override fun endPVETie(player: String) =
        "$player さんとAIのゲームは引き分けになりました。もう打てる場所がなかったためです。"
    override fun endPVETimeOut(player: String) =
        "$player さんが長時間次の手を打たなかっ"

    override fun boardInProgress() = "進行中"
    override fun boardFinished() = "終了"

    override fun boardMoves() = "手数"
    override fun boardLastMove() = "前回の手"

    override fun boardResult() = "結果"

    override fun boardWinDescription(winner: String) = "$winner の勝ち"
    override fun boardTieDescription() = "引き分け"

    override fun boardCommandGuide() =
        ":mag: ボタンを押すか、``/s`` ``列`` ``行`` のコマンドを使用して次の手を打ってください。"

    override fun announceWrittenOn(date: String) = "$date に書かれました。"

    override fun somethingWrongEmbedTitle() = "何かが間違っています"

    override fun permissionNotGrantedEmbedDescription(channelName: String) =
        "GomokuBotには、$channelName にメッセージを送信する権限がありません！ 役割と権限の設定を確認してください。"

    override fun permissionNotGrantedEmbedFooter() = "このメッセージは1分後に削除されます。"

    override fun notYetImplementedEmbedDescription() = "この機能はまだ実装されていません。"

    override fun notYetImplementedEmbedFooter(officialChannel: String) =
        "サポートチャンネル($officialChannel)でGomokuBotの最新情報を入手してください。"

    override fun exploreAboutRenju() = "レンジュについて知らないですか？$UNICODE_RIGHT を押してレンジュについて学びましょう。"

}
