import java.io.File;
import java.io.IOException;

import org.opencv.core.Core;

import dataset.Dataset;
import dataset.ImageData;
import dataset.NumberPlateMetaFile;
import processing.vmmr.ClassifierType;
import processing.vmmr.VMMRecognizer;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class VMMRTestVehicleApp {
	private static String ARFF_FILENAME = "D:/workspaces/VehicleData/training/_vehicles_30.arff";
	private static String INPUT_PATH = "D:/workspaces/VehicleData/testing/";
	
	public static void main(String[] args) throws Exception {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		DataSource source = new DataSource(ARFF_FILENAME);
		Instances instances = source.getDataSet(0);
		
		VMMRecognizer recognizer = new VMMRecognizer();
		double correct = recognizer.train(ClassifierType.SVM, instances, null);
		
		String s = String.format("correct: %.0f\n", correct);
		System.out.print(s);
		
		
		Dataset dataset = new Dataset(new File(INPUT_PATH), false);
		for (ImageData image : dataset.getImageList()) {
			
		}
	}
}
