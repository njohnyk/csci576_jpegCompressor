package com.nj.CSCI576;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ReadImage {
    BufferedImage image, newImage;
    int width = 1920;
    int height = 1080;

    String inputImage;
    int quantizationLevel, deliveryMode, latency;
    /** Read Image RGB
     *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
     */
    private void readImageRGB(int width, int height, String filePath) {
        try
        {
            int frameLength = width*height*3;

            String imgPath = filePath;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            int[] pixel = new int[width * height * 3];
            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    pixel[ind] = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    image.setRGB(x,y,pixel[ind]);
                    ind++;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showIms(String[] args){
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        inputImage = args[0];
        quantizationLevel = Integer.parseInt(args[1]);
        deliveryMode = Integer.parseInt(args[2]);
        latency = Integer.parseInt(args[3]);

        if(quantizationLevel < 0 || quantizationLevel > 7) {
            System.out.println("Enter quantizationLevel between 0 and 7");
            return;
        }

        if(deliveryMode < 0 || deliveryMode > 3) {
            System.out.println("Enter deliveryMode between 0 and 3");
            return;
        }

        readImageRGB(width, height, inputImage);



        if(deliveryMode == 1) {
            JFrame frame = new JFrame();
            SequentialMode sequentialMode = new SequentialMode(image, quantizationLevel);
            RenderImage renderImage = new RenderImage(newImage);

            frame.getContentPane().add(renderImage);
            frame.setSize (width, height);
            frame.setVisible (true);

            new Thread (new Runnable() {
                @Override
                public void run() {
                    try {
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                        int index = 0;
                        for(int y = 0; y < height; y+=8) {
                            for(int x = 0; x < width; x+=8) {
                                System.out.println("\n*************** " + "Block # " + index++ + " ***************");
                                System.out.println("Begin");
                                System.out.println("Start Timer: " + dateFormat.format(new Date()));
                                newImage = sequentialMode.getCompressedBlock(y, x);
                                renderImage.updateImage(newImage);
                                System.out.println("End Timer: " + dateFormat.format(new Date()));
                                System.out.println("Sleep: " + latency);
                                Thread.sleep(latency);
                            }
                        }
                        System.out.println("\n*************** Done ***************");
                    }
                    catch(InterruptedException e) {}
                }
            }).start();
        }


        if(deliveryMode == 2) {
            JFrame frame = new JFrame();
            SpectralSelection spectralSelection = new SpectralSelection(image, quantizationLevel);
            RenderImage renderImage = new RenderImage(newImage);

            frame.getContentPane().add(renderImage);
            frame.setSize (width, height);
            frame.setVisible(true);

            new Thread (new Runnable() {
                @Override
                public void run() {
                    try {
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                        int level = 0;
                        while (level < 64) {
                            System.out.println("\n*************** " + "Iteration " + level + " ***************");
                            System.out.println("Begin");
                            System.out.println("Start Timer: " + dateFormat.format(new Date()));
                            spectralSelection.getIndividualChannelData();
                            spectralSelection.divideChannels(level++);
                            newImage = spectralSelection.getNewImage();
                            renderImage.updateImage(newImage);
                            System.out.println("End Timer: " + dateFormat.format(new Date()));
                            System.out.println("Sleep: " + latency);
                            Thread.sleep(latency);
                        }
                        System.out.println("\n*************** Done ***************");
                    }
                    catch(InterruptedException e) {}
                }
            }).start();
        }

        if(deliveryMode == 3) {
            JFrame frame = new JFrame();
            SuccessiveBitApproximation successiveBitApproximation = new SuccessiveBitApproximation(image, quantizationLevel);
            RenderImage renderImage = new RenderImage(newImage);
            frame.getContentPane().add(renderImage);

            frame.setSize (width, height);
            frame.setVisible (true);

            new Thread (new Runnable() {
                @Override
                public void run() {
                    try {
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                        int level = 1;
                        while (level < 9) {
                            System.out.println("\n*************** " + "Iteration " + level + " ***************");
                            System.out.println("Begin");
                            System.out.println("Start Timer: " + dateFormat.format(new Date()));
                            successiveBitApproximation.getIndividualChannelData();
                            successiveBitApproximation.divideChannels(level++);
                            newImage = successiveBitApproximation.getNewImage();
                            renderImage.updateImage(newImage);
                            System.out.println("End Timer: " + dateFormat.format(new Date()));
                            System.out.println("Sleep: " + latency);
                            Thread.sleep(latency);
                        }
                        System.out.println("\n*************** Done ***************");
                    }
                    catch(InterruptedException e) {}
                }
            }).start();
        }
    }
}
