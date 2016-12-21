package processing;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
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
	
	
	public static boolean isNumberPlate(MatOfPoint contour) {
		Rect rect = Imgproc.boundingRect(contour);
		
		//  rough check: size and aspect ratio
    	if (rect.width  > Const.NUMBER_PLATE_WIDTH[0]  && rect.width  < Const.NUMBER_PLATE_WIDTH[1] &&
    		rect.height > Const.NUMBER_PLATE_HEIGHT[0] && rect.height < Const.NUMBER_PLATE_HEIGHT[1] &&
    		Math.abs(rect.width / rect.height - Const.NUMBER_PLATE_ASPECT_RATIO) < Const.NUMBER_PLATE_ASPECT_RATIO_VARIANCE) {
    		
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
