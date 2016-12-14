package dataset;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ImageData {
	private Mat mat;
	private BufferedImage image;
	private boolean imageOutdated = true;
	
	public ImageData(File filename) {
		setMat(Imgcodecs.imread(filename.toString()));
	}
	
	
	public Mat getMat() {
		return mat;
	}
	
	
	public void setMat(Mat mat) {
		this.mat = mat;
		imageOutdated = true;
	}
	
	
	public BufferedImage getImage() {
		if (imageOutdated) {
			rebuildImage();
		}
		return image;
	}
	
	
	private void rebuildImage() {
		image = bufferedImageFromMat(mat);
		imageOutdated = false;
	}
	
	
	public static BufferedImage bufferedImageFromMat(Mat m) {
		// source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
	    int type = BufferedImage.TYPE_BYTE_GRAY;
	    if (m.channels() > 1) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    }
	    int bufferSize = m.channels() * m.cols() * m.rows();
	    byte[] b = new byte[bufferSize];
	    m.get(0, 0, b); // get all the pixels
	    BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
	    final byte[] targetPixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
	    System.arraycopy(b, 0, targetPixels, 0, b.length);
	    return image;
	}
	
	
	public void display() {
		Image image = getImage();
		JOptionPane.showMessageDialog(null, new ImageIcon(image));
	}
}
