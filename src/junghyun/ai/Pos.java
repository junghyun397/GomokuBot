package junghyun.ai;

public class Pos {

    private int X;
    private int Y;

    private char humX;
    private int humY;

    public Pos(int x, int y) {
        this.X = x;
        this.Y = y;

        this.humX = Pos.intToEng(this.X);
        this.humY = this.Y +1;
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

    public String getHumText() {
        return "[ " + Character.toUpperCase(this.humX) + " " + this.humY + " ]";
    }

}
