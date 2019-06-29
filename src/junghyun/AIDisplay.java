package junghyun;

import junghyun.ai.Game;
import junghyun.ai.Pos;
import junghyun.ai.engin.AIBase;
import junghyun.discord.ui.graphics.TextDrawer;

import java.util.Scanner;

public class AIDisplay {

    private static Scanner scanner;

    private static Game game;

    private static final String clearText = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";

    private static AIBase.DIFF diff = AIBase.DIFF.MID;
    private static boolean useGrid = true;
    private static boolean usePrintStep = true;

    public static void main(String[] args) {
        AIDisplay.scanner = new Scanner(System.in);
        AIDisplay.setupSettings();
    }

    private static void setupSettings() {
        System.out.print("0: Easy 1: Normal 2: Extreme\n");
        System.out.print("Select Difficulty: ");
        int diff = Integer.valueOf(AIDisplay.scanner.nextLine());
        if (diff == 0) AIDisplay.diff = AIBase.DIFF.EAS;
        else if (diff == 2) AIDisplay.diff = AIBase.DIFF.EXT;
        else AIDisplay.diff = AIBase.DIFF.MID;
        System.out.print("\n");

        System.out.print("0: Use grid 1: Do not use grid\n");
        System.out.print("Select use Grid: ");
        int grid = Integer.valueOf(AIDisplay.scanner.nextLine());
        if (grid == 1) AIDisplay.useGrid = false;
        System.out.print("\n");

        System.out.print("0: Print EVE step 1: Do not print EVE step\n");
        System.out.print("Select use print EVE step: ");
        int printStep = Integer.valueOf(AIDisplay.scanner.nextLine());
        if (printStep == 1) AIDisplay.usePrintStep = false;
        System.out.print("\n");

        AIDisplay.selectGameType();
    }

    private static void selectGameType() {
        System.out.print("0: PvE 1: EvE\n");
        System.out.print("Select Type: ");
        int type = Integer.valueOf(AIDisplay.scanner.nextLine());
        System.out.print("\n");

        if (type == 0) AIDisplay.startPlayerGame();
        else AIDisplay.startAIGame();
    }

    private static void startAIGame() {
        AIDisplay.game = new Game();

        System.out.print("Count Loops: ");
        int loops = Integer.valueOf(AIDisplay.scanner.nextLine());
        System.out.print("\n");

        AIDisplay.loopAIGame(loops);
    }

    private static void startPlayerGame() {
        AIDisplay.game = new Game();

        System.out.print("0: Black 1: White\n");
        System.out.print("Select Color: ");
        int color = Integer.valueOf(AIDisplay.scanner.nextLine());
        System.out.print("\n");

        if (color == 0) AIDisplay.game.setPlayerColor(true);
        else AIDisplay.game.setPlayerColor(false);

        if (!AIDisplay.game.getPlayerColor()) AIDisplay.game.setStone(7, 7);

        AIDisplay.printState();
        AIDisplay.loopPlayer();
    }

    private static void loopAIGame(int loops) {
        int winBlack = 0;
        int winWhite = 0;
        int fullCount = 0;
        for (int i = 0; i < loops; i++) {
            AIDisplay.game = new Game();
            AIDisplay.game.setStone(7, 7);
            AIDisplay.game.setPlayerColor(true);

            while (true) {
                AIDisplay.game.setPlayerColor(!AIDisplay.game.getPlayerColor());
                Pos aiPos = new AIBase(AIDisplay.game, AIDisplay.diff).getAiPoint();
                AIDisplay.game.setStone(aiPos.getX(), aiPos.getY());
                if (AIDisplay.usePrintStep) AIDisplay.printState(aiPos);

                if (AIDisplay.game.isWin(aiPos.getX(), aiPos.getY(), AIDisplay.game.getPlayerColor())) {
                    String rsColor = "BLACK";
                    if (AIDisplay.game.getPlayerColor()) {
                        rsColor = "WHITE";
                        winWhite++;
                    } else winBlack++;

                    AIDisplay.printState(aiPos);
                    System.out.print("#" + (i + 1) + " " + rsColor + " Victory, " + "End. \n\n");
                    break;
                }

                if (AIDisplay.game.isFull()) {
                    fullCount++;
                    AIDisplay.printState(aiPos);
                    System.out.print("#" + (i + 1) + " Full. \n\n");
                    break;
                }
            }
        }
        System.out.print("End Simulation. V.Black: " + winBlack + " V.White: " + winWhite + " Full: " + fullCount + "\n\n\n");
        AIDisplay.selectGameType();
    }

    private static void loopPlayer() {
        while (true) {
            String[] str;
            Pos pos;

            try {
                str = AIDisplay.scanner.nextLine().split(" ");
                if (str[0].equals("flip")) {
                    AIDisplay.game.setPlayerColor(!AIDisplay.game.getPlayerColor());
                    Pos aiPos = new AIBase(AIDisplay.game, AIDisplay.diff).getAiPoint();
                    AIDisplay.game.setStone(aiPos.getX(), aiPos.getY(), !AIDisplay.game.getPlayerColor());
                    AIDisplay.printState(aiPos);
                    AIDisplay.loopPlayer();
                    return;
                }

                pos = new Pos(Pos.engToInt(str[0].toCharArray()[0]), Integer.valueOf(str[1]) - 1);
            } catch (Exception e) {
                System.out.print("Error. \n");
                AIDisplay.loopPlayer();
                return;
            }

            if (!AIDisplay.game.canSetStone(pos.getX(), pos.getY())) {
                System.out.print("Error. \n");
                AIDisplay.loopPlayer();
                return;
            }

            AIDisplay.game.setStone(pos.getX(), pos.getY());
            if (AIDisplay.game.isWin(pos.getX(), pos.getY(), !AIDisplay.game.getPlayerColor())) {
                System.out.print("Player Win. \n");
                AIDisplay.selectGameType();
                return;
            }

            if (AIDisplay.game.isFull()) {
                System.out.print("Full. \n");
                AIDisplay.selectGameType();
                return;
            }

            Pos aiPos = new AIBase(AIDisplay.game, AIDisplay.diff).getAiPoint();
            AIDisplay.game.setStone(aiPos.getX(), aiPos.getY(), !AIDisplay.game.getPlayerColor());
            if (AIDisplay.game.isWin(aiPos.getX(), aiPos.getY(), !AIDisplay.game.getPlayerColor())) {
                System.out.print("AI Win. \n");
                AIDisplay.selectGameType();
                return;
            }

            printState(aiPos);
        }
    }

    private static void printState() {
        System.out.print(AIDisplay.clearText + TextDrawer.getGraphics(AIDisplay.game, AIDisplay.useGrid) + "\n");
    }

    private static void printState(Pos aiPos) {
        System.out.print(TextDrawer.getGraphics(AIDisplay.game, aiPos, AIDisplay.useGrid) + "\n");
    }

}
