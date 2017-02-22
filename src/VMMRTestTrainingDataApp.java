import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.core.Core;

import dataset.ImageData;

public class VMMRTestTrainingDataApp {
	private static final String INPUT_PATH = "D:/workspaces/VehicleData/raw/";
	private static final String OUTPUT_PATH = "D:/workspaces/VehicleData/ARFF/";
	private static final int SUBSET_SIZE = 13;
	
	/**
	 * Training images by vehicle name
	 */
	private Map<String, List<ImageData>> trainingData;
	
	
	public VMMRTestTrainingDataApp() throws IOException {
		TrainingDataCreatorApp trainingDataCreator = new TrainingDataCreatorApp(INPUT_PATH);
		
		trainingData = new HashMap<>();
		for (ImageData image : trainingDataCreator.getTrainingData()) {
			if (trainingDataCreator.getDataset().getVehicleCounts().get(image.getMakeModel()) >= 8) {
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
		
		// permute all combinations of SUBSET_SIZE make-models
		List<Set<String>> subsets = VMMRTestTrainingDataApp.getSubsetsWithLength(new ArrayList<>(trainingData.keySet()), SUBSET_SIZE);
		for (Set<String> subset : subsets) {
			System.out.println(subset.toString());
		}
		
		// create an ARFF file for each subset
		int id = 0;
		for (Set<String> subset : subsets) {
			List<ImageData> subsetImages = getImages(subset);
			String dataId = String.format("%04d", id);
			id++;
			DictionaryTestApp.buildDictionaryAndARFF(subsetImages, DictionaryTestApp.DICTIONARY_SIZE, OUTPUT_PATH, dataId);
			System.out.printf("exported classification data %d / %d\n", id, subsets.size());
		}
		
		// BEFORE: Do a test: export the 7 vehicles from arff_30 the same way (subset = {"vw_golf_iv, mazda_mx-5, ..."})
		// Then test dict and arff. Should result in 78.3. If not, there is something wrong in this process here!!!
		
		// BEFORE: Add Tesla and Cactus to dataset
		// get true positive rate and confusion matrix for each ARFF file (VMMRTestVehicleApp(ARFF_Filename))
		// then for the best combinations, odd out the worst performing make-models and test again
	}
	
	
	private List<ImageData> getImages(Set<String> vehicleNames) {
		List<ImageData> resultList = new ArrayList<>();
		for (String vehicleName : vehicleNames) {
			List<ImageData> images = trainingData.get(vehicleName);
			resultList.addAll(images);
		}
		return resultList;
	}
	
	
	private static List<Set<String>> getSubsetsWithLength(List<String> superset, int length) {
		List<Set<String>> resultList = new ArrayList<>();
		Set<Set<String>> subsets = VMMRTestTrainingDataApp.getSubsets(superset);
		Iterator<Set<String>> outerIterator = subsets.iterator();
		while (outerIterator.hasNext()) {
			Set<String> subset = outerIterator.next();
			if (subset.size() == length) {
				resultList.add(subset);
			}
		}
		return resultList;
	}
	
	
	private static Set<Set<String>> getSubsets(List<String> superset) {
		List<String> list = new ArrayList<String>(superset);
		int n = list.size();
		
		Set<Set<String>> powerSet = new HashSet<Set<String>>();
		
		for (long i = 0; i < (1 << n); i++) {
		    Set<String> element = new HashSet<String>();
		    for (int j = 0; j < n; j++) {
		        if ((i >> j) % 2 == 1) {
		        	element.add(list.get(j));
		        }
		    }
		    powerSet.add(element);
		}
		
		return powerSet;
    }
	
	
	public static void main(String[] args) throws IOException {
//		Set<Set<String>> subsets = VMMRTestTrainingDataApp.getSubsets(java.util.Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"));
//		Iterator<Set<String>> outerIterator = subsets.iterator();
//		int num = 0;
//		while (outerIterator.hasNext()) {
//			Set<String> subset = outerIterator.next();
//			if (subset.size() == 11) {
//				System.out.println(subset.toString());
//				num++;
//			}
//		}
//		System.out.println(num);
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new VMMRTestTrainingDataApp();
	}
}
