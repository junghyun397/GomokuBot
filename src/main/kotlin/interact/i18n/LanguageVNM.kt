package interact.i18n

import java.util.*

class LanguageVNM : LanguageENG() {

    override fun targetRegion() = arrayOf(Locale("vi", "VN"))

    override fun languageCode() = "VNM"

    override fun languageName() = "Tiếng Việt:flag_vn:"
    override fun languageSuggestion() = "Vui lòng sử dụng lệnh `~lang` `VNM`"

    override fun helpInfo() = "GomokuBot / Trợ giúp"
    override fun helpDescription() =
        "GomokuBot là một con bot Discord mã nguồn mở cho phép bạn chơi ca-rô trên Discord. Mọi dữ liệu trò chơi được dùng để nâng cao tính chính xác của máy khi chơi ca-rô"
    override fun helpDeveloper() = "Nhà phát triển"
    override fun helpRepository() = "Xem mã nguồn trên GitHub"
    override fun helpVersion() = "Phiên bản"
    override fun helpSupport() = "Kênh hỗ trợ"
    override fun helpInvite() = "Link mời"

    override fun helpCommandInfo() = "GomokuBot / Dòng lệnh"
    override fun helpCommandHelp() = "`~help` Xin trợ giúp"
    override fun helpCommandRank() = "`~rank` Hiện bảng xếp hạng TOP 10 người chơi xuất sắc"
    override fun helpCommandLang(langList: String) =
        "`~lang` $langList Thay đổi ngôn ngữ khi giao tiếp với bot. VD) `~lang` `ENG`"
    override fun helpCommandSkin() = "`~skin` `A` `B` `C` Thay đổi skin của bot khi đánh. VD) `~skin` `A`"
    override fun helpCommandPVE() = "`~start` Đánh với máy."
    override fun helpCommandPVP() = "`~start` `@mention` Hai người chơi. VD) `~start` `@player`"
    override fun helpCommandResign() = "`~resign` Dừng ván đang chơi."

    override fun styleInformation() = "GomokuBot / Skin"
    override fun styleDescription() =
        "Skin A có thể không hiển thị chính xác với một số thiết bị. Chọn các skin có sẵn còn lại và sử dụng"
    override fun styleCommandInfo(style: String) = "Nhập lệnh ``~skin`` ``$style`` để chọn skin này"
    override fun styleUpdateError() = "Cú pháp chỉnh skin không hợp lệ."
    override fun styleUpdateSuccess(style: String) = "Skin chơi ca-rô đã chuyển sang skin ``$style`` !"

    override fun rankInfo() = "GomokuBot / Bảng xếp hạng"
    override fun rankDescription() = "TOP 10 người chơi xuất sắc."
    override fun rankWin() = "Thắng"
    override fun rankLose() = "Thua"

    override fun langUpdateError() = "Cú pháp chỉnh ngôn ngữ không hợp lệ."
    override fun langUpdateSuccess() =
        "Đã chỉnh ngôn ngữ sang Tiếng Việt :flag_vn: !\n`Translated by`: `Dongvan Technologies`"

    override fun gameNotFound(nameTag: String) =
        "$nameTag, bạn chưa bắt đầu ván nào. Sử dụng lệnh `~start` để chơi ván mới."
    override fun gameAlreadyInProgress(nameTag: String) =
        "$nameTag, bạn đánh chưa xong ván trước, vui lòng hoàn thành nốt ván đó. :thinking:"
    override fun gameSyntaxFail(nameTag: String) =
        "$nameTag, bạn đã nhập sai lệnh đánh. Sử dụng lệnh `~s` `alphabet` `number` để đánh. :thinking:"
    override fun gameInvalidMoveAlreadyExits(nameTag: String) = "$nameTag, có người đã đánh chỗ đó rồi! :thinking:"

    override fun gameCreatedInfo(playerName: String, targetName: String, fAttack: String) =
        "Ván mới đã được bắt đầu giữa `$playerName` và `$targetName`! `$fAttack` là người đánh trước."
    override fun gameCommandInfo() = "Sử dụng lệnh  `~s` `alphabet` `number` để đánh. VD) `~s` `h` `8`"

    override fun gameNextTurn(curName: String, prvName: String, lastPos: String) =
        "`$curName`, tới lượt đánh của bạn. `$prvName` đã đánh ở ô $lastPos"

    override fun gamePVPPleaseWait(turnName: String) =
        "`$turnName` đang suy nghĩ. Chờ `$turnName` đánh rồi mới đến lượt của bạn. :thinking:"
    override fun gamePVPWin(winName: String, loseName: String, lastPos: String) =
        "`$winName` đã thắng `$loseName`. Vị trí mà người thắng đánh cuối cùng là ô $lastPos!"
    override fun gamePVPWinCausedByResign(winName: String, loseName: String) =
        "`$winName` đã thắng `$loseName` vì `$loseName` đã xin dừng cuộc chơi!"

    override fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int) =
        "Tỉ số thắng thua giữa `$winName` và `$loseName` là: `$winName : $loseName`."

    override fun gamePVEWin(lastPos: String) = "Bạn thắng máy bằng cách đánh ở ô $lastPos. Chúc mừng! :tada:"
    override fun gamePVELose(lastPos: String) = "Bạn đã thua máy. Máy đã đánh ở ô gần nhất là $lastPos."
    override fun gamePVEResign() = "Bạn đã thua vì đã xin dừng cuộc chơi."

    override fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int) =
        "Tỉ số thắng thua của bạn khi đấu với máy là `$winCount : $loseCount`. Lúc này $playerName đang xếp"

    override fun gameTieCausedByFull() = "Ván này hoà vì hết ô để đánh."

    override fun inProgress() = "Đang xử lý"
    override fun finish() = "Đã xong"
    override fun progress() = "Số lượt đánh của cả hai đối thủ"
    override fun turns() = "Lượt đánh"
    override fun move() = "Ô đánh trước của đối thủ:"

}
