package junghyun.ai.engin;

import junghyun.ai.Game;
import junghyun.ai.Row;
import junghyun.ai.Stone;

import java.util.Arrays;

class AIRow {

    private Game game;

    private Stone[] row;
    private int length;
    private boolean passAll = false;
    private boolean color;

    AIRow(Row row, boolean color, Game game) {
        super();
        this.row = row.getRow();
        this.length = this.row.length;
        this.color = color;
        this.game = game;
        if (length < 5) {
            this.passAll = true;
        }
    }

    void checkPoints() {
        if (passAll) return;

        this.checkLoseFivePoint();
        this.checkWinFivePoint();

        this.checkWinOpenFourPoint();
        this.checkLoseOpenFourPoint();

        this.checkAttackPoint();
        this.checkDefensePoint();
    }

    private void checkLoseFivePoint() {

        Stone target_stone = null;

        for (int stack = 0; stack < this.length - 4; stack++) {
            int stone_count = 0;
            int blank_count = 0;

            for (int stack5 = 0; stack5 < 5; stack5++) {
                int root = stack+stack5;

                if (root > this.length) return;

                Stone stone = this.row[root];

                if (stone.isStoneAdded() && (stone.getColor() == !this.color)) {
                    stone_count++;
                } else if (stone.isStoneAdded() && (stone.getColor() == this.color)) {
                    stack5 = 10;
                } else if (!stone.isStoneAdded()) {
                    blank_count++;
                    target_stone = stone;
                }

                if ((stone_count == 4) && (blank_count == 1)) {
                    game.getPlate()[target_stone.getX()][target_stone.getY()].addFiveCount(game.getPlayerColor());
                    game.getPlate()[target_stone.getX()][target_stone.getY()].addPoint(AISetting.LOSE_5_POINT);
                }
            }
        }
    }

    private void checkWinFivePoint() {

        Stone target_stone = null;

        for (int stack = 0; stack < this.length - 4; stack++) {
            int stone_count = 0;
            int blank_count = 0;

            for (int stack5 = 0; stack5 < 5; stack5++) {
                int root = stack+stack5;

                if (root > this.length) return;

                Stone stone = this.row[root];

                if (stone.isStoneAdded() && (stone.getColor() == this.color)) {
                    stone_count++;
                } else if (stone.isStoneAdded() && (stone.getColor() != this.color)) {
                    stack5 = 10;
                } else if (!stone.isStoneAdded()) {
                    blank_count++;
                    target_stone = stone;
                }

                if ((stone_count == 4) && (blank_count == 1)) {
                    game.getPlate()[target_stone.getX()][target_stone.getY()].addFiveCount(!game.getPlayerColor());
                    game.getPlate()[target_stone.getX()][target_stone.getY()].addPoint(AISetting.WIN_5_POINT);
                }
            }
        }
    }

    private void checkWinOpenFourPoint() {

        Stone target_stone = null;

        for (int stack = 0; stack < this.length - 5; stack++) {
            int stone_count = 0;
            int blank_count = 0;

            for (int stack6 = 0; stack6 < 6; stack6++) {
                int root = stack+stack6;

                if (root > this.length) {
                    return;
                }

                Stone stone = this.row[root];

                if (stone.isStoneAdded() && (stone.getColor() == this.color)) {
                    if ((stack6 == 0) || (stack6 == 5)) {
                        stack6 = 10;
                    }
                    stone_count++;
                } else if (stone.isStoneAdded() && (stone.getColor() != this.color)) {
                    stack6 = 10;
                } else if (!stone.isStoneAdded()) {
                    if (!((stack6 == 0) || (stack6 == 5))) {
                        target_stone = stone;
                    }
                    blank_count++;
                }

                if ((stone_count == 3) && (blank_count == 3)) {
                    assert target_stone != null;
                    game.getPlate()[target_stone.getX()][target_stone.getY()].addOpenFourCount(!game.getPlayerColor());
                    game.getPlate()[target_stone.getX()][target_stone.getY()].addPoint(AISetting.MAKE_OPEN_4_POINT);
                }
            }
        }
    }

    private void checkLoseOpenFourPoint() {

        Stone target_stone = null;

        for (int stack = 0; stack < this.length -5; stack++) {
            int stone_count = 0;
            int blank_count = 0;

            for (int stack6 = 0; stack6 < 6; stack6++) {
                int root = stack+stack6;

                if (root > this.length) {
                    return;
                }

                Stone stone = this.row[root];

                if (stone.isStoneAdded() && (stone.getColor() != this.color)) {
                    if ((stack6 == 0) || (stack6 == 5)) {
                        stack6 = 10;
                    }
                    stone_count++;
                } else if (stone.isStoneAdded() && (stone.getColor() == this.color)) {
                    stack6 = 10;
                } else if (!stone.isStoneAdded()) {
                    if (!((stack6 == 0) || (stack6 == 5))) {
                        target_stone = stone;
                    }
                    blank_count++;
                }

                if ((stone_count == 3) && (blank_count == 3)) {
                    assert target_stone != null;
                    game.getPlate()[target_stone.getX()][target_stone.getY()].addOpenFourCount(game.getPlayerColor());
                    game.getPlate()[target_stone.getX()][target_stone.getY()].addPoint(AISetting.PLAYER_MAKE_OPEN_4_POINT);
                }
            }
        }
    }

    private void checkAttackPoint() {
        for (int stack = 0; stack < this.length -4; stack++) {

            int[] row3 = new int[4];
            int[] row4 = new int[5];

            boolean is_pass = false;

            for (int stack5 = 0; stack5 < 5; stack5++) {

                int root = stack+stack5;

                if (root > this.length) {
                    return;
                }

                Stone stone = this.row[root];

                if (stone.isStoneAdded() && (stone.getColor() != this.color)) {
                    stack5 = 10;
                    is_pass = true;
                } else if (stone.isStoneAdded() && (stone.getColor() == this.color)) {
                    if (stack5 < 4) {
                        row3[stack5] = 1;
                    }
                    row4[stack5] = 1;
                } else if (!stone.isStoneAdded()) {
                    if (stack5 < 4) {
                        row3[stack5] = 0;
                    }
                    row4[stack5] = 0;
                }
            }

            if (!is_pass) {

                if (Arrays.equals(row4, AISetting.FOUR_CASE_1_L)) { //●●○○●
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addFourCount(!this.game.getPlayerColor());
                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addFourCount(!this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_1_R)) { //●○○●●
                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addFourCount(!this.game.getPlayerColor());
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addFourCount(!this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_2_L)) { //●●○●○
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+4].getX()][row[stack+4].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addFourCount(!this.game.getPlayerColor());
                    game.getPlate()[row[stack+4].getX()][row[stack+4].getY()].addFourCount(!this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_2_R)) { //○●○●●
                    game.getPlate()[row[stack].getX()][row[stack].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack].getX()][row[stack].getY()].addFourCount(!this.game.getPlayerColor());
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addFourCount(!this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_3_L)) { //●●●○○
                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+4].getX()][row[stack+4].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addFourCount(!this.game.getPlayerColor());
                    game.getPlate()[row[stack+4].getX()][row[stack+4].getY()].addFourCount(!this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_3_R)) { //○○●●●
                    game.getPlate()[row[stack].getX()][row[stack].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack].getX()][row[stack].getY()].addFourCount(!this.game.getPlayerColor());
                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addFourCount(!this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_4)) { //●○●○●
                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addPoint(AISetting.MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addFourCount(!this.game.getPlayerColor());
                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addFourCount(!this.game.getPlayerColor());

                    stack += 7;
                }

                else if (Arrays.equals(row3, AISetting.THREE_CASE_1)) { //●●○○
                    if (this.checkRiskPos(stack-2, stack+3)) {
                        if ((!row[stack-2].isStoneAdded()) && (!row[stack-1].isStoneAdded())) {
                            game.getPlate()[row[stack-2].getX()][row[stack-2].getY()].addPoint(AISetting.MAKE_OPEN_3_POINT);
                            game.getPlate()[row[stack-1].getX()][row[stack-1].getY()].addPoint(AISetting.MAKE_OPEN_3_POINT);

                            game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.MAKE_OPEN_3_POINT);
                            game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addPoint(AISetting.MAKE_OPEN_3_POINT);

                            game.getPlate()[row[stack-2].getX()][row[stack-2].getY()].addThreeCount(!this.game.getPlayerColor());
                            game.getPlate()[row[stack-1].getX()][row[stack-1].getY()].addThreeCount(!this.game.getPlayerColor());

                            game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addThreeCount(!this.game.getPlayerColor());
                            game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addThreeCount(!this.game.getPlayerColor());

                            stack += 5;
                        }
                    }
                } else if (Arrays.equals(row3, AISetting.THREE_CASE_2)) { //●○●○
                    if (this.checkRiskPos(stack-2, stack+3)) {
                        if (!row[stack-1].isStoneAdded()) {
                            if ((!row[stack-2].isStoneAdded()) || (!row[stack+4].isStoneAdded())) {
                                game.getPlate()[row[stack-1].getX()][row[stack-1].getY()].addPoint(AISetting.MAKE_OPEN_3_POINT);
                                game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addPoint(AISetting.MAKE_OPEN_3_POINT);
                                game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addPoint(AISetting.MAKE_OPEN_3_POINT);

                                game.getPlate()[row[stack-1].getX()][row[stack-1].getY()].addThreeCount(!this.game.getPlayerColor());
                                game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addThreeCount(!this.game.getPlayerColor());
                                game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addThreeCount(!this.game.getPlayerColor());

                                stack += 6;
                            }
                        }
                    }
                } else if (Arrays.equals(row3, AISetting.THREE_CASE_3)) { //●○○●
                    if (this.checkRiskPos(stack-1, stack+4)) {
                        if ((!row[stack-1].isStoneAdded()) && (!row[stack+4].isStoneAdded())) {
                            game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addPoint(AISetting.MAKE_OPEN_3_POINT);
                            game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.MAKE_OPEN_3_POINT);

                            game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addThreeCount(!this.game.getPlayerColor());
                            game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addThreeCount(!this.game.getPlayerColor());

                            stack += 7;
                        }
                    }
                }
            }
        }
    }

    private void checkDefensePoint() {
        for (int stack = 0; stack < this.length -4; stack++) {

            int[] row3 = new int[4];
            int[] row4 = new int[5];

            boolean is_pass = false;

            for (int stack5 = 0; stack5 < 5; stack5++) {

                int root = stack+stack5;

                if (root > this.length) {
                    return;
                }

                Stone stone = this.row[root];

                if (stone.isStoneAdded() && (stone.getColor() == this.color)) {
                    stack5 = 10;
                    is_pass = true;
                } else if (stone.isStoneAdded() && (stone.getColor() != this.color)) {
                    if (stack5 < 4) {
                        row3[stack5] = 1;
                    }
                    row4[stack5] = 1;
                } else if (!stone.isStoneAdded()) {
                    if (stack5 < 4) {
                        row3[stack5] = 0;
                    }
                    row4[stack5] = 0;
                }
            }

            if (!is_pass) {

                if (Arrays.equals(row4, AISetting.FOUR_CASE_1_L)) { //●●○○●
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addFourCount(this.game.getPlayerColor());
                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addFourCount(this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_1_R)) { //●○○●●
                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addFourCount(this.game.getPlayerColor());
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addFourCount(this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_2_L)) { //●●○●○
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+4].getX()][row[stack+4].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addFourCount(this.game.getPlayerColor());
                    game.getPlate()[row[stack+4].getX()][row[stack+4].getY()].addFourCount(this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_2_R)) { //○●○●●
                    game.getPlate()[row[stack].getX()][row[stack].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack].getX()][row[stack].getY()].addFourCount(this.game.getPlayerColor());
                    game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addFourCount(this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_3_L)) { //●●●○○
                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+4].getX()][row[stack+4].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addFourCount(this.game.getPlayerColor());
                    game.getPlate()[row[stack+4].getX()][row[stack+4].getY()].addFourCount(this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_3_R)) { //○○●●●
                    game.getPlate()[row[stack].getX()][row[stack].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack].getX()][row[stack].getY()].addFourCount(this.game.getPlayerColor());
                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addFourCount(this.game.getPlayerColor());

                    stack += 7;
                } else if (Arrays.equals(row4, AISetting.FOUR_CASE_4)) { //●○●○●
                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);
                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addPoint(AISetting.PLAYER_MAKE_CLOSE_4_POINT);

                    game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addFourCount(this.game.getPlayerColor());
                    game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addFourCount(this.game.getPlayerColor());

                    stack += 7;
                }

                else if (Arrays.equals(row3, AISetting.THREE_CASE_1)) { //●●○○
                    if (this.checkRiskPos(stack-2, stack+3)) {
                        if ((!row[stack-2].isStoneAdded()) && (!row[stack-1].isStoneAdded())) {
                            game.getPlate()[row[stack-2].getX()][row[stack-2].getY()].addPoint(AISetting.PLAYER_MAKE_OPEN_3_POINT);
                            game.getPlate()[row[stack-1].getX()][row[stack-1].getY()].addPoint(AISetting.PLAYER_MAKE_OPEN_3_POINT);

                            game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.PLAYER_MAKE_OPEN_3_POINT);
                            game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addPoint(AISetting.PLAYER_MAKE_OPEN_3_POINT);

                            game.getPlate()[row[stack-2].getX()][row[stack-2].getY()].addThreeCount(this.game.getPlayerColor());
                            game.getPlate()[row[stack-1].getX()][row[stack-1].getY()].addThreeCount(this.game.getPlayerColor());

                            game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addThreeCount(this.game.getPlayerColor());
                            game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addThreeCount(this.game.getPlayerColor());

                            stack += 5;
                        }
                    }
                } else if (Arrays.equals(row3, AISetting.THREE_CASE_2)) { //●○●○
                    if (this.checkRiskPos(stack-2, stack+3)) {
                        if (!row[stack-1].isStoneAdded()) {
                            if ((!row[stack-2].isStoneAdded()) || (!row[stack+4].isStoneAdded())) {
                                game.getPlate()[row[stack-1].getX()][row[stack-1].getY()].addPoint(AISetting.PLAYER_MAKE_OPEN_3_POINT);
                                game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addPoint(AISetting.PLAYER_MAKE_OPEN_3_POINT);
                                game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addPoint(AISetting.PLAYER_MAKE_OPEN_3_POINT);

                                game.getPlate()[row[stack-1].getX()][row[stack-1].getY()].addThreeCount(this.game.getPlayerColor());
                                game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addThreeCount(this.game.getPlayerColor());
                                game.getPlate()[row[stack+3].getX()][row[stack+3].getY()].addThreeCount(this.game.getPlayerColor());

                                stack += 6;
                            }
                        }
                    }
                } else if (Arrays.equals(row3, AISetting.THREE_CASE_3)) { //●○○●
                    if (this.checkRiskPos(stack-1, stack+4)) {
                        if ((!row[stack-1].isStoneAdded()) && (!row[stack+4].isStoneAdded())) {
                            game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addPoint(AISetting.PLAYER_MAKE_OPEN_3_POINT);
                            game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addPoint(AISetting.PLAYER_MAKE_OPEN_3_POINT);

                            game.getPlate()[row[stack+1].getX()][row[stack+1].getY()].addThreeCount(this.game.getPlayerColor());
                            game.getPlate()[row[stack+2].getX()][row[stack+2].getY()].addThreeCount(this.game.getPlayerColor());

                            stack += 7;
                        }
                    }
                }
            }
        }
    }

    private boolean checkRiskPos(int start, int end) {
        return (start > -1) && (end <= this.length);
    }


}
