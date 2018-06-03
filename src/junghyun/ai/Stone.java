package junghyun.ai;

public class Stone {

    private int x;
    private int y;

    private int point = 0;

    private boolean isStoneAdded = false;

    private boolean color = true; //true = 검정

    private int blackThreeCount = 0;
    private int blackFourCount = 0;

    private int whiteThreeCount = 0;
    private int whiteFourCount = 0;

    public Stone(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //착수

    public boolean isStoneAdded() {
        return this.isStoneAdded;
    }

    public void setStone(boolean is_black) {
        this.isStoneAdded = true;
        this.color = is_black;
    }

    public boolean getColor() {
        return this.color;
    }

    //좌표

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    //3, 4연산

    public void addThreeCount(boolean color) {
        if (color) this.blackThreeCount++;
        else this.whiteThreeCount++;
    }

    public int getThreeCount(boolean color) {
        if (color) return this.blackThreeCount;
        else return this.whiteThreeCount;
    }

    public void addFourCount(boolean color) {
        if (color) this.blackFourCount++;
        else this.whiteFourCount++;
    }

    public int getFourCount(boolean color) {
        if (color) return this.blackFourCount;
        else return this.whiteFourCount;
    }

    //평가

    public void addPoint(int point) {
        this.point += point;
    }

    public void delPoint(int point) {
        this.point -= point;
    }

    public void resetPoint() {
        this.blackThreeCount = 0;
        this.blackFourCount = 0;
        this.whiteThreeCount = 0;
        this.whiteFourCount = 0;

        this.point = 0;
    }

    public int getPoint() {
        return this.point;
    }

}
