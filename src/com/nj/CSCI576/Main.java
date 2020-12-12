package com.nj.CSCI576;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        ReadImage readImage = new ReadImage();
        SwingUtilities.invokeLater(() -> readImage.showIms(args));
    }
}