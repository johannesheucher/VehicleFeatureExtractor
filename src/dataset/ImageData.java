package dataset;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import processing.NumberPlateExtractor;
import processing.RoIExtractor;

public class ImageData {
	private Mat mat;
	private BufferedImage image;
	private boolean imageOutdated = true;
	
	private String name;
	private File filename;
	
	
	public ImageData(File filename) {
		this(Imgcodecs.imread(filename.toString()));
		this.filename = filename;
		name = filename.getName();
	}
	
	
	public ImageData(Mat mat) {
		setMat(mat);
	}
	
	
	public File getFilename() {
		return filename;
	}
	
	
	public String getName() {
		return name;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public String getMake() {
//		Pattern p = Pattern.compile("([^_]+)\\w*");
//		String name = getName().toLowerCase();
//		Matcher matcher = p.matcher(name);
//		matcher.find();
		String[] parts = getName().toLowerCase().split("_");
		return parts[0].replaceAll("\\s+", "_");
	}
	
	
	public String getModel() {
//		Matcher matcher = Pattern.compile("[^_]+_([^_]+)\\w*").matcher(getName().toLowerCase());
//		matcher.find();
		String[] parts = getName().toLowerCase().split("_");
		return parts[1].replaceAll("\\s+", "_");
	}
	
	
	public String getMakeModel() {
		return getMake() + "_" + getModel();
	}
	
	
	public String getVehicleKey() {
		return getName().substring(0, getName().lastIndexOf("."));
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
	
	
	public void toGray() {
		Imgproc.cvtColor(getMat(), getMat(), Imgproc.COLOR_BGR2GRAY);
		needsRefreshImage();
	}
	
	
	public Mat extractRoI() {
		// make gray
		Mat gray = new Mat();
		Imgproc.cvtColor(getMat(), gray, Imgproc.COLOR_BGR2GRAY);
		
		// detect number plate
		Mat matRoI = null;
		Range rowRange = new Range();
		Range colRange = new Range();
		NumberPlateExtractor.calculateCropOffset(gray, rowRange, colRange);
		Mat sub = gray.submat(rowRange, colRange);
		Rect rect = NumberPlateExtractor.extract(sub);
		if (rect != null) {
			matRoI = RoIExtractor.extract(sub, rect);
		}
		return matRoI;
	}
	
	
	@Override
	public Object clone() {
		ImageData cloned = new ImageData(mat.clone());
		cloned.filename = getFilename();
		cloned.name = getName();
		return cloned;
	}
	
	
	public void display() {
		Image image = getImage();
		JOptionPane.showMessageDialog(null, new ImageIcon(image));
	}
	
	
	private static BufferedImage bufferedImageFromMat(Mat m) {
		// source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
	    int type = BufferedImage.TYPE_BYTE_GRAY;
	    if (m.channels() == 3) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    }
	    if (m.channels() == 4) {
	        type = BufferedImage.TYPE_4BYTE_ABGR;
	    }
	    int bufferSize = m.channels() * m.cols() * m.rows();
	    byte[] b = new byte[bufferSize];
	    m.get(0, 0, b); // get all the pixels
	    BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
	    final byte[] targetPixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
	    System.arraycopy(b, 0, targetPixels, 0, b.length);
	    return image;
	}
	
	
	public void save(File filename) {
		Imgcodecs.imwrite(filename.toString(), getMat());
	}
}
