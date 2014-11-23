package org.surmenok.ml.images;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class Image {
    private BufferedImage image;

    public Image (String fileName) throws IOException {
        //todo: make it disposable
        image = ImageIO.read(new File(fileName));
    }

    public BufferedImage getSourceImage() {
        return image;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public byte[] getRectanglePixels(int x, int y, int sourceWidth, int sourceHeight, int resizeWidth, int resizeHeight) {
        BufferedImage resizedImage = getRectangleImage(x, y, sourceWidth, sourceHeight, resizeWidth, resizeHeight);

        final byte[] pixels = ((DataBufferByte) resizedImage.getRaster().getDataBuffer()).getData();
        return pixels;
    }

    public BufferedImage getRectangleImage(int x, int y, int sourceWidth, int sourceHeight, int resizeWidth, int resizeHeight) {
        BufferedImage resizedImage = new BufferedImage(resizeWidth, resizeHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = resizedImage.createGraphics();

        BufferedImage subImage = image.getSubimage(x, y, sourceWidth, sourceHeight);

        g.drawImage(subImage, 0, 0, resizeWidth, resizeHeight, null);
        g.dispose();
        return resizedImage;
    }

    public double[] getRectanglePixelArray(Rectangle rectangle, int sampleWidth, int sampleHeight) {
        System.out.printf(
                "getRectanglePixelArray\tx: %d\ty: %d\twidth: %d\theight: %d\timage width: %d\timage height: %d\n",
                rectangle.x,
                rectangle.y,
                rectangle.width,
                rectangle.height,
                image.getWidth(),
                image.getHeight());

        byte[] pixels = this.getRectanglePixels(rectangle.x, rectangle.y, rectangle.width, rectangle.height, sampleWidth, sampleHeight);

        double[] doublePixels = new double[pixels.length];
        for(int i = 0; i < pixels.length; i++) {
            doublePixels[i] = pixels[i];
        }

        return doublePixels;
    }
}
