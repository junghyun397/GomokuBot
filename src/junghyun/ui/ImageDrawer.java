package junghyun.ui;

import junghyun.ai.Game;
import junghyun.ai.Stone;
import junghyun.unit.Pos;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageDrawer {

    public static File getGraphics(Game game) {
        return getGraphics(game, new Pos(-1, -1));
    }

    public static File getGraphics(Game game, Pos aiPos) {
        BufferedImage image = new BufferedImage(600,600, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.createGraphics();

        Stone[][] plate = game.getPlate();
        int pieceSize = (int) Math.floor(600/15);
        int pieceHalfSIze = (int) Math.floor(pieceSize/2);

        ((Graphics2D) graphics).setBackground(Color.DARK_GRAY);

        graphics.setColor(Color.BLACK);
//        for (int x = 0; x < 15; x++) graphics.drawLine(pieceSize*x+ pieceHalfSIze, pieceHalfSIze, pieceSize*x+ pieceHalfSIze, 600 - pieceHalfSIze);
//        for (int y = 0; y < 15; y++) graphics.drawLine(pieceHalfSIze, pieceSize*y+ pieceHalfSIze, 600 - pieceHalfSIze, pieceSize*y+ pieceHalfSIze);

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                Stone stone = plate[x][y];
                if (stone.isStoneAdded()) {
                    if (stone.getColor()) graphics.setColor(Color.BLACK);
                    else graphics.setColor(Color.LIGHT_GRAY);

                    graphics.fillOval(pieceSize*x, pieceSize*y, pieceSize, pieceSize);

                    if ((aiPos.getX() == x) && (aiPos.getY() == y)) {
                        graphics.setColor(Color.WHITE);
                        graphics.fillOval(pieceSize*x-pieceHalfSIze, pieceSize*y-pieceHalfSIze, pieceSize/10, pieceSize/10);
                    }

                }
            }
        }

        File outputFile = new File("GomokuBot_img.jpg");
        try {
            ImageIO.write(image, "jpg", outputFile);
        } catch (IOException e) {
            return null;
        }
        return outputFile;
    }

}
