package junghyun.ai;

public class Row {

    private Stone[] row;

    private int length;

    private boolean pass_all = false;

    //초기화

    public Row(Stone[] row) {
        this.row = row;
        this.length = row.length;
        if (length < 5) {
            this.pass_all = true;
        }
    }

    //오목 확인

    public boolean findFIve(boolean color) {

        if (pass_all) {
            return false;
        } else {
            int rows_count = 0;

            for (int count = 0; count < this.length; count++) {
                rows_count = this.findFIveLoop(rows_count, count, color);
                if (rows_count >= 5) { //오목 확인 완료
                    return true;
                }
            }
            return false;
        }
    }

    private int findFIveLoop(int rows_count, int count, boolean color) {
        Stone stone = this.row[count];
        if (stone.isStoneAdded()) {
            if(stone.getColor() == color) {
                rows_count++;
                return rows_count;
            }
        }
        return 0;
    }

    //데이터 제어

    public Stone[] get_row() {
        return this.row;
    }

}
