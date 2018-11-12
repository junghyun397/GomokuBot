package junghyun.ai.engin;

import junghyun.ai.Game;
import junghyun.unit.Pos;
import junghyun.ai.Stone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIBase {

    private Game game;

    public AIBase(Game game) {
        this.game = game;
    }

    public Pos getAiPoint() {
        this.sumPoint();
        return this.getMax();
    }

    private void sumPoint() {
        game.resetAllPoint();
        this.sumDotPoints();
        this.sumRowPoints();
        this.sumOverlapPoint();
    }

    private void sumDotPoints() {
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                Stone stone = game.getPlate()[x][y];
                if (stone.isStoneAdded()) {
                    if (stone.getColor() == game.getColor()) { //자신의 돌
                        this.addDotPoint(stone, AISetting.DEF_AI_POINT);
                    } else if (stone.getColor() != game.getColor()) { //상대의 돌
                        this.addDotPoint(stone, AISetting.DEF_PLAYER_POINT);
                    }
                }
            }
        }
    }

    private void sumRowPoints() {
        AIRow[] x_rows = new AIRow[15];
        AIRow[] y_rows = new AIRow[15];

        AIRow[] xy_rows = new AIRow[29];
        AIRow[] yx_rows = new AIRow[29];

        for (int i = 0; i < 15; i++) {
            x_rows[i] = new AIRow(game.getXRow(i, 0), game.getColor(), this.game);
            y_rows[i] = new AIRow(game.getYRow(0, i), game.getColor(), this.game);
            xy_rows[i] = new AIRow(game.getXYRow(i, 0), game.getColor(), this.game);
            yx_rows[i] = new AIRow(game.getYXRow(i, 0), game.getColor(), this.game);
        }

        int row_index = 14;
        for (int i = 0; i < 14; i++) {
            row_index++;
            xy_rows[row_index] = new AIRow(game.getXYRow(0, i), game.getColor(), this.game);
            yx_rows[row_index] = new AIRow(game.getYXRow(14, i), game.getColor(), this.game);
        }

        for (int i = 0; i < 15; i++) {
            x_rows[i].checkPoints();
            y_rows[i].checkPoints();
            xy_rows[i].checkPoints();
            yx_rows[i].checkPoints();
        }

        for (int i = 16; i < 29; i++) {
            xy_rows[i].checkPoints();
            yx_rows[i].checkPoints();
        }
    }

    private void sumOverlapPoint() {
        Stone[][] plate = game.getPlate();
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                Stone stone = plate[x][y];
                if (stone.getColor() == this.game.getPlayerColor()) { //플레이어
                    if ((stone.getFourCount(this.game.getPlayerColor()) > 0) && (stone.getThreeCount(this.game.getPlayerColor()) > 0)) { //4-3
                        game.addPoint(stone.getX(), stone.getY(), AISetting.PLAYER_MAKE_4_3_POINT);
                    } else if (stone.getFourCount(this.game.getPlayerColor()) > 1) { //4-4
                        game.addPoint(stone.getX(), stone.getY(), AISetting.PLAYER_MAKE_4_4_POINT);
                    }
                } else { //인공지능
                    if ((stone.getFourCount(!this.game.getPlayerColor()) > 0) && (stone.getThreeCount(!this.game.getPlayerColor()) > 0)) { //4-3
                        game.addPoint(stone.getX(), stone.getY(), AISetting.MAKE_4_3_POINT);
                    } else if (stone.getFourCount(!this.game.getPlayerColor()) > 1) { //4-4
                        game.addPoint(stone.getX(), stone.getY(), AISetting.MAKE_4_4_POINT);
                    }
                }
            }
        }
    }

    private void addDotPoint(Stone stone, int point) {
        Stone[][] plate = game.getPlate();
        for (int in_x = 0; in_x < 3; in_x++) {
            for (int in_y = 0; in_y < 3; in_y++) {
                int n_x = stone.getX()+in_x-1;
                int n_y = stone.getY()+in_y-1;
                if (Pos.checkSize(n_x, n_y)) {
                    Stone n_stone = plate[n_x][n_y];
                    n_stone.addPoint(point);
                    plate[n_x][n_y] = n_stone;
                }
            }
        }

        game.setPlate(plate);
    }

    private Pos getMax() {
        Stone[][] plate = game.getPlate();

        int score_max = 0;
        List<Stone> max_stones = new ArrayList<>();

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                Stone stone = plate[x][y];
                if (!stone.isStoneAdded())
                    if (stone.getPoint() > score_max) {
                        score_max = stone.getPoint();
                        max_stones.clear();
                        max_stones.add(stone);
                    } else if (stone.getPoint() == score_max) {
                        max_stones.add(stone);
                    }
            }
        }

        Stone rs_stone;
        if (max_stones.size() == 1) {
            rs_stone = max_stones.get(0);
        } else {
            int rand = new Random().nextInt(max_stones.size());
            rs_stone = max_stones.get(rand);
        }
        return new Pos(rs_stone.getX(), rs_stone.getY());
    }

}
