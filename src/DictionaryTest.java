import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorMatcher;

import dataset.Dataset;
import dataset.Dictionary;
import dataset.ImageData;
import processing.FeatureExtractor;
import processing.NumberPlateExtractor;

public class DictionaryTest {

	public static void main(String[] args) {
		// load and train training set
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Dataset trainingSet = new Dataset(new File("D:/workspaces/Vehicle Data/dictionarytest/train"));
		
		List<Mat> trainingDescriptorsList = new ArrayList<>();
		for (ImageData image : trainingSet.getImageList()) {
			NumberPlateExtractor.toGray(image.getMat(), image.getMat());
			Mat descriptors = new Mat();
			FeatureExtractor.extract(image.getMat(), new MatOfKeyPoint(), descriptors);
			trainingDescriptorsList.add(descriptors);
		}
	    Dictionary dictionary = new Dictionary(trainingDescriptorsList, 50);
	    
	    
	    // create histograms
		for (ImageData image : trainingSet.getImageList()) {
			Mat descriptors = new Mat();
			FeatureExtractor.extract(image.getMat(), new MatOfKeyPoint(), descriptors);
		    
		    // match features
			DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
			MatOfDMatch matches = new MatOfDMatch();
		    matcher.match(descriptors, dictionary.getCodewords(), matches);
		    DMatch[] matchesArray = matches.toArray();
		    int[] histogram = new int[50];
		    for (DMatch match : matchesArray) {
		    	histogram[match.trainIdx]++;
		    }
		    System.out.println(image.getName() + ":   " + Arrays.toString(histogram));
		    
		    TODO: draw histograms into an image and save to file (fastest way to check similarities)
		}
		
		// load test set
//	    Dataset testSet = new Dataset(new File("D:/workspaces/Vehicle Data/dictionarytest/test"));
//	    List<Mat> testDescriptorsList = new ArrayList<>();
	}
}
