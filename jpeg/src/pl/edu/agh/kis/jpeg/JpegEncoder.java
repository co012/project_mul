package pl.edu.agh.kis.jpeg;

//Version 1.0a
//Copyright (C) 1998, James R. Weeks and BioElectroMech.
//Visit BioElectroMech at www.obrador.com.  Email James@obrador.com.

//See license.txt for details about the allowed used of this software.
//This software is based in part on the work of the Independent JPEG Group.
//See IJGreadme.txt for details about the Independent JPEG Group's license.

//This encoder is inspired by the Java Jpeg encoder by Florian Raemy,
//studwww.eurecom.fr/~raemy.
//It borrows a great deal of code and structure from the Independent
//Jpeg Group's Jpeg 6a library, Copyright Thomas G. Lane.

/*
The JpegEncoder and its associated classes are Copyright (c) 1998, James R.
Weeks and BioElectroMech.  This software is based in part on the work of the
Independent JPEG Group.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions, all files included with the source code, and the following
disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/*
* JpegEncoder - The JPEG main program which performs a jpeg compression of
* an image.
*/

public class JpegEncoder {
	private final BufferedOutputStream outStream;
	private final JpegInfo jpegInfo;
	private final Huffman huffman;
	private final DCT dct;
	private final int imageHeight, imageWidth;

	private final static byte[] JFIF =
			new byte[]{ (byte) 0xff, (byte) 0xe0, 0x00, 0x10, 0x4a, 0x46, 0x49,
			0x46, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00
	};

	private final static byte[] SOI = { (byte) 0xFF, (byte) 0xD8 };


	public static int[] jpegNaturalOrder = { 0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33, 40,
			48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51,
			58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63, };

	public JpegEncoder(Image image, int quality, OutputStream out) {
		/*
		 * Quality of the image. 0 to 100 and from bad image quality, high compression
		 * to good image quality low compression
		 */


		/*
		 * Getting picture information It takes the Width, Height and RGB scans of the
		 * image.
		 */
		jpegInfo = new JpegInfo(image);

		imageHeight = jpegInfo.imageHeight;
		imageWidth = jpegInfo.imageWidth;
		outStream = new BufferedOutputStream(out);
		dct = new DCT(quality);
		huffman = new Huffman();
	}

	public void Compress() throws IOException {
		WriteHeaders(outStream);
		WriteCompressedData(outStream);
		WriteEOI(outStream);
		outStream.flush();
	}

	void WriteCompressedData(BufferedOutputStream outStream) throws IOException {
		int i, j, r, c, a, b;
		int comp, xpos, ypos, xblockoffset, yblockoffset;
		float[][] inputArray;
		float[][] dctArray1 = new float[8][8];
		double[][] dctArray2;
		int[] dctArray3;

		/*
		 * This method controls the compression of the image. Starting at the upper left
		 * of the image, it compresses 8x8 blocks of data until the entire image has
		 * been compressed.
		 */

		int[] lastDCvalue = new int[jpegInfo.numberOfComponents];
		int MinBlockWidth, MinBlockHeight;
//This initial setting of MinBlockWidth and MinBlockHeight is done to
//ensure they start with values larger than will actually be the case.
		MinBlockWidth = ((imageWidth % 8 != 0) ? (int) (Math.floor((double) imageWidth / 8.0) + 1) * 8 : imageWidth);
		MinBlockHeight = ((imageHeight % 8 != 0) ? (int) (Math.floor((double) imageHeight / 8.0) + 1) * 8
				: imageHeight);
		for (comp = 0; comp < jpegInfo.numberOfComponents; comp++) {
			MinBlockWidth = Math.min(MinBlockWidth, jpegInfo.blockWidth[comp]);
			MinBlockHeight = Math.min(MinBlockHeight, jpegInfo.blockHeight[comp]);
		}
		for (r = 0; r < MinBlockHeight; r++) {
			for (c = 0; c < MinBlockWidth; c++) {
				xpos = c * 8;
				ypos = r * 8;
				for (comp = 0; comp < jpegInfo.numberOfComponents; comp++) {
					inputArray = jpegInfo.components[comp];

					for (i = 0; i < jpegInfo.verticalSamplingFactor[comp]; i++) {
						for (j = 0; j < jpegInfo.horizontalSamplingFactor[comp]; j++) {
							xblockoffset = j * 8;
							yblockoffset = i * 8;
							for (a = 0; a < 8; a++) {
								for (b = 0; b < 8; b++) {

//I believe this is where the dirty line at the bottom of the image is
//coming from.  I need to do a check here to make sure I'm not reading past
//image data.
//This seems to not be a big issue right now. (04/04/98)

									dctArray1[a][b] = inputArray[ypos + yblockoffset + a][xpos + xblockoffset + b];
								}
							}
//The following code commented out because on some images this technique
//results in poor right and bottom borders.
//                     if ((!JpegObj.lastColumnIsDummy[comp] || c < Width - 1) && (!JpegObj.lastRowIsDummy[comp] || r < Height - 1)) {
							dctArray2 = dct.forwardDCT(dctArray1);
							dctArray3 = dct.quantizeBlock(dctArray2, jpegInfo.quantizationTableNumber[comp]);
//                     }
//                     else {
//                        zeroArray[0] = dctArray3[0];
//                        zeroArray[0] = lastDCvalue[comp];
//                        dctArray3 = zeroArray;
//                     }
							huffman.HuffmanBlockEncoder(outStream, dctArray3, lastDCvalue[comp],
									jpegInfo.directCurrentTableNumber[comp],
									jpegInfo.alternatingCurrentTableNumber[comp]);
							lastDCvalue[comp] = dctArray3[0];
						}
					}
				}
			}
		}
		huffman.flushBuffer(outStream);
	}

	void WriteEOI(BufferedOutputStream out) throws IOException {
		byte[] EOI = { (byte) 0xFF, (byte) 0xD9 };
		WriteMarker(EOI, out);
	}

	void WriteHeaders(BufferedOutputStream out) throws IOException {
//the SOI marker
		WriteMarker(SOI, out);

//The order of the following headers is quiet inconsequential.
//the JFIF header
		WriteArray(JFIF, out);

//Comment Header
		WriteArray(jpegInfo.getCommentHeader(), out);

//The DQT header
		WriteArray(getDQT(), out);

//Start of Frame Header
		WriteArray(jpegInfo.getStartOfHeader(), out);

//The DHT Header

		WriteArray(huffman.getDHT(), out);

//Start of Scan Header
		WriteArray(jpegInfo.getStartOfScanHeader(), out);

	}

	private byte[] getDQT() {
		//0 is the luminance index and 1 is the chrominance index
		int j;
		int offset;
		int i;
		int[] tempArray;
		byte[] DQT = new byte[134];
		DQT[0] = (byte) 0xFF;
		DQT[1] = (byte) 0xDB;
		DQT[2] = (byte) 0x00;
		DQT[3] = (byte) 0x84;
		offset = 4;
		for (i = 0; i < 2; i++) {
			DQT[offset++] = (byte) (i);
			tempArray = (int[]) dct.quantum[i];
			for (j = 0; j < 64; j++) {
				DQT[offset++] = (byte) tempArray[jpegNaturalOrder[j]];
			}
		}
		return DQT;
	}

	void WriteMarker(byte[] data, BufferedOutputStream out) throws IOException {
		out.write(data, 0, 2);
	}

	void WriteArray(byte[] data, BufferedOutputStream out) throws IOException {
		int length;
		length = ((data[2] & 0xFF) << 8) + (data[3] & 0xFF) + 2;
		out.write(data, 0, length);
	}
}

//This class incorporates quality scaling as implemented in the JPEG-6a
//library.

/*
 * DCT - A Java implementation of the Discreet Cosine Transform
 */

//This class was modified by James R. Weeks on 3/27/98.
//It now incorporates Huffman table derivation as in the C jpeg library
//from the IJG, Jpeg-6a.

/*
 * JpegInfo - Given an image, sets default information about it and divides it
 * into its constituant components, downsizing those that need to be.
 */

