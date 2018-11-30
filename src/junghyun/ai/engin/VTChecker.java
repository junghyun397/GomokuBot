package junghyun.ai.engin;

import junghyun.ai.Game;
import junghyun.unit.Pos;

class VTChecker {

    private Game game;

    private Pos vtPos;
    private boolean isFind;

    VTChecker(Game game) {
        this.game = game;

        this.vtPos = null;
        this.isFind = false;
    }

    void sumVTPos() {
        try {
            this.bootVTChecker();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        this.game.addPoint(this.vtPos.getX(), this.vtPos.getY(), AISetting.MAKE_VT_POINT);
    }

    private void bootVTChecker() throws CloneNotSupportedException {
        Game nGame = this.game.clone();
        this.sumRowPoints(nGame);

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                if ((nGame.getPlate()[x][y].getFourCount(!nGame.getPlayerColor()) > 0) ||
                        (nGame.getPlate()[x][y].getThreeCount(!nGame.getPlayerColor()) > 0)) {
                    nGame.setStone(x, y);
                    this.vtPos = new Pos(x, y);
                    this.findDefendPoint();
                }
            }
        }
    }

    private void findAttackPoint() throws CloneNotSupportedException {
        if (this.isFind) return;

        Game nGame = this.game.clone();
        this.sumRowPoints(nGame);

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                if (((nGame.getPlate()[x][y].getFourCount(!nGame.getPlayerColor()) > 0) &&
                        (nGame.getPlate()[x][y].getThreeCount(!nGame.getPlayerColor()) > 0)) ||
                        (nGame.getPlate()[x][y].getFourCount(!nGame.getPlayerColor()) > 1)) {
                    this.isFind = true;
                    return;
                } else if ((nGame.getPlate()[x][y].getFourCount(!nGame.getPlayerColor()) > 0) ||
                        (nGame.getPlate()[x][y].getThreeCount(!nGame.getPlayerColor()) > 0)) {
                    nGame.setStone(x, y);
                    this.findDefendPoint();
                }
            }
        }
    }

    private void findDefendPoint() throws CloneNotSupportedException {
        if (this.isFind) return;

        Game nGame = this.game.clone();
        this.sumRowPoints(nGame);

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                if ((nGame.getPlate()[x][y].getFourCount(nGame.getPlayerColor()) > 0) ||
                        (nGame.getPlate()[x][y].getFiveCount(nGame.getPlayerColor()) > 0)) {
                    return;
                } else if ((nGame.getPlate()[x][y].getFourCount(!nGame.getPlayerColor()) > 0) ||
                        (nGame.getPlate()[x][y].getFiveCount(!nGame.getPlayerColor()) > 0)) {
                    nGame.setStone(x, y);
                    this.findAttackPoint();
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
            x_rows[i] = new AIRow(nGame.getXRow(i), nGame.getColor(), this.game);
            y_rows[i] = new AIRow(nGame.getYRow(i), nGame.getColor(), this.game);
            xy_rows[i] = new AIRow(nGame.getXYRow(i, 0), nGame.getColor(), this.game);
            yx_rows[i] = new AIRow(nGame.getYXRow(i, 0), nGame.getColor(), this.game);
        }

        int row_index = 14;
        for (int i = 0; i < 14; i++) {
            row_index++;
            xy_rows[row_index] = new AIRow(nGame.getXYRow(0, i), nGame.getColor(), this.game);
            yx_rows[row_index] = new AIRow(nGame.getYXRow(14, i), nGame.getColor(), this.game);
        }

        for (int i = 0; i < 15; i++) {
            x_rows[i].checkADPoints();
            y_rows[i].checkADPoints();
            xy_rows[i].checkADPoints();
            yx_rows[i].checkADPoints();
        }

        for (int i = 16; i < 29; i++) {
            xy_rows[i].checkADPoints();
            yx_rows[i].checkADPoints();
        }
    }
}
