package com.nj.CSCI576;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RenderImage extends JPanel
{
    BufferedImage image;

    public RenderImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void paintComponent (Graphics g)
    {
        super.paintComponent (g);
        final Graphics2D g2d = (Graphics2D ) g;
        g2d.drawImage(image, 0, 0, this);
    }

    public void updateImage(BufferedImage newImage) {
        image = newImage;
        repaint();
    }

}
