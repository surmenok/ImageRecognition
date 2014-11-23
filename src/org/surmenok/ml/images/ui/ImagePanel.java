package org.surmenok.ml.images.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {
    private BufferedImage image;

    public ImagePanel(BufferedImage image) {
        this.image = image;
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if(image != null){
            g.drawImage(image, 0, 0, this);
        }
    }
}
