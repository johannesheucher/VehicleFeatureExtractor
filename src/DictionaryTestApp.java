import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorMatcher;

import dataset.BoFHistogram;
import dataset.Dataset;
import dataset.Dictionary;
import dataset.ImageData;
import processing.FeatureExtractor;

public class DictionaryTestApp {
	private static final int DICTIONARY_SIZE = 200;
	private static final String INPUT_PATH = "D:/workspaces/VehicleData/training";
	private static final String OUTPUT_PATH = "D:/workspaces/VehicleData/training/";
	private static final String ARFF_FILENAME = "_vehicles.arff";
	
	public static void main(String[] args) throws IOException {
		// load training set and build dictionary
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Dataset trainingSet = new Dataset(new File(INPUT_PATH), false);
		
		List<Mat> trainingDescriptorsList = new ArrayList<>();
		for (ImageData image : trainingSet.getImageList()) {
			Mat descriptors = new Mat();
			FeatureExtractor.extract(image.getMat(), new MatOfKeyPoint(), descriptors);
			trainingDescriptorsList.add(descriptors);
		}
	    Dictionary dictionary = new Dictionary(trainingDescriptorsList, DICTIONARY_SIZE);
	    
	    
	    // build histograms
	    List<BoFHistogram> histograms = new ArrayList<>();
	    for (ImageData image : trainingSet.getImageList()) {
	    	histograms.add(new BoFHistogram(image, dictionary));
	    	double sum = 0;
	    	for (Double bin : histograms.get(histograms.size() - 1).getValues()) {
		    	sum += bin;
		    }
	    	System.out.println("sum = " + sum);
	    }
	    toArff(histograms, new File(OUTPUT_PATH + ARFF_FILENAME));
	    
//	    // build confusion matrix
//	    int numImages = testSet.getImageList().size();
//	    double[][] confusionMatrix = new double[numImages][numImages];
//	    for (int i = 0; i < numImages; i++) {
//	    	for (int j = 0; j < numImages; j++) {
//	    		confusionMatrix[i][j] = ssd(histograms.get(i), histograms.get(j));
//	    	}
//	    }
//	    BufferedWriter writer = new BufferedWriter(new FileWriter("confusion.csv"));
//	    StringBuilder builder = new StringBuilder();
//	    
//	    for (int i = 0; i < confusionMatrix.length; i++) {
//	    	for (int j = 0; j < confusionMatrix.length; j++) {
//	    		if (j == 0) {
//	    			builder.append(testSet.getImageList().get(i).getName() + ",");
//	    		}
//	    		builder.append(String.valueOf(confusionMatrix[i][j]) + ",");
//	    	}
//	    	builder.append(System.lineSeparator());
//	    }
//	    writer.write(builder.toString());
//		writer.close();
	}
	
	
	public static void toArff(List<BoFHistogram> histograms, File filename) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append("@relation " + filename.getName() + System.lineSeparator() + System.lineSeparator());
		
		// attributes
		Set<String> makemodels = new HashSet<>();
		for (BoFHistogram histogram : histograms) {
			String makemodel = histogram.getSource().getMakeModel();
			//if (makemodel.contains("porsche") || makemodel.contains("golf") || makemodel.contains("corsa")) {
				makemodels.add(makemodel);
			//}
		}
		StringBuilder makemodelBuilder = new StringBuilder();
		Iterator<String> makemodelIterator = makemodels.iterator();
		while (makemodelIterator.hasNext()) {
			makemodelBuilder.append(makemodelIterator.next());
			makemodelBuilder.append(",");
		}
		makemodelBuilder.deleteCharAt(makemodelBuilder.length() - 1);
		builder.append("@attribute makemodel {" + makemodelBuilder.toString() + "}" + System.lineSeparator());
		
		for (int i = 0; i < histograms.get(0).getValues().size(); i++) {
			builder.append("@attribute feature" + String.valueOf(i) + " numeric" + System.lineSeparator());
		}
		
		builder.append(System.lineSeparator() + "@data" + System.lineSeparator());
		for (BoFHistogram histogram : histograms) {
			String makemodel = histogram.getSource().getMakeModel();
			if (makemodels.contains(makemodel)) {
				builder.append(makemodel + ",");
				builder.append(histogram.toCSV() + System.lineSeparator());
			}
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
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
	
	
	public static void saveHistograms(Dataset dataset, List<BoFHistogram> histograms, String filename) throws IOException {
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
		    for (double bin : histograms.get(i).getValues()) {
		    	builder.append(String.valueOf(bin) + ",");
		    }
		    builder.append(System.lineSeparator());
		}
		
		writer.write(builder.toString());
		writer.close();
	}
}
