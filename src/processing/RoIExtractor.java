package processing;

import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Rect;

public class RoIExtractor {
	
	public static final double ROI_TOP_FACTOR = 0.9;
	public static final double ROI_BOTTOM_FACTOR = 0.2;
	public static final double ROI_LEFT_FACTOR = 0.9;		// higher value than right, because EU area of number plate is not part of the rect
	public static final double ROI_RIGHT_FACTOR = 0.8;
	
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
		
		return src.submat(rowRange, colRange);
	}
}
