package junghyun.unit;

public class Pos {

    private int X = 0;
    private int Y = 0;

    private char humX = 0;
    private int humY = 0;

    private String errorStr = null;
    private boolean isError = false;

    public Pos(int x, int y) {
        this.X = x;
        this.Y = y;

        this.humX = Pos.intToEng(this.X);
        this.humY = this.Y +1;
    }

    public Pos(String str) {
        this.isError = true;
        this.errorStr = str;
    }

    public static int engToInt(char str) {
        return str-97;
    }

    private static char intToEng(int inter) {
        return (char) ((char) inter+97);
    }

    public static boolean checkSize(int x, int y) {
        return (x >= 0) && (x <= 14) && (y >= 0) && (y <= 14);
    }

    public int getX() {
        return this.X;
    }

    public int getY() {
        return this.Y;
    }

    public char getHumX() {
        return this.humX;
    }

    public int getHumY() {
        return this.humY;
    }

    public boolean isError() {
        return this.isError;
    }

    public String getErrorStr() {
        return this.errorStr;
    }

}
