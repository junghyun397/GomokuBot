package junghyun.ai.engin;

import junghyun.ai.Game;
import junghyun.ai.Pos;
import junghyun.ai.Stone;

class VTChecker {

    private Game game;

    private Pos vtPos;
    private boolean isFind;
    private int countPath;

    VTChecker(Game game) {
        this.game = game;

        this.vtPos = null;
        this.isFind = false;
        this.countPath = 0;
    }

    void sumVTPos() {
        try {
            this.bootVTChecker();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        if (vtPos != null) this.game.getPlate()[this.vtPos.getX()][this.vtPos.getY()].addPoint(AISetting.MAKE_VT_POINT);
    }

    private void bootVTChecker() throws CloneNotSupportedException {
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                Stone targetStone = this.game.getPlate()[x][y];
                if ((targetStone.getThreeCount(!this.game.getPlayerColor()) > 0)
                        || (targetStone.getFourCount(!this.game.getPlayerColor()) > 0)) {
                    Game nGame = this.game.deepCopy();
                    nGame.setStone(x, y);
                    this.findDefensePoint(x, y, nGame, false);
                }
            }
        }
    }

    private void findDefensePoint(int topX, int topY, Game bGame, boolean hasThree) throws CloneNotSupportedException {
        this.countPath++;
        if (this.isFind || this.countPath > AISetting.MAX_VT_PATH) return;
        bGame.resetAllPoint();
        this.sumRowPoints(bGame);

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                Stone targetStone = this.game.getPlate()[x][y];
                if ((targetStone.getFiveCount(!this.game.getPlayerColor()) > 0)
                    || (targetStone.getOpenFourCount(!this.game.getPlayerColor()) > 0)) {
                    if (targetStone.getFourCount(this.game.getPlayerColor()) > 0) return;
                    else if (targetStone.getThreeCount(this.game.getPlayerColor()) > 0) hasThree = true;

                    Game nGame = bGame.deepCopy();
                    nGame.setStone(x, y);
                    this.findAttackPoint(topX, topY, nGame, hasThree);
                }
            }
        }
    }

    private void findAttackPoint(int topX, int topY, Game bGame, boolean hasThree) throws CloneNotSupportedException {
        this.countPath++;
        if (this.isFind || this.countPath > AISetting.MAX_VT_PATH) return;
        bGame.resetAllPoint();
        this.sumRowPoints(bGame);
        this.countPath++;

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                Stone targetStone = this.game.getPlate()[x][y];

                int countThree = targetStone.getThreeCount(!this.game.getPlayerColor());
                int countFour = targetStone.getFourCount(!this.game.getPlayerColor());
                if (hasThree) countThree = 0;

                if (countThree + countFour > 1) {
                    this.vtPos = new Pos(topX, topY);
                    this.isFind = true;
                    return;
                } else if (countThree + countFour > 0) {
                    Game nGame = bGame.deepCopy();
                    nGame.setStone(x, y);
                    this.findDefensePoint(topX, topY, nGame, hasThree);
                }
            }
        }
    }

    private void sumRowPoints(Game nGame) {
        AIRow[] x_rows = new AIRow[15];
        AIRow[] y_rows = new AIRow[15];

        AIRow[] xy_rows = new AIRow[29];
        AIRow[] yx_rows = new AIRow[29];

        for (int i = 0; i < 15; i++) {
            x_rows[i] = new AIRow(nGame.getXRow(i), !nGame.getPlayerColor(), nGame);
            y_rows[i] = new AIRow(nGame.getYRow(i), !nGame.getPlayerColor(), nGame);
            xy_rows[i] = new AIRow(nGame.getXYRow(i, 0), !nGame.getPlayerColor(), nGame);
            yx_rows[i] = new AIRow(nGame.getYXRow(i, 0), !nGame.getPlayerColor(), nGame);
        }

        int row_index = 14;
        for (int i = 0; i < 14; i++) {
            row_index++;
            xy_rows[row_index] = new AIRow(nGame.getXYRow(0, i), !nGame.getPlayerColor(), nGame);
            yx_rows[row_index] = new AIRow(nGame.getYXRow(14, i), !nGame.getPlayerColor(), nGame);
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
}
