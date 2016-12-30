package processing;

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
		if (detector == null || descriptorExtractor == null) {
			detector = FeatureDetector.create(FeatureDetector.ORB);
			descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		}
		
		// extract descriptors
		detector.detect(src, keyPoints);
		descriptorExtractor.compute(src, keyPoints, descriptors);
	}
}
