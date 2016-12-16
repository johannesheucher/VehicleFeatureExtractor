package dataset;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageData {
	private Mat mat;
	private BufferedImage image;
	private boolean imageOutdated = true;
	
	public ImageData(File filename) {
		this(Imgcodecs.imread(filename.toString()));
	}
	
	
	public ImageData(Mat mat) {
		setMat(mat);
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
	
	
	public void needsRefreshImage() {
		imageOutdated = true;
	}
	
	
	private void rebuildImage() {
		image = bufferedImageFromMat(mat);
		needsRefreshImage();
	}
	
	
	public void resize(Size size) {
		Mat resizedMat = new Mat();
		Imgproc.resize(mat, resizedMat, size);
		setMat(resizedMat);
	}
	
	
	public void resize(int width) {
		// resize and keep aspect ratio
		float ratio = mat.width() / (float)mat.height();
		int height = (int)(width / ratio);
		resize(new Size(width, height));
	}
	
	
	@Override
	public Object clone() {
		return new ImageData(mat.clone());
	}
	
	
	public void display() {
		Image image = getImage();
		JOptionPane.showMessageDialog(null, new ImageIcon(image));
	}
	
	
	private static BufferedImage bufferedImageFromMat(Mat m) {
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
}
