package pl.edu.agh.kis.jpeg;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Huffman {
    private int bufferPutBits, bufferPutBuffer;
    private int[][][] DC_matrix;
    private int[][][] AC_matrix;
    private final int[] bitsDCluminance = {0x00, 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0};
    private final int[] valDCluminance = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    private final int[] bitsDCchrominance = {0x01, 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};
    private final int[] valDCchrominance = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    private final int[] bitsACluminance = {0x10, 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 0x7d};
    private final int[] valACluminance = {0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06, 0x13, 0x51,
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
    private final int[] bitsACchrominance = {0x11, 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 0x77};

    private final int[] valACchrominance = {0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31, 0x06, 0x12, 0x41, 0x51,
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
    private static final int[] jpegNaturalOrder = {0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33,
            40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44,
            51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63,};

    /**
     * The Huffman class constructor
     **/
    public Huffman() {

        bits = new ArrayList<>();
        bits.add(bitsDCluminance);
        bits.add(bitsACluminance);
        bits.add(bitsDCchrominance);
        bits.add(bitsACchrominance);
        val = new ArrayList<>();
        val.add(valDCluminance);
        val.add(valACluminance);
        val.add(valDCchrominance);
        val.add(valACchrominance);
        initHuf();
    }

    /**
     * HuffmanBlockEncoder run length encodes and Huffman encodes the quantized
     * data.
     */

    void HuffmanBlockEncoder(BufferedOutputStream outStream, int[] zigzag, int prec, int DCcode, int ACcode)
            throws IOException {
        encodeDCPortion(outStream, zigzag, prec, DC_matrix[DCcode]);
        encodeACPortion(outStream, zigzag, AC_matrix[ACcode]);
    }

    private void encodeACPortion(BufferedOutputStream outStream, int[] zigzag, int[][] ACSubMatrix) throws IOException {
        int temp2;
        int temp;
        int r = 0;

        for (int k = 1; k < 64; k++) {
            if ((temp = zigzag[jpegNaturalOrder[k]]) == 0) {
                r++;
            } else {
                while (r > 15) {
                    bufferIt(outStream, ACSubMatrix[0xF0][0], ACSubMatrix[0xF0][1]);
                    r -= 16;
                }
                temp2 = temp;
                if (temp < 0) {
                    temp = -temp;
                    temp2--;
                }
                int nbits = 1;
                while ((temp >>= 1) != 0) {
                    nbits++;
                }
                int i = (r << 4) + nbits;
                bufferIt(outStream, ACSubMatrix[i][0], ACSubMatrix[i][1]);
                bufferIt(outStream, temp2, nbits);

                r = 0;
            }
        }

        if (r > 0) {
            bufferIt(outStream, ACSubMatrix[0][0], ACSubMatrix[0][1]);
        }
    }

    private void encodeDCPortion(BufferedOutputStream outStream, int[] zigzag, int prec, int[][] dc_matrix) throws IOException {
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

        bufferIt(outStream, (dc_matrix)[nbits][0], (dc_matrix)[nbits][1]);
        // The arguments in bufferIt are code and size.
        if (nbits != 0) {
            bufferIt(outStream, temp2, nbits);
        }
    }

//Uses an integer long (32 bits) buffer to store the Huffman encoded bits
//and sends them to outStream by the byte.

    void bufferIt(BufferedOutputStream outStream, int code, int size) throws IOException {
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

    void flushBuffer(BufferedOutputStream outStream) throws IOException {
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

        int[] huffsize = new int[257];
        int[] huffcode = new int[257];

        /*
         * init of the DC values for the chrominance [][0] is the code [][1] is the
         * number of bit
         */
        int[][] DC_matrix1 = new int[12][2];
        encodeHuffman(DC_matrix1, huffsize, huffcode, bitsDCchrominance, valDCchrominance);

        /*
         * Init of the AC hufmann code for the chrominance matrix [][][0] is the code &
         * matrix[][][1] is the number of bit needed
         */
        int[][] AC_matrix1 = new int[255][2];
        encodeHuffman(AC_matrix1, huffsize, huffcode, bitsACchrominance, valACchrominance);

        /*
         * init of the DC values for the luminance [][0] is the code [][1] is the number
         * of bit
         */
        int[][] DC_matrix0 = new int[12][2];
        encodeHuffman(DC_matrix0, huffsize, huffcode, bitsDCluminance, valDCluminance);

        /*
         * Init of the AC hufmann code for luminance matrix [][][0] is the code &
         * matrix[][][1] is the number of bit
         */
        int[][] AC_matrix0 = new int[255][2];
        encodeHuffman(AC_matrix0, huffsize, huffcode, bitsACluminance, valACluminance);

        DC_matrix = new int[2][][];
        AC_matrix = new int[2][][];

        DC_matrix[0] = DC_matrix0;
        DC_matrix[1] = DC_matrix1;
        AC_matrix[0] = AC_matrix0;
        AC_matrix[1] = AC_matrix1;
    }

    private void encodeHuffman(int[][] matrix, int[] huffmanSize, int[] huffmanCode, int[] bits, int[] values) {
        int p = 0;
        for (int l = 1; l <= 16; l++) {
            for (int i = 1; i <= bits[l]; i++) {
                huffmanSize[p++] = l;
            }
        }
        huffmanSize[p] = 0;
        int lastP = p;

        int code = 0;
        int si = huffmanSize[0];
        p = 0;
        while (huffmanSize[p] != 0) {
            while (huffmanSize[p] == si) {
                huffmanCode[p++] = code;
                code++;
            }
            code <<= 1;
            si++;
        }

        for (p = 0; p < lastP; p++) {
            matrix[values[p]][0] = huffmanCode[p];
            matrix[values[p]][1] = huffmanSize[p];
        }
    }


    public byte[] getDHT(){
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
