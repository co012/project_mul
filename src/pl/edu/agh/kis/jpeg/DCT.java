package pl.edu.agh.kis.jpeg;

class DCT {
    /**
     * DCT Block Size - default 8
     */
    private final int N = 8;

    Object[] quantum = new Object[2];
    private final Object[] Divisors = new Object[2];

    /**
     * Quantitization Matrix for luminace.
     */
    private final int[] quantum_luminance =
            new int[]{16, 11, 10, 16, 24, 40, 51, 61, 12, 12, 14, 19, 26, 58, 60, 55, 14,
                    13, 16, 24, 40, 57, 69, 56, 14, 17, 22, 29, 51, 87, 80, 62, 18, 22, 37,
                    56, 68, 109, 103, 77, 24, 35, 55, 64, 81, 104, 113, 92, 49, 64, 78, 87,
                    103, 121, 120, 101, 72, 92, 95, 98, 112, 100, 103, 99,
    };
    private final double[] DivisorsLuminance = new double[N * N];

    /**
     * Quantitization Matrix for chrominance.
     */
    private final int[] quantum_chrominance =
            new int[]{17, 18, 24, 47, 99, 99, 99, 99, 18, 21, 26, 66, 99, 99, 99, 99, 24, 26,
                    56, 99, 99, 99, 99, 99, 47, 66, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,
                    99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,
                    99, 99, 99, 99, 99, 99, 99, 99
    };
    private final double[] DivisorsChrominance = new double[N * N];

    /* Constructs a new DCT object. Initializes the cosine transform matrix these
     * are used when computing the DCT and it's inverse. This also initializes the
     * run length counters and the ZigZag sequence. Note that the image quality can
     * be worse than 25 however the image will be extemely pixelated, usually to a
     * block size of N.
     *
     * @param QUALITY The quality of the image (0 worst - 100 best)
     */
    public DCT(int QUALITY) {
        initMatrix(QUALITY);
    }

    /*
     * This method sets up the quantization matrix for luminance and chrominance
     * using the Quality parameter.
     */
    private void initMatrix(final int quality) {
        double[] AANscaleFactor = {1.0, 1.387039845, 1.306562965, 1.175875602, 1.0, 0.785694958, 0.541196100,
                0.275899379};
        int i;
        int j;
        int index;
        int qualityNormalized;
        int temp;

//converting quality setting to that specified in the jpeg_quality_scaling
//method in the IJG Jpeg-6a C libraries

        qualityNormalized = Math.max(Math.min(quality, 100), 1);

        if (qualityNormalized < 50)
            qualityNormalized = 5000 / qualityNormalized;
        else
            qualityNormalized = 200 - qualityNormalized * 2;



        for (j = 0; j < 64; j++) {
            temp = (quantum_luminance[j] * qualityNormalized + 50) / 100;
            quantum_luminance[j] = Math.max(Math.min(temp, 255), 1);
        }
        index = 0;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
//The divisors for the LL&M method (the slow integer method used in
//jpeg 6a library).  This method is currently (04/04/98) incompletely
//implemented.
//                     DivisorsLuminance[index] = ((double) quantum_luminance[index]) << 3;
//The divisors for the AAN method (the float method used in jpeg 6a library.
                DivisorsLuminance[index] = (1.0
                        / ((double) quantum_luminance[index] * AANscaleFactor[i] * AANscaleFactor[j] * 8.0));
                index++;
            }
        }


        for (j = 0; j < 64; j++) {
            temp = (quantum_chrominance[j] * qualityNormalized + 50) / 100;
            temp = Math.max(Math.min(temp, 255), 1);
            quantum_chrominance[j] = temp;
        }
        index = 0;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
//The divisors for the LL&M method (the slow integer method used in
//jpeg 6a library).  This method is currently (04/04/98) incompletely
//implemented.
//                     DivisorsChrominance[index] = ((double) quantum_chrominance[index]) << 3;
//The divisors for the AAN method (the float method used in jpeg 6a library.
                DivisorsChrominance[index] = (1.0
                        / ((double) quantum_chrominance[index] * AANscaleFactor[i] * AANscaleFactor[j] * 8.0));
                index++;
            }
        }

//quantum and Divisors are objects used to hold the appropriate matices

        quantum[0] = quantum_luminance;
        Divisors[0] = DivisorsLuminance;
        quantum[1] = quantum_chrominance;
        Divisors[1] = DivisorsChrominance;

    }

    /*
     * This method preforms a DCT on a block of image data using the AAN method as
     * implemented in the IJG Jpeg-6a library.
     */
    double[][] forwardDCT(float input[][]) {
        double output[][] = new double[N][N];
        double tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
        double tmp10, tmp11, tmp12, tmp13;
        double z1, z2, z3, z4, z5, z11, z13;
        int i;
        int j;

//Subtracts 128 from the input values
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                output[i][j] = ((double) input[i][j] - (double) 128.0);
//                     input[i][j] -= 128;

            }
        }

        for (i = 0; i < 8; i++) {
            tmp0 = output[i][0] + output[i][7];
            tmp7 = output[i][0] - output[i][7];
            tmp1 = output[i][1] + output[i][6];
            tmp6 = output[i][1] - output[i][6];
            tmp2 = output[i][2] + output[i][5];
            tmp5 = output[i][2] - output[i][5];
            tmp3 = output[i][3] + output[i][4];
            tmp4 = output[i][3] - output[i][4];

            tmp10 = tmp0 + tmp3;
            tmp13 = tmp0 - tmp3;
            tmp11 = tmp1 + tmp2;
            tmp12 = tmp1 - tmp2;

            output[i][0] = tmp10 + tmp11;
            output[i][4] = tmp10 - tmp11;

            z1 = (tmp12 + tmp13) * (double) 0.707106781;
            output[i][2] = tmp13 + z1;
            output[i][6] = tmp13 - z1;

            tmp10 = tmp4 + tmp5;
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;

            z5 = (tmp10 - tmp12) * (double) 0.382683433;
            z2 = ((double) 0.541196100) * tmp10 + z5;
            z4 = ((double) 1.306562965) * tmp12 + z5;
            z3 = tmp11 * ((double) 0.707106781);

            z11 = tmp7 + z3;
            z13 = tmp7 - z3;

            output[i][5] = z13 + z2;
            output[i][3] = z13 - z2;
            output[i][1] = z11 + z4;
            output[i][7] = z11 - z4;
        }

        for (i = 0; i < 8; i++) {
            tmp0 = output[0][i] + output[7][i];
            tmp7 = output[0][i] - output[7][i];
            tmp1 = output[1][i] + output[6][i];
            tmp6 = output[1][i] - output[6][i];
            tmp2 = output[2][i] + output[5][i];
            tmp5 = output[2][i] - output[5][i];
            tmp3 = output[3][i] + output[4][i];
            tmp4 = output[3][i] - output[4][i];

            tmp10 = tmp0 + tmp3;
            tmp13 = tmp0 - tmp3;
            tmp11 = tmp1 + tmp2;
            tmp12 = tmp1 - tmp2;

            output[0][i] = tmp10 + tmp11;
            output[4][i] = tmp10 - tmp11;

            z1 = (tmp12 + tmp13) * (double) 0.707106781;
            output[2][i] = tmp13 + z1;
            output[6][i] = tmp13 - z1;

            tmp10 = tmp4 + tmp5;
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;

            z5 = (tmp10 - tmp12) * (double) 0.382683433;
            z2 = ((double) 0.541196100) * tmp10 + z5;
            z4 = ((double) 1.306562965) * tmp12 + z5;
            z3 = tmp11 * ((double) 0.707106781);

            z11 = tmp7 + z3;
            z13 = tmp7 - z3;

            output[5][i] = z13 + z2;
            output[3][i] = z13 - z2;
            output[1][i] = z11 + z4;
            output[7][i] = z11 - z4;
        }

        return output;
    }

    /*
     * This method quantitizes data and rounds it to the nearest integer.
     */
    int[] quantizeBlock(double inputData[][], int code) {
        int outputData[] = new int[N * N];
        int i, j;
        int index;
        index = 0;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
//The second line results in significantly better compression.
                outputData[index] = (int) (Math.round(inputData[i][j] * (((double[]) (Divisors[code]))[index])));
//                     outputData[index] = (int)(((inputData[i][j] * (((double[]) (Divisors[code]))[index])) + 16384.5) -16384);
                index++;
            }
        }

        return outputData;
    }
}
