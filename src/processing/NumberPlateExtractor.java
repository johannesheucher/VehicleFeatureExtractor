package processing;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import dataset.ImageData;

public class NumberPlateExtractor {
	
	public static List<Point> extract(ImageData image, Size kernelSize) {
		Mat mat = new Mat();
		
		// convert to gray scale
		toGray(image.getMat(), mat);
		
		topHat(mat, mat, kernelSize);
		
		return null;
	}
	
	
	public static void toGray(Mat src, Mat dst) {
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
	}
	
	
	public static void topHat(Mat src, Mat dst, Size kernelSize) {
		// morphological operation - Top Hat (difference between image and opening of the image)
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize);
		Imgproc.morphologyEx(src, dst, Imgproc.MORPH_TOPHAT, kernel);
	}
}
