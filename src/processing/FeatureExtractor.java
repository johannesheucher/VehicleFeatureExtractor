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
	public static void extract(Mat src, Mat descriptors) {
		if (detector == null || descriptorExtractor == null) {
			detector = FeatureDetector.create(FeatureDetector.SURF);
			descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
		}
		
		// extract descriptors
		MatOfKeyPoint keyPoints = new MatOfKeyPoint();
		detector.detect(src, keyPoints);
		descriptorExtractor.compute(src, keyPoints, descriptors);
	}
}
