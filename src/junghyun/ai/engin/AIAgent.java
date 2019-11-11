package junghyun.ai.engin;

import junghyun.ai.Game;
import junghyun.ai.Pos;
import junghyun.ai.Stone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIAgent {

    final private Game game;

    public enum DIFF {EAS, MID, EXT}
    final private DIFF diff;

    final private Random random;

    final private AIRow[] xRows = new AIRow[15];
    final private AIRow[] yRows = new AIRow[15];
    final private AIRow[] xyRows = new AIRow[29];
    final private AIRow[] yxRows = new AIRow[29];

    public AIAgent(Game game, DIFF diff) {
        this.game = game;
        this.diff = diff;
        this.random = new Random();

        this.initAIRows();
    }

    public Pos getAiPoint() {
        this.sumPoint();
        return this.getMax();
    }

    private void initAIRows() {
        for (int i = 0; i < 15; i++) {
            xRows[i] = new AIRow(game.getXRow(i), game.getColor(), this.game);
            yRows[i] = new AIRow(game.getYRow(i), game.getColor(), this.game);
            xyRows[i] = new AIRow(game.getXYRow(i, 0), game.getColor(), this.game);
            yxRows[i] = new AIRow(game.getYXRow(i, 0), game.getColor(), this.game);
        }

        for (int i = 0; i < 14; i++) {
            xyRows[i+15] = new AIRow(game.getXYRow(0, i), game.getColor(), this.game);
            yxRows[i+15] = new AIRow(game.getYXRow(14, i), game.getColor(), this.game);
        }
    }

    private void sumRowPoints() {
        for (int i = 0; i < 15; i++) {
            xRows[i].checkPoints();
            yRows[i].checkPoints();
            xyRows[i].checkPoints();
            yxRows[i].checkPoints();
        }

        for (int i = 16; i < 29; i++) {
            xyRows[i].checkPoints();
            yxRows[i].checkPoints();
        }
    }


    private void sumPoint() {
        game.resetAllPoint();

        this.sumDotPoints();
        this.sumRowPoints();

        if ((this.diff == DIFF.MID) || (this.diff == DIFF.EXT)) this.sumOverlapPoint();
        if (this.diff == DIFF.EXT) new VTChecker(game).sumVTPos();
    }

    private void sumDotPoints() {
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                Stone stone = game.getPlate()[x][y];
                if (stone.isStoneAdded()) {
                    if (stone.getColor() == game.getColor()) {
                        this.addDotPoint(stone, AISetting.DEF_AI_POINT);
                    } else if (stone.getColor() != game.getColor()) {
                        this.addDotPoint(stone, AISetting.DEF_PLAYER_POINT);
                    }
                }
            }
        }
    }

    private void sumOverlapPoint() {
        Stone[][] plate = game.getPlate();
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                Stone stone = plate[x][y];
                if (stone.getColor() == this.game.getPlayerColor()) {
                    if ((stone.getFourCount(this.game.getPlayerColor()) > 0)
                            && (stone.getThreeCount(this.game.getPlayerColor()) > 0)) { //4-3
                        this.game.getPlate()[x][y].addPoint(AISetting.PLAYER_MAKE_4_3_POINT);
                    } else if (stone.getFourCount(this.game.getPlayerColor()) > 1) { //4-4
                        this.game.getPlate()[x][y].addPoint(AISetting.PLAYER_MAKE_4_4_POINT);
                    }
                } else {
                    if ((stone.getFourCount(!this.game.getPlayerColor()) > 0)
                            && (stone.getThreeCount(!this.game.getPlayerColor()) > 0)) { //4-3
                        this.game.getPlate()[x][y].addPoint(AISetting.MAKE_4_3_POINT);
                    } else if (stone.getFourCount(!this.game.getPlayerColor()) > 1) { //4-4
                        this.game.getPlate()[x][y].addPoint(AISetting.MAKE_4_4_POINT);
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
                if (!stone.isStoneAdded()) {
                    if (stone.getPoint() > score_max) {
                        score_max = stone.getPoint();
                        max_stones.clear();
                        max_stones.add(stone);
                    } else if (stone.getPoint() == score_max) {
                        max_stones.add(stone);
                    }
                }
            }
        }

        Stone rs_stone;
        if (max_stones.size() == 1) {
            rs_stone = max_stones.get(0);
        } else {
            int rand = random.nextInt(max_stones.size());
            rs_stone = max_stones.get(rand);
        }
        return new Pos(rs_stone.getX(), rs_stone.getY());
    }

}
