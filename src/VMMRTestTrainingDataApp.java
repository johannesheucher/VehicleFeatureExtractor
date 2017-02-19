import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;

import dataset.ImageData;

public class VMMRTestTrainingDataApp {
	private static final String INPUT_PATH = "D:/workspaces/VehicleData/raw/";
	private static final String OUTPUT_PATH = "D:/workspaces/VehicleData/ARFF/";
	
	/**
	 * Training images by vehicle name
	 */
	private Map<String, List<ImageData>> trainingData;
	
	
	public VMMRTestTrainingDataApp() {
		TrainingDataCreatorApp trainingDataCreator = new TrainingDataCreatorApp(INPUT_PATH);
		
		trainingData = new HashMap<>();
		for (ImageData image : trainingDataCreator.getTrainingData()) {
			if (trainingDataCreator.getDataset().getVehicleCounts().get(image.getMakeModel()) >= 9) {
				String makeModel = image.getMakeModel();
				List<ImageData> vehicles;
				if (trainingData.containsKey(makeModel)) {
					vehicles = trainingData.get(makeModel);
				} else {
					vehicles = new ArrayList<>();
					trainingData.put(makeModel, vehicles);
				}
				vehicles.add(image);
			}
		}
		
		// permute all combinations of 8 make-models and create an ARFF file
		
		// get true positive rate for each ARFF file (VMMRTestVehicleApp(ARFF_Filename))
	}
	
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new VMMRTestTrainingDataApp();
	}
}
