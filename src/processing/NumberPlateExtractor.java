package processing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class NumberPlateExtractor {
	
	private static final int GAUSS_KERNEL_SIZE = 7;
	
	/**
	 * Extracts bounding region around found number plate or <strong>null</strong>
	 * @param src Must contain a gray mat
	 * @return Bounding region or <strong>null</strong>
	 */
	public static Rect extract(Mat src) {
		Mat mat = new Mat();
		
		// blur
		Imgproc.GaussianBlur(src, mat, new Size(GAUSS_KERNEL_SIZE, GAUSS_KERNEL_SIZE), 1, 1);
		
		// binarize
		Imgproc.threshold(mat, mat, 130, 255, Imgproc.THRESH_BINARY);
		
		// detect edges
		Imgproc.Canny(mat, mat, 50, 200, 3, true);
		
		// detect contours
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	    Imgproc.findContours(mat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// filter for potential number plate contours
		for (int i = 0; i < contours.size(); i++) {
			Rect rect = Imgproc.boundingRect(contours.get(i));
			if (NumberPlateExtractor.isNumberPlate(contours.get(i), rect)) {
				return rect;
	    	}
		}
		
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
	
	
	private static boolean isNumberPlate(MatOfPoint contour, Rect boundingRect) {
		//  rough check: size and aspect ratio
    	if (boundingRect.width  > Const.NUMBER_PLATE_MIN_WIDTH  && boundingRect.width  < Const.NUMBER_PLATE_MAX_WIDTH &&
    		boundingRect.height > Const.NUMBER_PLATE_MIN_HEIGHT && boundingRect.height < Const.NUMBER_PLATE_MAX_HEIGHT &&
    		Math.abs(boundingRect.width / boundingRect.height - Const.NUMBER_PLATE_ASPECT_RATIO) < Const.NUMBER_PLATE_ASPECT_RATIO_EPSILON) {
    		
    		// TODO: Better check could be to perform matchTemplate on the contour:
    		// 1. Build template as bounding box of contour
    		// 2. Match this template with the contour
    		// remark: Which line width is best?
    		// remark: Maybe, it is simpler to implement this on my own instead of using overheaded matchTemplate
    		
    		// more expensive check: shape by approximating contour with a polygon
    		MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
    		double perimeter = Imgproc.arcLength(contour2f, true);
    		MatOfPoint2f approx = new MatOfPoint2f();
    		Imgproc.approxPolyDP(contour2f, approx, 0.04 * perimeter, true);
    		
    		return (approx.rows() >= 2 && approx.rows() <= 6);
    	}
		return false;
	}
}
