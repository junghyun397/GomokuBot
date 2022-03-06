package interact.i18n

class LanguageVNM : LanguageENG() {

    override fun languageCode(): String = "VNM"

    override fun languageName(): String = "Tiếng Việt:flag_vn:"
    override fun languageDescription(): String = "Vui lòng sử dụng lệnh `~lang` `VNM`"

    override fun helpInfo(): String = "GomokuBot / Trợ giúp"
    override fun helpDescription(): String = 
        "GomokuBot là một con bot Discord mã nguồn mở cho phép bạn chơi ca-rô trên Discord. Mọi dữ liệu trò chơi được dùng để nâng cao tính chính xác của máy khi chơi ca-rô"
    override fun helpDeveloper(): String = "Nhà phát triển"
    override fun helpRepository(): String = "Xem mã nguồn trên GitHub"
    override fun helpVersion(): String = "Phiên bản"
    override fun helpSupport(): String = "Kênh hỗ trợ"
    override fun helpInvite(): String = "Link mời"

    override fun helpCommandInfo(): String = "GomokuBot / Dòng lệnh"
    override fun helpCommandHelp(): String = "`~help` Xin trợ giúp"
    override fun helpCommandRank(): String = "`~rank` Hiện bảng xếp hạng TOP 10 người chơi xuất sắc"
    override fun helpCommandLang(langList: String): String =
        "`~lang` $langList Thay đổi ngôn ngữ khi giao tiếp với bot. VD) `~lang` `ENG`"
    override fun helpCommandSkin(): String = "`~skin` `A` `B` `C` Thay đổi skin của bot khi đánh. VD) `~skin` `A`"
    override fun helpCommandPVE(): String = "`~start` Đánh với máy."
    override fun helpCommandPVP(): String = "`~start` `@mention` Hai người chơi. VD) `~start` `@player`"
    override fun helpCommandResign(): String = "`~resign` Dừng ván đang chơi."

    override fun skinInformation(): String = "GomokuBot / Skin"
    override fun skinDescription(): String =
        "Skin A có thể không hiển thị chính xác với một số thiết bị. Chọn các skin có sẵn còn lại và sử dụng"
    override fun skinCommandInfo(style: String): String = "Nhập lệnh ``~skin`` ``$style`` để chọn skin này"
    override fun skinUpdateError(): String = "Cú pháp chỉnh skin không hợp lệ."
    override fun skinUpdateSuccess(style: String): String = "Skin chơi ca-rô đã chuyển sang skin ``$style`` !"

    override fun rankInfo(): String = "GomokuBot / Bảng xếp hạng"
    override fun rankDescription(): String = "TOP 10 người chơi xuất sắc."
    override fun rankWin(): String = "Thắng"
    override fun rankLose(): String = "Thua"

    override fun langUpdateError(): String = "Cú pháp chỉnh ngôn ngữ không hợp lệ."
    override fun langUpdateSuccess(): String =
        "Đã chỉnh ngôn ngữ sang Tiếng Việt :flag_vn: !\n`Translated by`: `Dongvan Technologies`"

    override fun gameNotFound(nameTag: String): String =
        "$nameTag, bạn chưa bắt đầu ván nào. Sử dụng lệnh `~start` để chơi ván mới."
    override fun gameAlreadyInProgress(nameTag: String): String =
        "$nameTag, bạn đánh chưa xong ván trước, vui lòng hoàn thành nốt ván đó. :thinking:"
    override fun gameSyntaxFail(nameTag: String): String =
        "$nameTag, bạn đã nhập sai lệnh đánh. Sử dụng lệnh `~s` `alphabet` `number` để đánh. :thinking:"
    override fun gameAlreadyExits(nameTag: String): String = "$nameTag, có người đã đánh chỗ đó rồi! :thinking:"

    override fun gameCreatedInfo(playerName: String, targetName: String, fAttack: String): String =
        "Ván mới đã được bắt đầu giữa `$playerName` và `$targetName`! `$fAttack` là người đánh trước."
    override fun gameCommandInfo(): String = "Sử dụng lệnh  `~s` `alphabet` `number` để đánh. VD) `~s` `h` `8`"

    override fun gameNextTurn(curName: String, prvName: String, lastPos: String): String =
        "`$curName`, tới lượt đánh của bạn. `$prvName` đã đánh ở ô $lastPos"

    override fun gamePVPPleaseWait(turnName: String): String =
        "`$turnName` đang suy nghĩ. Chờ `$turnName` đánh rồi mới đến lượt của bạn. :thinking:"
    override fun gamePVPWin(winName: String, loseName: String, lastPos: String): String =
        "`$winName` đã thắng `$loseName`. Vị trí mà người thắng đánh cuối cùng là ô $lastPos!"
    override fun gamePVPWinCausedByResign(winName: String, loseName: String): String =
        "`$winName` đã thắng `$loseName` vì `$loseName` đã xin dừng cuộc chơi!"

    override fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int): String =
        "Tỉ số thắng thua giữa `$winName` và `$loseName` là: `$winName : $loseName`."

    override fun gamePVEWin(lastPos: String): String = "Bạn thắng máy bằng cách đánh ở ô $lastPos. Chúc mừng! :tada:"
    override fun gamePVELose(lastPos: String): String = "Bạn đã thua máy. Máy đã đánh ở ô gần nhất là $lastPos."
    override fun gamePVEResign(): String = "Bạn đã thua vì đã xin dừng cuộc chơi."

    override fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int): String =
        "Tỉ số thắng thua của bạn khi đấu với máy là `$winCount : $loseCount`. Lúc này $playerName đang xếp"

    override fun gameTieCausedByFull(): String = "Ván này hoà vì hết ô để đánh."

    override fun boardInProgress(): String = "Đang xử lý"
    override fun boardFinish(): String = "Đã xong"
    override fun boardProgress(): String = "Số lượt đánh của cả hai đối thủ"
    override fun boardTurns(): String = "Lượt đánh"
    override fun boardLocation(): String = "Ô đánh trước của đối thủ:"

}