package com.nj.CSCI576;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SequentialMode extends JPanel {
    BufferedImage image, newImage;
    int[][] redChannel, greenChannel, blueChannel;
    final int N = 8;
    int[][] redChannelBlock, greenChannelBlock, blueChannelBlock;
    int width, height;
    int DC;
    int[] AC;
    int quantizationLevel;

    public SequentialMode(BufferedImage image, int quantizationLevel) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();

        this.quantizationLevel = quantizationLevel;

        redChannel = new int[height][width];
        greenChannel = new int[height][width];
        blueChannel = new int[height][width];

        redChannelBlock = new int[N][N];
        greenChannelBlock = new int[N][N];
        blueChannelBlock = new int[N][N];

        newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        DC = 0;
        AC = new int[N * N];
    }

    public BufferedImage getCompressedBlock(int startRow, int startCol) {
        getIndividualChannelData();
        divideChannels(startRow, startCol);
        return newImage;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(newImage, 0, 0, this);
    }

    public void getIndividualChannelData() {
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int pixelValue = image.getRGB(x,y);

                int alpha = (pixelValue >> 24) & 0xff;
                int red = (pixelValue >> 16) & 0xff;
                int green = (pixelValue >> 8) & 0xff;
                int blue = (pixelValue) & 0xff;

                redChannel[y][x] = red;
                greenChannel[y][x] = green;
                blueChannel[y][x] = blue;
            }
        }
    }

    public void divideChannels(int startRow, int startCol) {
        for(int i = startRow, row = 0; i < (startRow + N); i++, row++) {
            for(int j = startCol, col = 0; j < (startCol + N); j++, col++) {
                redChannelBlock[row][col] = redChannel[i][j];
                greenChannelBlock[row][col] = greenChannel[i][j];
                blueChannelBlock[row][col] = blueChannel[i][j];
            }
        }

        int[][] quantizedRedBlock = encodeChannelBlock(redChannelBlock);
        int[][] IDCTRedBlock = decodeChannelBlock(quantizedRedBlock);

        int[][] quantizedGreenBlock = encodeChannelBlock(greenChannelBlock);
        int[][] IDCTGreenBlock = decodeChannelBlock(quantizedGreenBlock);

        int[][] quantizedBlueBlock = encodeChannelBlock(blueChannelBlock);
        int[][] IDCTBlueBlock = decodeChannelBlock(quantizedBlueBlock);

//        System.out.println("\nIDCTRedBlock");
//        displayMatrix(IDCTRedBlock, N);
//        System.out.println("\nIDCTGreenBlock");
//        displayMatrix(IDCTGreenBlock, N);
//        System.out.println("\nIDCTBlueBlock");
//        displayMatrix(IDCTBlueBlock, N);

        int pixelValue;
        for(int i = 0, rowCounter = 0; i < N; i++, rowCounter++) {
            for(int j = 0, colCounter = 0; j < N; j++, colCounter++) {
                int red = IDCTRedBlock[i][j];
                int green = IDCTGreenBlock[i][j];
                int blue = IDCTBlueBlock[i][j];

                red = red < 0 ? 0 : red;
                red = red > 255 ? 255 : red;

                green = green < 0 ? 0 : green;
                green = green > 255 ? 255 : green;

                blue = blue < 0 ? 0 : blue;
                blue = blue > 255 ? 255 : blue;

                int a =  0xff000000;
                int r = ((red & 0xff) << 16);
                int g = ((green & 0xff) << 8);
                int b = (blue & 0xff);

                pixelValue = a | r | g | b;
                int t1 = startCol + colCounter;
                int t2 = startRow + rowCounter;
                newImage.setRGB(t1, t2, pixelValue);
            }
        }
    }

    public int[][] encodeChannelBlock(int[][] block) {
        int[][] initBlock, DCTBlock, quantizedBlock;

        initBlock = block;
        DCTBlock = getDCT(initBlock);
        quantizedBlock = quantize(DCTBlock);

        DC = quantizedBlock[0][0];
        AC = getAC(quantizedBlock);

        return quantizedBlock;
    }

    public int[][] getDCT(int[][] testBlock) {
        int[][] dct = new int[N][N];
        double oneBySqrt2 = 1 / Math.sqrt(2);
        double coeffU, coeffV;
        for(int u = 0; u < N; u++) {
            for(int v  = 0; v < N; v++) {
                double temp = 0.0;
                double exp1 = u * Math.PI / (2 * N);
                double exp2 = v * Math.PI / (2 * N);
                for(int x = 0; x < N; x++) {
                    for(int y = 0; y < N; y++) {
                        double t1 = (2 * x + 1) * exp1;
                        double t2 = (2 * y + 1) * exp2;
                        double cosXValue = Math.cos(t1);
                        double cosYValue = Math.cos(t2);
                        temp += testBlock[x][y] * cosXValue * cosYValue;
                    }
                }
                coeffU = u == 0 ? oneBySqrt2 : 1;
                coeffV = v == 0 ? oneBySqrt2 : 1;

                temp *= (1 / Math.sqrt(2 * N)) * coeffU * coeffV;
                dct[u][v] = (int) Math.round(temp);
            }
        }
        return dct;
    }

    public int[][] getInverseDCT(int[][] dct) {
        int[][] idct = new int[N][N];
        double oneBySqrt2 = 1 / Math.sqrt(2);
        double coeffU, coeffV;
        for(int x = 0; x < N; x++) {
            for(int y  = 0; y < N; y++) {
                double temp = 0.0;
                for(int u = 0; u < N; u++) {
                    for(int v = 0; v < N; v++) {
                        double exp1 = u * Math.PI / (2 * N);
                        double exp2 = v * Math.PI / (2 * N);

                        coeffU = u == 0 ? oneBySqrt2 : 1;
                        coeffV = v == 0 ? oneBySqrt2 : 1;

                        double t1 = (2 * x + 1) * exp1;
                        double t2 = (2 * y + 1) * exp2;
                        double cosXValue = Math.cos(t1);
                        double cosYValue = Math.cos(t2);

                        temp += coeffU * coeffV * dct[u][v] * cosXValue * cosYValue;
                    }
                }


                temp *= 1 / Math.sqrt(2 * N);
                idct[x][y] = (int) Math.round(temp);
            }
        }
        return idct;
    }

    public int[][] quantize(int[][] dct) {
        int[][] compress = new int[N][N];
        int[][] quantizationTable = getQuantizationTable(quantizationLevel);
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                compress[i][j] = Math.round(dct[i][j] / quantizationTable[i][j]);
            }
        }
        return compress;
    }

    public int[] getAC(int[][] block) {
        int[] AC = new int[N * N];
        int row = 0, col = 0;
        int index = 0;
        boolean row_inc = false;

        int mn = Math.min(N, N);
        for (int len = 1; len <= mn; ++len) {
            for (int i = 0; i < len; ++i) {
                AC[index++] = block[row][col];

                if (i + 1 == len)
                    break;
                if (row_inc) {
                    ++row;
                    --col;
                } else {
                    --row;
                    ++col;
                }
            }

            if (len == mn)
                break;

            if (row_inc) {
                ++row;
                row_inc = false;
            } else {
                ++col;
                row_inc = true;
            }
        }

        if (row == 0) {
            if (col == N - 1)
                ++row;
            else
                ++col;
            row_inc = true;
        } else {
            if (row == N - 1)
                ++col;
            else
                ++row;
            row_inc = false;
        }

        int MAX = Math.max(N, N) - 1;
        for (int len, diag = MAX; diag > 0; --diag) {

            if (diag > mn)
                len = mn;
            else
                len = diag;

            for (int i = 0; i < len; ++i) {
                AC[index++] = block[row][col];

                if (i + 1 == len)
                    break;
                if (row_inc) {
                    ++row;
                    --col;
                } else {
                    ++col;
                    --row;
                }
            }

            if (row == 0 || col == N - 1) {
                if (col == N - 1)
                    ++row;
                else
                    ++col;

                row_inc = true;
            }

            else if (col == 0 || row == N - 1) {
                if (row == N - 1)
                    ++col;
                else
                    ++row;

                row_inc = false;
            }
        }

        for(int i = 0; i < (N * N) - 1; i++) {
            AC[i] = AC[i + 1];
        }

        return AC;
    }

    public int[][] decodeChannelBlock(int[][] quantizedBlock) {
        int[][] deQuantizeBlock, IDCTBlock;

        deQuantizeBlock = deQuantize(quantizedBlock);
        IDCTBlock = getInverseDCT(deQuantizeBlock);

        return IDCTBlock;
    }

    public int[][] deQuantize(int[][] dct) {
        int[][] compress = new int[N][N];
        int[][] quantizationTable = getQuantizationTable(quantizationLevel);
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                compress[i][j] = Math.round(dct[i][j] * quantizationTable[i][j]);
            }
        }
        return compress;
    }

    public int[][] getQuantizationTable(int factor) {
        double quantizationValue = Math.pow(2, factor);
        int[][] quantizationTable = new int[N][N];

        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                quantizationTable[i][j] = (int) quantizationValue;
            }
        }
//        System.out.println("\nquantizationTable");
//        displayMatrix(quantizationTable, N);
        return quantizationTable;
    }

    public void displayMatrix(int[][] block, int N) {
        System.out.println("[");
        for(int i = 0; i < N; i++) {
            System.out.printf("%-10s", "\t[");
            for(int j = 0; j < N; j++) {
                if(j == N - 1) {
                    System.out.printf("%-10s", block[i][j] + "");
                } else {
                    System.out.printf("%-10s", block[i][j] + ",  ");
                }

            }
            if(i == N - 1) {
                System.out.printf("%-10s", "]");
            } else {
                System.out.print("], ");
            }

            System.out.println();
        }
        System.out.println("]");
    }

}

