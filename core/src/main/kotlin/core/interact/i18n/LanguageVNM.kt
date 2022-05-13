package core.interact.i18n

class LanguageVNM : LanguageENG() {

    override fun languageCode() = "VNM"

    override fun languageName() = "Tiếng Việt\uD83C\uDDFB\uD83C\uDDF3"
    override fun languageSuggestion() = "Vui lòng sử dụng lệnh `~lang` `VNM`"

    override fun helpAboutEmbedTitle() = "GomokuBot / Trợ giúp"
    override fun helpAboutEmbedDescription(platform: String) =
        "GomokuBot là một con bot Discord mã nguồn mở cho phép bạn chơi ca-rô trên Discord. Mọi dữ liệu trò chơi được dùng để nâng cao tính chính xác của máy khi chơi ca-rô"
    override fun helpAboutEmbedDeveloper() = "Nhà phát triển"
    override fun helpAboutEmbedRepository() = "Xem mã nguồn trên GitHub"
    override fun helpAboutEmbedVersion() = "Phiên bản"
    override fun helpAboutEmbedSupport() = "Kênh hỗ trợ"
    override fun helpAboutEmbedInvite() = "Link mời"

    override fun helpCommandEmbedTitle() = "GomokuBot / Dòng lệnh"
    override fun helpCommandEmbedHelp() = "`~help` Xin trợ giúp"
    override fun helpCommandEmbedRank() = "`~rank` Hiện bảng xếp hạng TOP 10 người chơi xuất sắc"
    override fun helpCommandEmbedLang(langList: String) =
        "`~lang` $langList Thay đổi ngôn ngữ khi giao tiếp với bot. VD) `~lang` `ENG`"
    override fun helpCommandEmbedStyle() = "`~skin` `A` `B` `C` Thay đổi skin của bot khi đánh. VD) `~skin` `A`"
    override fun helpCommandEmbedStartPVE() = "`~start` Đánh với máy."
    override fun helpCommandEmbedStartPVP() = "`~start` `@mention` Hai người chơi. VD) `~start` `@player`"
    override fun helpCommandEmbedResign() = "`~resign` Dừng ván đang chơi."

    override fun styleEmbedTitle() = "GomokuBot / Skin"
    override fun styleEmbedDescription() =
        "Skin A có thể không hiển thị chính xác với một số thiết bị. Chọn các skin có sẵn còn lại và sử dụng"
    override fun styleEmbedSuggestion(styleName: String) = "Nhập lệnh ``~skin`` ``$styleName`` để chọn skin này"
    override fun styleErrorNotfound() = "Cú pháp chỉnh skin không hợp lệ."
    override fun styleUpdated(styleName: String) = "Skin chơi ca-rô đã chuyển sang skin ``$styleName`` !"

    override fun rankEmbedTitle() = "GomokuBot / Bảng xếp hạng"
    override fun rankEmbedDescription() = "TOP 10 người chơi xuất sắc."
    override fun rankEmbedWin() = "Thắng"
    override fun rankEmbedLose() = "Thua"

    override fun languageUpdated() =
        "Đã chỉnh ngôn ngữ sang Tiếng Việt :flag_vn: !\n`Translated by`: `Dongvan Technologies`"

    override fun startErrorSessionAlready(user: String) =
        "$user, bạn đánh chưa xong ván trước, vui lòng hoàn thành nốt ván đó. :thinking:"
    override fun setErrorIllegalArgument(player: String) =
        "player, bạn đã nhập sai lệnh đánh. Sử dụng lệnh `~s` `alphabet` `number` để đánh. :thinking:"
    override fun setErrorExist(player: String, move: String) = "$player, có người đã đánh chỗ đó rồi! :thinking:"

    override fun beginPVP(blackPlayer: String, whitePlayer: String) =
        "Ván mới đã được bắt đầu giữa `$blackPlayer` và `$whitePlayer`! `$blackPlayer` là người đánh trước."

    override fun processNextPVP(player: String, priorPlayer: String, latestMove: String) =
        "`$player`, tới lượt đánh của bạn. `$priorPlayer` đã đánh ở ô $latestMove"

    override fun processErrorOrder(user: String, player: String) =
        "`$player` đang suy nghĩ. Chờ `$player` đánh rồi mới đến lượt của bạn. :thinking:"
    override fun endPVPWin(winner: String, looser: String, latestMove: String) =
        "`$winner` đã thắng `$looser`. Vị trí mà người thắng đánh cuối cùng là ô $latestMove!"
    override fun endPVPResign(winner: String, looser: String) =
        "`$winner` đã thắng `$looser` vì `$looser` đã xin dừng cuộc chơi!"

    override fun endPVEWin(player: String, latestPos: String) = "Bạn thắng máy bằng cách đánh ở ô $latestPos. Chúc mừng! :tada:"
    override fun endPVELose(player: String, latestPos: String) = "Bạn đã thua máy. Máy đã đánh ở ô gần nhất là $latestPos."
    override fun endPVEResign(player: String) = "Bạn đã thua vì đã xin dừng cuộc chơi."

    override fun endPVPTie(owner: String, opponent: String) = "Ván này hoà vì hết ô để đánh."

    override fun boardInProgress() = "Đang xử lý"
    override fun boardFinished() = "Đã xong"
    override fun boardMoves() = "Số lượt đánh của cả hai đối thủ"
    override fun boardLatestMove() = "Ô đánh trước của đối thủ:"

}
