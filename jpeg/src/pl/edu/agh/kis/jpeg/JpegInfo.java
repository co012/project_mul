package pl.edu.agh.kis.jpeg;

import java.awt.*;
import java.awt.image.PixelGrabber;

class JpegInfo {
    private String comment;
    private final Image image;
    public final int imageHeight;
    public final int imageWidth;
    public final int[] blockWidth;
    public final int[] blockHeight;

    // the following are set as the default
    final int precision = 8;
    final int numberOfComponents = 3;
    final float[][][] components;
    final static int[] componentsId = {1, 2, 3};
    final int[] horizontalSamplingFactor = {1, 1, 1};
    final int[] verticalSamplingFactor = {1, 1, 1};
    final int[] quantizationTableNumber = {0, 1, 1};
    final int[] directCurrentTableNumber = {0, 1, 1};
    final int[] alternatingCurrentTableNumber = {0, 1, 1};
    final int Ss = 0;
    final int Se = 63;
    final int Al = 0;
    private final int[] componentsWidth;
    private final int[] componentsHeight;

    public JpegInfo(Image image) {
        components = new float[numberOfComponents][][];
        componentsWidth = new int[numberOfComponents];
        componentsHeight = new int[numberOfComponents];
        blockWidth = new int[numberOfComponents];
        blockHeight = new int[numberOfComponents];
        this.image = image;
        imageWidth = image.getWidth(null);
        imageHeight = image.getHeight(null);
        comment = "JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech.  ";
        getYCCArray();
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    /*
     * This method creates and fills three arrays, Y, Cb, and Cr using the input
     * image.
     */

    private void getYCCArray() {
        int[] values = new int[imageWidth * imageHeight];
        int r, g, b, y, x;
//In order to minimize the chance that grabPixels will throw an exception
//it may be necessary to grab some pixels every few scanlines and process
//those before going for more.  The time expense may be prohibitive.
//However, for a situation where memory overhead is a concern, this may be
//the only choice.
        PixelGrabber grabber = new PixelGrabber(image.getSource(), 0, 0, imageWidth, imageHeight, values, 0,
                imageWidth);
        int maxHorizontalSamplingFactor = 1;
        int maxVerticalSamplingFactor = 1;
        for (y = 0; y < numberOfComponents; y++) {
            maxHorizontalSamplingFactor = Math.max(maxHorizontalSamplingFactor, horizontalSamplingFactor[y]);
            maxVerticalSamplingFactor = Math.max(maxVerticalSamplingFactor, verticalSamplingFactor[y]);
        }

        for (y = 0; y < numberOfComponents; y++) {
            componentsWidth[y] = (((imageWidth % 8 != 0) ? ((int) Math.ceil((double) imageWidth / 8.0)) * 8 : imageWidth)
                    / maxHorizontalSamplingFactor) * horizontalSamplingFactor[y];

            // results in a multiple of 8 for compWidth
            // this will make the rest of the program fail for the unlikely
            // event that someone tries to compress an 16 x 16 pixel image
            // which would of course be worse than pointless
            blockWidth[y] = (int) Math.ceil((double) componentsWidth[y] / 8.0);
            componentsHeight[y] = (((imageHeight % 8 != 0) ? ((int) Math.ceil((double) imageHeight / 8.0)) * 8 : imageHeight)
                    / maxVerticalSamplingFactor) * verticalSamplingFactor[y];

            blockHeight[y] = (int) Math.ceil((double) componentsHeight[y] / 8.0);
        }
        try {
            grabber.grabPixels();
        } catch (InterruptedException ignored) {
        }
        float[][] Y = new float[componentsHeight[0]][componentsWidth[0]];
        float[][] Cr1 = new float[componentsHeight[0]][componentsWidth[0]];
        float[][] Cb1 = new float[componentsHeight[0]][componentsWidth[0]];
        int index = 0;
        for (y = 0; y < imageHeight; ++y) {
            for (x = 0; x < imageWidth; ++x) {
                r = ((values[index] >> 16) & 0xff);
                g = ((values[index] >> 8) & 0xff);
                b = (values[index] & 0xff);


                Y[y][x] = (float) ((0.299 * (float) r + 0.587 * (float) g + 0.114 * (float) b));
                Cb1[y][x] = 128 + (float) ((-0.16874 * (float) r - 0.33126 * (float) g + 0.5 * (float) b));
                Cr1[y][x] = 128 + (float) ((0.5 * (float) r - 0.41869 * (float) g - 0.08131 * (float) b));
                index++;
            }
        }



        components[0] = Y;
        components[1] = Cb1;
        components[2] = Cr1;
    }

    public byte[] getStartOfHeader(){
        byte[] SOF = new byte[19];
        SOF[0] = (byte) 0xFF;
        SOF[1] = (byte) 0xC0;
        SOF[2] = (byte) 0x00;
        SOF[3] = (byte) 17;
        SOF[4] = (byte) precision;
        SOF[5] = (byte) ((imageHeight >> 8) & 0xFF);
        SOF[6] = (byte) ((imageHeight) & 0xFF);
        SOF[7] = (byte) ((imageWidth >> 8) & 0xFF);
        SOF[8] = (byte) ((imageWidth) & 0xFF);
        SOF[9] = (byte) numberOfComponents;
        int index = 10;

        for (int i = 0; i < numberOfComponents; i++) {
            SOF[index++] = (byte) componentsId[i];
            SOF[index++] = (byte) ((horizontalSamplingFactor[i] << 4) + verticalSamplingFactor[i]);
            SOF[index++] = (byte) quantizationTableNumber[i];
        }

        return SOF;
    }

    public byte[] getStartOfScanHeader(){
        byte[] SOS = new byte[14];
        SOS[0] = (byte) 0xFF;
        SOS[1] = (byte) 0xDA;
        SOS[2] = (byte) 0x00;
        SOS[3] = (byte) 12;
        SOS[4] = (byte) numberOfComponents;
        int index = 5;
        for (int i = 0; i < SOS[4]; i++) {
            SOS[index++] = (byte) JpegInfo.componentsId[i];
            SOS[index++] = (byte) ((directCurrentTableNumber[i] << 4) + alternatingCurrentTableNumber[i]);
        }
        SOS[index++] = (byte) Ss;
        SOS[index++] = (byte) Se;
        SOS[index] = (byte) Al;
        return SOS;
    }

    public byte[] getCommentHeader(){
        byte[] comment = getComment().getBytes();
        int length = comment.length;
        byte[] COM = new byte[length + 4];
        COM[0] = (byte) 0xFF;
        COM[1] = (byte) 0xFE;
        COM[2] = (byte) ((length >> 8) & 0xFF);
        COM[3] = (byte) (length & 0xFF);
        System.arraycopy(comment, 0, COM, 4, comment.length);
        return COM;
    }


}
