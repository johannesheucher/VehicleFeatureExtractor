package dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorMatcher;

import processing.FeatureExtractor;

/**
 * Global descriptor for a make-model. Bag of Features (BoF).
 * Uses a normalized histogram indicating the number of features found.
 * @author Johannes
 */
public class BoFHistogram {
	private List<Double> values;
	private ImageData source;

	public BoFHistogram(ImageData source, Dictionary dictionary) {
		update(source, dictionary);
	}


	public void update(ImageData source, Dictionary dictionary) {
		this.source = source;
		Mat descriptors = new Mat();
		FeatureExtractor.extract(source.getMat(), new MatOfKeyPoint(), descriptors);

		// match features
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
		MatOfDMatch matches = new MatOfDMatch();
		matcher.match(descriptors, dictionary.getCodewords(), matches);
		DMatch[] matchesArray = matches.toArray();
		Double[] histogram = new Double[dictionary.getSize()];
		for (int i = 0; i < histogram.length; i++) {
			histogram[i] = 0.0;
		}
		for (DMatch match : matchesArray) {
			histogram[match.trainIdx] += 1.0 / descriptors.rows();		// includes normalization
		}
		values = new ArrayList<Double>(Arrays.asList(histogram));
	}


	public List<Double> getValues() {
		return values;
	}
	
	
	public ImageData getSource() {
		return source;
	}


	public String toCSV() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < values.size(); i++) {
			builder.append(String.valueOf(values.get(i)));
			if (i < values.size() - 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}
}
