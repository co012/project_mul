package pl.edu.agh.kis.jpeg;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Huffman {
    private int bufferPutBits, bufferPutBuffer;
    private int[][][] DC_matrix;
    private int[][][] AC_matrix;
    private final static int[] BITS_DC_LUMINANCE = {0, 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0};
    private final static int[] VALUES_DC_LUMINANCE = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    private final static int[] BITS_DC_CHROMINANCE = {1, 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};
    private final static int[] VALUES_DC_CHROMINANCE = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    private final static int[] BITS_AC_LUMINANCE = {16, 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 125};
    private final static int[] VALUES_AC_LUMINANCE = {0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06, 0x13, 0x51,
            0x61, 0x07, 0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42, 0xb1, 0xc1, 0x15, 0x52, 0xd1, 0xf0,
            0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a,
            0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55,
            0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77,
            0x78, 0x79, 0x7a, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98,
            0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8,
            0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8,
            0xd9, 0xda, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6,
            0xf7, 0xf8, 0xf9, 0xfa
    };
    private final static int[] BITS_AC_CHROMINANCE = {17, 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 119};
    private final static int[] VALUES_AC_CHROMINANCE = {0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31, 0x06, 0x12, 0x41, 0x51,
            0x07, 0x61, 0x71, 0x13, 0x22, 0x32, 0x81, 0x08, 0x14, 0x42, 0x91, 0xa1, 0xb1, 0xc1, 0x09, 0x23, 0x33, 0x52,
            0xf0, 0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34, 0xe1, 0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26, 0x27,
            0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53,
            0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75,
            0x76, 0x77, 0x78, 0x79, 0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95,
            0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5,
            0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5,
            0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5,
            0xf6, 0xf7, 0xf8, 0xf9, 0xfa
    };

    private final List<int[]> bits;
    private final List<int[]> val;

    /*
     * jpegNaturalOrder[i] is the natural-order position of the i'th element of
     * zigzag order.
     */
    private static final int[] JPEG_NATURAL_ORDER = {0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33,
            40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44,
            51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63,};

    /**
     * The Huffman class constructor
     **/
    public Huffman() {

        bits = new ArrayList<>();
        bits.add(BITS_DC_LUMINANCE);
        bits.add(BITS_AC_LUMINANCE);
        bits.add(BITS_DC_CHROMINANCE);
        bits.add(BITS_AC_CHROMINANCE);
        val = new ArrayList<>();
        val.add(VALUES_DC_LUMINANCE);
        val.add(VALUES_AC_LUMINANCE);
        val.add(VALUES_DC_CHROMINANCE);
        val.add(VALUES_AC_CHROMINANCE);
        initHuf();
    }

    /**
     * HuffmanBlockEncoder run length encodes and Huffman encodes the quantized
     * data.
     */

    public void HuffmanBlockEncoder(BufferedOutputStream outStream, int[] zigzag, int prec, int dcCode, int acCode)
            throws IOException {
        encodeDCPortion(outStream, zigzag, prec, DC_matrix[dcCode]);
        encodeACPortion(outStream, zigzag, AC_matrix[acCode]);
    }

    private void encodeACPortion(BufferedOutputStream outStream, int[] zigzag, int[][] acSubMatrix) throws IOException {

        int r = 0;
        for (int k = 1; k < 64; k++) {
            int zigzagElement;
            if ((zigzagElement = zigzag[JPEG_NATURAL_ORDER[k]]) == 0) {
                r++;
            } else {
                while (r > 15) {
                    bufferIt(outStream, acSubMatrix[0xF0][0], acSubMatrix[0xF0][1]);
                    r -= 16;
                }
                int temp2 = zigzagElement;
                if (zigzagElement < 0) {
                    zigzagElement = -zigzagElement;
                    temp2--;
                }
                int nbits = 1;
                while ((zigzagElement >>= 1) != 0) {
                    nbits++;
                }
                int index = (r << 4) + nbits;
                bufferIt(outStream, acSubMatrix[index][0], acSubMatrix[index][1]);
                bufferIt(outStream, temp2, nbits);

                r = 0;
            }
        }

        if (r > 0) {
            bufferIt(outStream, acSubMatrix[0][0], acSubMatrix[0][1]);
        }
    }

    private void encodeDCPortion(BufferedOutputStream outStream, int[] zigzag, int prec, int[][] dcSubMatrix) throws IOException {
        int temp2;
        int temp;
        temp2 = zigzag[0] - prec;
        temp = temp2;
        if (temp < 0) {
            temp = -temp;
            temp2--;
        }
        int nbits = 0;
        while (temp != 0) {
            nbits++;
            temp >>= 1;
        }

        bufferIt(outStream, (dcSubMatrix)[nbits][0], (dcSubMatrix)[nbits][1]);
        // The arguments in bufferIt are code and size.
        if (nbits != 0) {
            bufferIt(outStream, temp2, nbits);
        }
    }

    //Uses an integer long (32 bits) buffer to store the Huffman encoded bits
    //and sends them to outStream by the byte.

    private void bufferIt(BufferedOutputStream outStream, int code, int size) throws IOException {
        int PutBuffer = code;
        int PutBits = bufferPutBits;

        PutBuffer &= (1 << size) - 1;
        PutBits += size;
        PutBuffer <<= 24 - PutBits;
        PutBuffer |= bufferPutBuffer;

        while (PutBits >= 8) {
            int c = ((PutBuffer >> 16) & 0xFF);
            outStream.write(c);
            if (c == 0xFF) {
                outStream.write(0);
            }
            PutBuffer <<= 8;
            PutBits -= 8;
        }
        bufferPutBuffer = PutBuffer;
        bufferPutBits = PutBits;

    }

    public void flushBuffer(BufferedOutputStream outStream) throws IOException {
        int PutBuffer = bufferPutBuffer;
        int PutBits = bufferPutBits;
        while (PutBits >= 8) {
            int c = ((PutBuffer >> 16) & 0xFF);
            outStream.write(c);
            if (c == 0xFF) {
                outStream.write(0);
            }
            PutBuffer <<= 8;
            PutBits -= 8;
        }
        if (PutBits > 0) {
            int c = ((PutBuffer >> 16) & 0xFF);
            outStream.write(c);
        }
    }

    /**
     * Initialisation of the Huffman codes for Luminance and Chrominance. This code
     * results in the same tables created in the IJG Jpeg-6a library.
     **/

    private void initHuf() {

        int[] huffSize = new int[257];
        int[] huffCode = new int[257];

        /*
         * init of the DC values for the chrominance [][0] is the code [][1] is the
         * number of bit
         */
        int[][] DC_matrix1 = new int[12][2];
        encodeHuffman(DC_matrix1, huffSize, huffCode, BITS_DC_CHROMINANCE, VALUES_DC_CHROMINANCE);

        /*
         * Init of the AC hufmann code for the chrominance matrix [][][0] is the code &
         * matrix[][][1] is the number of bit needed
         */
        int[][] AC_matrix1 = new int[255][2];
        encodeHuffman(AC_matrix1, huffSize, huffCode, BITS_AC_CHROMINANCE, VALUES_AC_CHROMINANCE);

        /*
         * init of the DC values for the luminance [][0] is the code [][1] is the number
         * of bit
         */
        int[][] DC_matrix0 = new int[12][2];
        encodeHuffman(DC_matrix0, huffSize, huffCode, BITS_DC_LUMINANCE, VALUES_DC_LUMINANCE);

        /*
         * Init of the AC hufmann code for luminance matrix [][][0] is the code &
         * matrix[][][1] is the number of bit
         */
        int[][] AC_matrix0 = new int[255][2];
        encodeHuffman(AC_matrix0, huffSize, huffCode, BITS_AC_LUMINANCE, VALUES_AC_LUMINANCE);

        DC_matrix = new int[2][][];
        AC_matrix = new int[2][][];

        DC_matrix[0] = DC_matrix0;
        DC_matrix[1] = DC_matrix1;
        AC_matrix[0] = AC_matrix0;
        AC_matrix[1] = AC_matrix1;
    }

    private void encodeHuffman(int[][] matrix, int[] huffmanSize, int[] huffmanCode, int[] bits, int[] values) {
        int lastIndex = 0;
        for (int i = 1; i <= 16; i++) {
            for (int j = 1; j <= bits[i]; j++) {
                huffmanSize[lastIndex++] = i;
            }
        }
        huffmanSize[lastIndex] = 0;

        int code = 0;
        int si = huffmanSize[0];
        int k = 0;
        while (huffmanSize[k] != 0) {
            while (huffmanSize[k] == si) {
                huffmanCode[k++] = code;
                code++;
            }
            code <<= 1;
            si++;
        }

        for (int i = 0; i < lastIndex; i++) {
            matrix[values[i]][0] = huffmanCode[i];
            matrix[values[i]][1] = huffmanSize[i];
        }
    }


    public byte[] getDHT() {
        int temp, oldIndex, intermediateIndex;
        int index = 4;
        oldIndex = 4;
        byte[] DHT1 = new byte[17];
        byte[] DHT4 = new byte[4];
        DHT4[0] = (byte) 0xFF;
        DHT4[1] = (byte) 0xC4;
        for (int i = 0; i < 4; i++) {
            int bytes = 0;
            DHT1[index++ - oldIndex] = (byte) bits.get(i)[0];
            for (int j = 1; j < 17; j++) {
                temp = bits.get(i)[j];
                DHT1[index++ - oldIndex] = (byte) temp;
                bytes += temp;
            }
            intermediateIndex = index;
            byte[] DHT2 = new byte[bytes];
            for (int j = 0; j < bytes; j++) {
                DHT2[index++ - intermediateIndex] = (byte) val.get(i)[j];
            }
            byte[] DHT3 = new byte[index];
            System.arraycopy(DHT4, 0, DHT3, 0, oldIndex);
            System.arraycopy(DHT1, 0, DHT3, oldIndex, 17);
            System.arraycopy(DHT2, 0, DHT3, oldIndex + 17, bytes);
            DHT4 = DHT3;
            oldIndex = index;
        }
        DHT4[2] = (byte) (((index - 2) >> 8) & 0xFF);
        DHT4[3] = (byte) ((index - 2) & 0xFF);

        return DHT4;
    }

}
