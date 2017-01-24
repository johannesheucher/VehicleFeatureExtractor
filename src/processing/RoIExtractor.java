package processing;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class RoIExtractor {
	
	public static final double ROI_TOP_FACTOR = 0.7;
	public static final double ROI_BOTTOM_FACTOR = 0.1;
	public static final double ROI_LEFT_FACTOR = 0.6;		// higher value than right, because EU area of number plate is not part of the rect
	public static final double ROI_RIGHT_FACTOR = 0.6;
	
	/**
	 * Region of Interest
	 * <pre>
	 *                                    TOP_FACTOR * w
	 *                                          +
	 *
	 *                             ____________ w ____________
	 *                            |                           |
	 *      LEFT_FACTOR * w +     h       Number Plate        |     + RIGHT_FACTOR * w
	 *                            |___________________________|
	 *
	 *                                          +
	 *                                  BOTTOM_FACTOR * w
	 * </pre>
	 */
	public static Mat extract(Mat src, Rect numberPlate) {
		int w = numberPlate.width;
		int h = numberPlate.height;
		
		Range rowRange = new Range((int)Math.max(numberPlate.y - ROI_TOP_FACTOR * w, 0), (int)Math.min(numberPlate.y + h + ROI_BOTTOM_FACTOR * w, src.rows() - 1));
		Range colRange = new Range((int)Math.max(numberPlate.x - ROI_LEFT_FACTOR * w, 0), (int)Math.min(numberPlate.x + w + ROI_RIGHT_FACTOR * w, src.cols() - 1));
		
		Mat submat = src.submat(rowRange, colRange);
		
		// delete content of number plate
		Point p = new Point(numberPlate.x - colRange.start, numberPlate.y - rowRange.start);
		Point q = new Point(numberPlate.x + numberPlate.width - colRange.start, numberPlate.y + numberPlate.height - rowRange.start);
		Imgproc.rectangle(submat, p, q, new Scalar(0), -1);
		
		return submat;
	}
}
