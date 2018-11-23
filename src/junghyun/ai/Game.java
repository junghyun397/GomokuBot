package junghyun.ai;

import junghyun.unit.Pos;

import java.util.ArrayList;

public class Game {

    private boolean playerColor = true;

    private int turns = 1;
    private boolean turnColor = true;

    private Stone[][] plate = new Stone[15][15];

    private ArrayList<Pos> log = new ArrayList<>();

    public Game() {
        this.clearPlate();
    }

    public boolean getPlayerColor() {
        return this.playerColor;
    }

    public void setPlayerColor(boolean color) {
        this.playerColor = color;
    }

    private void clearPlate() {
        for (byte x = 0; x < 15 ; x++) {
            for (byte y = 0; y < 15 ; y++) {
                this.plate[x][y] = new Stone(x, y);
            }
        }
    }

    public void setPlate(Stone[][] plate) {
        this.plate = plate;
    }

    public Stone[][] getPlate() {
        return this.plate;
    }

    public int getTurns() {
        return this.turns;
    }

    private void setColor(boolean color) {
        this.turnColor = color;
    }

    public boolean getColor() {
        return this.turnColor;
    }

    public Row getXRow(int x) {
        return new Row(this.plate[x]);
    }

    public Row getYRow(int y) {
        Stone[] rows = new Stone[15];
        for (int i = 0; i < 15; i++) {
            rows[i] = this.plate[i][y];
        }
        return new Row(rows);
    }

    public Row getXYRow(int x, int y) {
        int tempLength = y-x;
        int rowx, rowy, length;

        if (tempLength > -1) { //양수
            rowx = 0;
            rowy = tempLength;
            length = 15-rowy;
        } else { //음수
            rowx = Math.abs(y-x);
            rowy = 0;
            length = 15-rowx;
        }

        Stone[] row = new Stone[length];

        for (int i = 0; i < length; i++) {
            row[i] = this.plate[rowx][rowy];
            rowx++;
            rowy++;
        }
        return new Row(row);
    }

    public Row getYXRow(int x, int y) {
        int tempLength = y+x;
        int rowx, rowy, length;

        if (tempLength < 15) {
            rowx = tempLength;
            rowy = 0;
            length = Math.abs(tempLength)+1;
        } else {
            rowx = 14;
            rowy = Math.abs(tempLength-14);
            length = 15-rowy;
        }

        Stone[] row = new Stone[length];

        for (int i = 0; i < length; i++) {
            row[i] = this.plate[rowx][rowy];
            rowx--;
            rowy++;
        }
        return new Row(row);
    }

    public boolean canSetStone(int x, int y) {
        if (!((x > -1) && (x < 15) && (y > -1) && (y < 15))) {
            return false;
        } else return !this.plate[x][y].isStoneAdded();
    }

    public boolean isWin(int x, int y, boolean color) {
        return this.getXRow(x).findFive(color) || this.getYRow(y).findFive(color)
                || this.getXYRow(x, y).findFive(color) || this.getYXRow(x, y).findFive(color);
    }

    public void setStone(int x, int y) {
        this.setStone(x, y, this.turnColor);
    }

    public void setStone(int x, int y, boolean color) {
        Stone stone = this.plate[x][y];
        stone.setStone(color);
        this.plate[x][y] = stone;
        this.turns++;
        this.setColor(!color);

        this.log.add(new Pos(x, y));
    }

    public void addThreePoint(int x, int y, boolean color) {
        Stone stone = this.plate[x][y];
        stone.addThreeCount(color);
        this.plate[x][y] = stone;
    }

    public void addFourPoint(int x, int y, boolean color) {
        Stone stone = this.plate[x][y];
        stone.addFourCount(color);
        this.plate[x][y] = stone;
    }

    public void addPoint(int x, int y, int point) {
        Stone stone = this.plate[x][y];
        stone.addPoint(point);
        this.plate[x][y] = stone;
    }

    private void resetPoint(int x, int y) {
        Stone stone = this.plate[x][y];
        stone.resetPoint();
        this.plate[x][y] = stone;
    }

    public void resetAllPoint() {
        for (byte x = 0; x < 15 ; x++) {
            for (byte y = 0; y < 15 ; y++) {
                this.resetPoint(x, y);
            }
        }
    }

    public ArrayList<Pos> getLog() {
        return this.log;
    }

    public boolean isFull() {
        int count = 0;
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                if (this.plate[x][y].isStoneAdded()) {
                    count++;
                }
            }
        }

        return (count == 225) || (count == 224);
    }

}
