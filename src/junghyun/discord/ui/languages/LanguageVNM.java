package junghyun.discord.ui.languages;

public class LanguageVNM extends LanguageENG {

    @Override
    public String LANGUAGE_CODE() {
        return "VNM";
    }

    @Override
    public String LANGUAGE_NAME() {
        return "Tiếng Việt:flag_vn:";
    }
    @Override
    public String LANGUAGE_DESCRIPTION() {
        return "Vui lòng sử dụng lệnh `~lang` `VNM`";
    }

    @Override
    public String HELP_INFO() {
        return "GomokuBot / Trợ giúp";
    }
    @Override
    public String HELP_DESCRIPTION() {
        return "GomokuBot là một con bot Discord mã nguồn mở cho phép bạn chơi ca-rô trên Discord. Mọi dữ liệu trò chơi được dùng để nâng cao tính chính xác của máy khi chơi ca-rô";
    }
    @Override
    public String HELP_DEV() {
        return "Nhà phát triển";
    }
    @Override
    public String HELP_GIT() {
        return "Xem mã nguồn trên GitHub";
    }
    @Override
    public String HELP_VERSION() {
        return "Phiên bản";
    }
    @Override
    public String HELP_SUPPORT() {
        return "Kênh hỗ trợ";
    }
    @Override
    public String HELP_INVITE_LINK() {
        return "Link mời";
    }

    @Override
    public String HELP_CMD_INFO() {
        return "GomokuBot / Dòng lệnh";
    }
    @Override
    public String HELP_CMD_HELP() {
        return "`~help` Xin trợ giúp";
    }
    @Override
    public String HELP_CMD_RANK() {
        return "`~rank` Hiện bảng xếp hạng TOP 10 người chơi xuất sắc";
    }
    @Override
    public String HELP_CMD_LANG(String langList) {
        return "`~lang` " + langList + " Thay đổi ngôn ngữ khi giao tiếp với bot. VD) `~lang` `ENG`";
    }
    @Override
    public String HELP_CMD_SKIN() {
        return "`~skin` `A` `B` `C` Thay đổi skin của bot khi đánh. VD) `~skin` `A`";
    }
    @Override
    public String HELP_CMD_PVE() {
        return "`~start` Đánh với máy.";
    }
    @Override
    public String HELP_CMD_PVP() {
        return "`~start` `@mention` Hai người chơi. VD) `~start` `@player`";
    }
    @Override
    public String HELP_CMD_RESIGN() {
        return "`~resign` Dừng ván đang chơi.";
    }

    @Override
    public String SKIN_INFO() {
        return "GomokuBot / Skin";
    }
    @Override
    public String SKIN_DESCRIPTION() {
        return "Skin A có thể không hiển thị chính xác với một số thiết bị. Chọn các skin có sẵn còn lại và sử dụng";
    }
    @Override
    public String SKIN_CMD_INFO(String style) {
        return "Nhập lệnh ``~skin`` ``" + style + "`` để chọn skin này";
    }
    @Override
    public String SKIN_CHANGE_ERROR() {
        return "Cú pháp chỉnh skin không hợp lệ.";
    }
    @Override
    public String SKIN_CHANGE_SUCCESS(String style) {
        return "Skin chơi ca-rô đã chuyển sang skin ``" + style + "`` !";
    }

    @Override
    public String RANK_INFO() {
        return "GomokuBot / Bảng xếp hạng";
    }
    @Override
    public String RANK_DESCRIPTION() {
        return "TOP 10 người chơi xuất sắc.";
    }
    @Override
    public String RANK_WIN() {
        return "Thắng";
    }
    @Override
    public String RANK_LOSE() {
        return "Thua";
    }

    @Override
    public String LANG_CHANGE_ERROR() {
        return "Cú pháp chỉnh ngôn ngữ không hợp lệ.";
    }
    @Override
    public String LANG_SUCCESS() {
        return "Đã chỉnh ngôn ngữ sang Tiếng Việt :flag_vn: !\n`Translated by`: `Dongvan Technologies`";
    }

    @Override
    public String GAME_NOT_FOUND(String nameTag) {
        return nameTag + ", bạn chưa bắt đầu ván nào. Sử dụng lệnh `~start` để chơi ván mới.";
    }
    @Override
    public String GAME_CREATE_FAIL(String nameTag) {
        return nameTag + ", bạn đánh chưa xong ván trước, vui lòng hoàn thành nốt ván đó. :thinking:";
    }
    @Override
    public String GAME_SYNTAX_FAIL(String nameTag) {
        return nameTag + ", bạn đã nhập sai lệnh đánh. Sử dụng lệnh `~s` `alphabet` `number` để đánh. :thinking:";
    }
    @Override
    public String GAME_ALREADY_IN(String nameTag) {
        return nameTag + ", có người đã đánh chỗ đó rồi! :thinking:";
    }

    @Override
    public String GAME_CREATE_INFO(String playerName, String targetName, String fAttack) {
        return "Ván mới đã được bắt đầu giữa `" + playerName + "` và `" + targetName + "`! `" + fAttack + "` là người đánh trước.";
    }
    @Override
    public String GAME_CMD_INFO() {
        return "Sử dụng lệnh  `~s` `alphabet` `number` để đánh. VD) `~s` `h` `8`";
    }

    @Override
    public String GAME_NEXT_TURN(String curName, String prvName, String lastPos) {
        return "`" + curName + "`, tới lượt đánh của bạn. `" + prvName + "` đã đánh ở ô " + lastPos;
    }

    @Override
    public String GAME_PVP_TURN(String turnName) {
        return "`" + turnName + "` đang suy nghĩ. Chờ `" + turnName + "` đánh rồi mới đến lượt của bạn. :thinking:";
    }
    @Override
    public String GAME_PVP_WIN(String winName, String loseName, String lastPos) {
        return "`" + winName + "` đã thắng `" + loseName + "`. Vị trí mà người thắng đánh cuối cùng là ô " + lastPos + "!";
    }
    @Override
    public String GAME_PVP_RESIGN(String winName, String loseName) {
        return "`" + winName + "` đã thắng `" + loseName + "` vì `" + loseName + "` đã xin dừng cuộc chơi!";
    }

    @Override
    public String GAME_PVP_INFO(String winName, String loseName, int winCount, int loseCount) {
        return "Tỉ số thắng thua giữa `" + winName + "` và `" + loseName + "` là: `" + winName + " : " + loseName + "`.";
    }

    @Override
    public String GAME_PVE_WIN(String lastPos) {
        return "Bạn thắng máy bằng cách đánh ở ô " + lastPos + ". Chúc mừng! :tada:";
    }
    @Override
    public String GAME_PVE_LOSE(String lastPos) {
        return "Bạn đã thua máy. Máy đã đánh ở ô gần nhất là " + lastPos + ".";
    }
    @Override
    public String GAME_PVE_RESIGN() {
        return "Bạn đã thua vì đã xin dừng cuộc chơi.";
    }

    @Override
    public String GAME_PVE_INFO(String playerName, int winCount, int loseCount, int rank) {
        return "Tỉ số thắng thua của bạn khi đấu với máy là `" + winCount + " : " + loseCount + "`. Lúc này " + playerName + " đang xếp";
    }

    @Override
    public String GAME_FULL() {
        return "Ván này hoà vì hết ô để đánh.";
    }

    @Override
    public String GAME_ARCHIVED(String messageLink) {
        return ":tada: Bạn đánh rất hay - Click chuột vào link \n " + messageLink + " để xem lại ván này.";
    }

    @Override
    public String BOARD_INP() {
        return "Đang xử lý";
    }
    @Override
    public String BOARD_FINISH() {
        return "Đã xong";
    }
    @Override
    public String BOARD_TURNS() {
        return "Số lượt đánh của cả hai đối thủ";
    }
    @Override
    public String BOARD_TURN() {
        return "Lượt đánh";
    }
    @Override
    public String BOARD_LOCATION() {
        return "Ô đánh trước của đối thủ:";
    }

}
