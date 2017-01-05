import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	private static final int DICTIONARY_SIZE = 50;
	
	public static void main(String[] args) throws IOException {
		// load training set and build dictionary
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Dataset trainingSet = new Dataset(new File("D:/workspaces/Vehicle Data/dictionarytest/train"), true);
		
		List<Mat> trainingDescriptorsList = new ArrayList<>();
		for (ImageData image : trainingSet.getImageList()) {
			Mat descriptors = new Mat();
			FeatureExtractor.extract(image.getMat(), new MatOfKeyPoint(), descriptors);
			trainingDescriptorsList.add(descriptors);
		}
	    Dictionary dictionary = new Dictionary(trainingDescriptorsList, DICTIONARY_SIZE);
	    
	    
	    // test dictionary with both training set and test set
	    // build histograms
	    Dataset testSet = new Dataset(new File("D:/workspaces/Vehicle Data/dictionarytest/train"), true);
	    testSet.addData(new File("D:/workspaces/Vehicle Data/dictionarytest/test"), true);
	    List<double[]> histograms = new ArrayList<>();
	    for (ImageData image : testSet.getImageList()) {
	    	histograms.add(getHistogram(image, dictionary));
	    	double sum = 0;
	    	for (double bin : histograms.get(histograms.size() - 1)) {
		    	sum += bin;
		    }
	    	System.out.println("sum = " + sum);
	    }
	    saveHistograms(testSet, histograms, "histograms.csv");
	    
	    // build confusion matrix
	    double[][] confusionMatrix = new double[dictionary.getSize()][dictionary.getSize()];		too big! shall only be testData.length!
	    for (int i = 0; i < histograms.size(); i++) {
	    	for (int j = 0; j < histograms.size(); j++) {
	    		confusionMatrix[i][j] = ssd(histograms.get(i), histograms.get(j));
	    	}
	    }
	    BufferedWriter writer = new BufferedWriter(new FileWriter("confusion.csv"));
	    StringBuilder builder = new StringBuilder();
	    
	    for (int i = 0; i < confusionMatrix.length; i++) {
	    	for (int j = 0; j < confusionMatrix.length; j++) {
	    		if (j == 0) {
	    			builder.append(testSet.getImageList().get(i).getName() + ",");
	    		}
	    		builder.append(String.valueOf(confusionMatrix[i][j]) + ",");
	    	}
	    	builder.append(System.lineSeparator());
	    }
	    writer.write(builder.toString());
		writer.close();
	}
	
	
	public static double ssd(double[] histogram0, double[] histogram1) {
		double sum = 0;
		for (int i = 0; i < histogram0.length; i++) {
			double difference = histogram0[i] - histogram1[i];
			sum += difference * difference;
		}
		return sum;
	}
	
	
	public static double[] getHistogram(ImageData image, Dictionary dictionary) {
		Mat descriptors = new Mat();
		FeatureExtractor.extract(image.getMat(), new MatOfKeyPoint(), descriptors);
	    
	    // match features
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
		MatOfDMatch matches = new MatOfDMatch();
	    matcher.match(descriptors, dictionary.getCodewords(), matches);
	    DMatch[] matchesArray = matches.toArray();
	    double[] histogram = new double[dictionary.getSize()];
	    for (DMatch match : matchesArray) {
	    	histogram[match.trainIdx] += 1.0 / descriptors.rows();		// includes normalization
	    }
	    
	    return histogram;
	}
	
	
	public static void saveHistograms(Dataset dataset, List<double[]> histograms, String filename) throws IOException {
		// write histograms to file
	    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
	    StringBuilder builder = new StringBuilder();
	    // - labels
	    builder.append("name,");
	    for (int i = 0; i < DICTIONARY_SIZE; i++) {
	    	builder.append("bin" + String.valueOf(i) + ",");
	    }
	    builder.append(System.lineSeparator());
	    
	    // - data
		for (int i = 0; i < dataset.getImageList().size(); i++) {
		    builder.append(dataset.getImageList().get(i).getName() + ",");
		    for (double bin : histograms.get(i)) {
		    	builder.append(String.valueOf(bin) + ",");
		    }
		    builder.append(System.lineSeparator());
		}
		
		writer.write(builder.toString());
		writer.close();
	}
}
