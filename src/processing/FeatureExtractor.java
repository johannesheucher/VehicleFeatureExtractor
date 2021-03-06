package processing;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;

public class FeatureExtractor {
	private static FeatureDetector detector = null;
	private static DescriptorExtractor descriptorExtractor = null;
	
	/**
	 * TODO
	 * @param src Input
	 * @param descriptors Output
	 */
	public static void extract(Mat src, MatOfKeyPoint keyPoints, Mat descriptors) {
		if (src.type() != CvType.CV_8UC1) {
			throw new RuntimeException("Expected Mat type 8UC1");
		}
		if (detector == null || descriptorExtractor == null) {
			detector = FeatureDetector.create(FeatureDetector.ORB);
			descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		}
		
		// extract descriptors
		detector.detect(src, keyPoints);
//		Mat descriptors8u = new Mat();
		descriptorExtractor.compute(src, keyPoints, descriptors);
		
		// convert descriptors' type from U8 to 32F
//		descriptors8u.convertTo(descriptors, CvType.CV_32F);
	}
}
