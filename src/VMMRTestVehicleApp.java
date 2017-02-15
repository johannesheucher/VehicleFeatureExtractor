import java.io.File;

import org.opencv.core.Core;

import dataset.BoFHistogram;
import dataset.Dataset;
import dataset.Dictionary;
import dataset.ImageData;
import processing.vmmr.ClassifierType;
import processing.vmmr.VMMRecognizer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class VMMRTestVehicleApp {
	private static String ARFF_FILENAME			= "D:/workspaces/VehicleData/training/_vehicles_30.arff";
	private static String DICTIONARY_FILENAME	= "D:/workspaces/VehicleData/training/_dictionary_30.bytes";
	private static String TESTING_PATH			= "D:/workspaces/VehicleData/testing/";
	
	VMMRecognizer recognizer;
	private Dictionary dictionary;
	
	public VMMRTestVehicleApp() throws Exception {
		// train classifier
		DataSource source = new DataSource(ARFF_FILENAME);
		Instances instances = source.getDataSet(0);
		
		recognizer = new VMMRecognizer();
		String options = "-C 2 -N 2 -L 1.0e-3 -P 1.0e-11  -K \"weka.classifiers.functions.supportVector.NormalizedPolyKernel -E 2.0 -C -1\"";
		double correct = recognizer.train(ClassifierType.SVM, instances, weka.core.Utils.splitOptions(options));		// TODO: Fill with best options
		
		String s = String.format("classification performance: %.1f%% correct\n", correct/instances.size() * 100);
		System.out.print(s);
		
		// load dictionary
		dictionary = new Dictionary(DICTIONARY_FILENAME);
	}
	
	
	public String classifyImage(ImageData image) {
		BoFHistogram globalDescriptor = new BoFHistogram(image,	dictionary);
		Instance instance = globalDescriptor.toInstance();
		return recognizer.classify(instance);
	}
	
	
	public static void main(String[] args) throws Exception {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		VMMRTestVehicleApp app = new VMMRTestVehicleApp();
		
		// load test instance and classify
		Dataset dataset = new Dataset(new File(TESTING_PATH), false);
		for (ImageData image : dataset.getImageList()) {
			String label = app.classifyImage(image);
			System.out.printf("%s classified as:\t\t%s\n", image.getName(), label);
		}
	}
}
