package dataset;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;

public class ImageData {
	private BufferedImage image;
	
	public ImageData(BufferedImage image) {
		this.image = image;
	}
	
	
	public ImageData(File filename) throws IOException {
		this(ImageIO.read(filename));
	}
	
	
	public BufferedImage getImage() {
		return image;
	}
	
	
	public Mat getMat() {
		// @TODO
		return null;
	}
}
