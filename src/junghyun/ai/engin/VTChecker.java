package junghyun.ai.engin;

import junghyun.ai.Game;
import junghyun.db.Logger;
import junghyun.ui.TextDrawer;
import junghyun.unit.Pos;

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
        if (vtPos != null) this.game.addPoint(this.vtPos.getX(), this.vtPos.getY(), AISetting.MAKE_VT_POINT);
    }

    private void bootVTChecker() throws CloneNotSupportedException {
        Game nGame = this.game.clone();
        this.sumRowPoints(nGame);

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                if (nGame.getPlate()[x][y].isStoneAdded()) continue;
                if ((nGame.getPlate()[x][y].getFourCount(!nGame.getPlayerColor()) > 0) ||
                        (nGame.getPlate()[x][y].getThreeCount(!nGame.getPlayerColor()) > 0)) {
                    Game cGame = nGame.clone();
                    cGame.setStone(x, y);
                    this.findDefendPoint(new Pos(x, y), cGame);
                }
            }
        }
    }

    private void findAttackPoint(Pos topNode, Game nGame, boolean attackThree) throws CloneNotSupportedException {
        this.countPath++;
        if ((this.isFind) || (this.countPath > AISetting.MAX_VT_PATH)) return;

        nGame.resetAllPoint();
        this.sumRowPoints(nGame);

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                if (this.isFind) return;
                if (nGame.getPlate()[x][y].isStoneAdded()) continue;
                if (((nGame.getPlate()[x][y].getFourCount(!nGame.getPlayerColor()) > 0) &&
                        (nGame.getPlate()[x][y].getThreeCount(!nGame.getPlayerColor()) > 0)) ||
                        (nGame.getPlate()[x][y].getFourCount(!nGame.getPlayerColor()) > 1)) {
                    this.isFind = true;
                    this.vtPos = topNode;
                    nGame.setStone(x, y);
                    Logger.loggerDev(TextDrawer.getGraphics(nGame));
                    return;
                } else if ((nGame.getPlate()[x][y].getFourCount(!nGame.getPlayerColor()) > 0) ||
                        (nGame.getPlate()[x][y].getThreeCount(!nGame.getPlayerColor()) > 0)) {
                    if (!((nGame.getPlate()[x][y].getFourCount(!nGame.getPlayerColor()) == 0)
                            && attackThree)) {
                        Game cGame = nGame.clone();
                        cGame.setStone(x, y);
                        this.findDefendPoint(topNode, cGame);
                    }
                }
            }
        }
    }

    private void findDefendPoint(Pos topNode, Game nGame) throws CloneNotSupportedException {
        if (this.isFind) return;

        nGame.resetAllPoint();
        this.sumRowPoints(nGame);

        boolean isThree;
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                if (this.isFind) return;
                if (nGame.getPlate()[x][y].isStoneAdded()) continue;
                if (((nGame.getPlate()[x][y].getFiveCount(!nGame.getPlayerColor()) > 0) ||
                        (nGame.getPlate()[x][y].getOpenFourCount(!nGame.getPlayerColor()) > 0)) &&
                        !((nGame.getPlate()[x][y].getFourCount(nGame.getPlayerColor()) > 0)) ||
                        (nGame.getPlate()[x][y].getOpenFourCount(nGame.getPlayerColor()) > 0)) {
                    isThree = nGame.getPlate()[x][y].getThreeCount(nGame.getPlayerColor()) > 0;
                    Game cGame = nGame.clone();
                    cGame.setStone(x, y);
                    this.findAttackPoint(topNode, cGame, isThree);
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
