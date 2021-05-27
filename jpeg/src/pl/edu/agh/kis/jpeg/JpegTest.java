package pl.edu.agh.kis.jpeg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;


public class JpegTest {
	
	public static void main(String[] args) {
		String inputImagePath = "res/img1.bmp";
		String outputImagePath = "res/out1.jpg";
		
		try {
			BufferedImage img = ImageIO.read(new File(inputImagePath));
			FileOutputStream fos = new FileOutputStream(outputImagePath);

			JpegEncoder jpegEncoder = new JpegEncoder(img, 20, fos);
			jpegEncoder.Compress();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("end.");
	}
}
