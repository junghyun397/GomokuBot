package junghyun;

import junghyun.ai.Game;
import junghyun.ai.Pos;
import junghyun.ai.engin.AIAgent;
import junghyun.discord.ui.graphics.TextDrawer;

import java.util.Scanner;

public class AIDemo {

    private static Scanner scanner;

    private static Game game;

    private static final String clearText = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";

    private static AIAgent.DIFF diff = AIAgent.DIFF.MID;
    private static boolean useGrid = true;
    private static boolean usePrintStep = true;

    public static void main(String[] args) {
        AIDemo.scanner = new Scanner(System.in);
        AIDemo.setupSettings();
    }

    private static void setupSettings() {
        System.out.print("0: Easy 1: Normal 2: Extreme\n");
        System.out.print("Select Difficulty: ");
        int diff = Integer.parseUnsignedInt(AIDemo.scanner.nextLine());
        if (diff == 0) AIDemo.diff = AIAgent.DIFF.EAS;
        else if (diff == 2) AIDemo.diff = AIAgent.DIFF.EXT;
        else AIDemo.diff = AIAgent.DIFF.MID;
        System.out.print("\n");

        System.out.print("0: Use grid 1: Do not use grid\n");
        System.out.print("Select use Grid: ");
        int grid = Integer.parseUnsignedInt(AIDemo.scanner.nextLine());
        if (grid == 1) AIDemo.useGrid = false;
        System.out.print("\n");

        System.out.print("0: Print EVE step 1: Do not print EVE step\n");
        System.out.print("Select use print EVE step: ");
        int printStep = Integer.parseUnsignedInt(AIDemo.scanner.nextLine());
        if (printStep == 1) AIDemo.usePrintStep = false;
        System.out.print("\n");

        AIDemo.selectGameType();
    }

    private static void selectGameType() {
        System.out.print("0: PvE 1: EvE\n");
        System.out.print("Select Type: ");
        int type = Integer.parseUnsignedInt(AIDemo.scanner.nextLine());
        System.out.print("\n");

        if (type == 0) AIDemo.startPlayerGame();
        else AIDemo.startAIGame();
    }

    private static void startAIGame() {
        AIDemo.game = new Game();

        System.out.print("Count Loops: ");
        int loops = Integer.parseUnsignedInt(AIDemo.scanner.nextLine());
        System.out.print("\n");

        AIDemo.loopAIGame(loops);
    }

    private static void startPlayerGame() {
        AIDemo.game = new Game();

        System.out.print("0: Black 1: White\n");
        System.out.print("Select Color: ");
        int color = Integer.parseUnsignedInt(AIDemo.scanner.nextLine());
        System.out.print("\n");

        AIDemo.game.setPlayerColor(color == 0);

        if (!AIDemo.game.getPlayerColor()) AIDemo.game.setStone(7, 7);

        AIDemo.printState();
        AIDemo.loopPlayer();
    }

    private static void loopAIGame(int loops) {
        int winBlack = 0;
        int winWhite = 0;
        int fullCount = 0;
        for (int i = 0; i < loops; i++) {
            AIDemo.game = new Game();
            AIDemo.game.setStone(7, 7);
            AIDemo.game.setPlayerColor(true);

            while (true) {
                AIDemo.game.setPlayerColor(!AIDemo.game.getPlayerColor());
                Pos aiPos = new AIAgent(AIDemo.game, AIDemo.diff).getAiPoint();
                AIDemo.game.setStone(aiPos.getX(), aiPos.getY());
                if (AIDemo.usePrintStep) AIDemo.printState(aiPos);

                if (AIDemo.game.isWin(aiPos.getX(), aiPos.getY(), AIDemo.game.getPlayerColor())) {
                    String rsColor = "WHITE";
                    if (AIDemo.game.getPlayerColor()) {
                        rsColor = "BLACK";
                        winBlack++;
                    } else winWhite++;

                    AIDemo.printState(aiPos);
                    System.out.print("#" + (i + 1) + " " + rsColor + " Victory, " + "End. \n\n");
                    break;
                }

                if (AIDemo.game.isFull()) {
                    fullCount++;
                    AIDemo.printState(aiPos);
                    System.out.print("#" + (i + 1) + " Full. \n\n");
                    break;
                }
            }
        }
        System.out.print("End Simulation. V.Black: " + winBlack + " V.White: " + winWhite + " Full: " + fullCount + "\n\n\n");
        AIDemo.selectGameType();
    }

    private static void loopPlayer() {
        while (true) {
            String[] str;
            Pos pos;

            try {
                str = AIDemo.scanner.nextLine().split(" ");
                if (str[0].equals("flip")) {
                    AIDemo.game.setPlayerColor(!AIDemo.game.getPlayerColor());
                    Pos aiPos = new AIAgent(AIDemo.game, AIDemo.diff).getAiPoint();
                    AIDemo.game.setStone(aiPos.getX(), aiPos.getY(), !AIDemo.game.getPlayerColor());
                    AIDemo.printState(aiPos);
                    AIDemo.loopPlayer();
                    return;
                }

                pos = new Pos(Pos.engToInt(str[0].toCharArray()[0]), Integer.parseUnsignedInt(str[1]) - 1);
            } catch (Exception e) {
                System.out.print("Error. \n");
                AIDemo.loopPlayer();
                return;
            }

            if (!AIDemo.game.canSetStone(pos.getX(), pos.getY())) {
                System.out.print("Error. \n");
                AIDemo.loopPlayer();
                return;
            }

            AIDemo.game.setStone(pos.getX(), pos.getY());
            if (AIDemo.game.isWin(pos.getX(), pos.getY(), !AIDemo.game.getPlayerColor())) {
                System.out.print("Player Win. \n");
                AIDemo.selectGameType();
                return;
            }

            if (AIDemo.game.isFull()) {
                System.out.print("Full. \n");
                AIDemo.selectGameType();
                return;
            }

            Pos aiPos = new AIAgent(AIDemo.game, AIDemo.diff).getAiPoint();
            AIDemo.game.setStone(aiPos.getX(), aiPos.getY(), !AIDemo.game.getPlayerColor());
            if (AIDemo.game.isWin(aiPos.getX(), aiPos.getY(), !AIDemo.game.getPlayerColor())) {
                System.out.print("AI Win. \n");
                AIDemo.selectGameType();
                return;
            }

            printState(aiPos);
        }
    }

    private static void printState() {
        System.out.print(AIDemo.clearText + TextDrawer.getConsoleGraphics(AIDemo.game, AIDemo.useGrid) + "\n");
    }

    private static void printState(Pos aiPos) {
        System.out.print(TextDrawer.getConsoleGraphics(AIDemo.game, aiPos, AIDemo.useGrid) + "\n");
    }

}
