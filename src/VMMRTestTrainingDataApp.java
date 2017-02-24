import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import dataset.Dataset;
import dataset.ImageData;

public class VMMRTestTrainingDataApp {
	private static final String INPUT_PATH = "D:/workspaces/VehicleData/raw/";
	private static final String OUTPUT_PATH = "D:/workspaces/VehicleData/ARFF_original_seven/";
	private static final int SUBSET_SIZE = 13;
	private static final int MIN_VEHICLES_PER_CLASS = 5;	// DEBUG: 8
	
	/**
	 * Training images by vehicle name
	 */
	private Map<String, List<ImageData>> trainingData;
	
	
	public VMMRTestTrainingDataApp() throws Exception {
		TrainingDataCreatorApp trainingDataCreator = new TrainingDataCreatorApp(INPUT_PATH);
		
		// DEBUG
		//Dataset trainingSet = new Dataset(new File("D:/workspaces/VehicleData/training/"), false);
		List<ImageData> subsetImages = new ArrayList<>();
		Set<String> allowed = new HashSet<>(java.util.Arrays.asList("vw_kaefer","vw_polo_9n3","mazda_mx-5_nd","opel_mokka","porsche_911","vw_golf_iv","opel_corsa_d"));
		for (ImageData image : trainingDataCreator.getTrainingData()) {
			if (allowed.contains(image.getMakeModel())) {
				
				// save as 8UC1, import as 8UC3 gives best results
//				Imgproc.cvtColor(image.getMat(), image.getMat(), Imgproc.COLOR_GRAY2RGB);
				
				// whyever, this is necessary for better classification performance -> save and reload image
				String name = image.getName();
				File filename = new File(OUTPUT_PATH + "training_images/" + "tmp" + name.substring(name.indexOf(".")));
				image.save(filename);
				image = new ImageData(filename);
				image.setName(name);
				Imgproc.cvtColor(image.getMat(), image.getMat(), Imgproc.COLOR_BGR2GRAY);
				
				subsetImages.add(image);
				System.out.println("exported vehicle " + image.getName());
			}
		}
//		if (true) {
//			return;
//		}
		
//		Dataset trainingSet = new Dataset(new File(OUTPUT_PATH + "training_images1/"), false);
//		subsetImages = trainingSet.getImageList();
		
		// divide by make-model
//		trainingData = new HashMap<>();
//		for (ImageData image : trainingDataCreator.getTrainingData()) {
//		//for (ImageData image : trainingSet.getImageList()) {
//			if (trainingDataCreator.getDataset().getVehicleCounts().get(image.getMakeModel()) >= MIN_VEHICLES_PER_CLASS) {
//			//if (trainingSet.getVehicleCounts().get(image.getMakeModel()) >= MIN_VEHICLES_PER_CLASS) {
//				String makeModel = image.getMakeModel();
//				List<ImageData> vehicles;
//				if (trainingData.containsKey(makeModel)) {
//					vehicles = trainingData.get(makeModel);
//				} else {
//					vehicles = new ArrayList<>();
//					trainingData.put(makeModel, vehicles);
//				}
//				vehicles.add(image);
//			}
//		}
		
		// permute all combinations of SUBSET_SIZE make-models
		// DEBUG: uncomment
//		List<Set<String>> subsets = VMMRTestTrainingDataApp.getSubsetsWithLength(new ArrayList<>(trainingData.keySet()), SUBSET_SIZE);
//		for (Set<String> subset : subsets) {
//			System.out.println(subset.toString());
//		}
		
		// create an ARFF file for each subset
		int id = 0;
		//for (Set<String> subset : subsets) {
		// DEBUG
		id = 42;
		//Set<String> subset = new HashSet<>(java.util.Arrays.asList("vw_kaefer","vw_polo_9n3","mazda_mx-5_nd","opel_mokka","porsche_911","vw_golf_iv","opel_corsa_d"));
		// DEBUG end
			//List<ImageData> subsetImages = getImages(subset);
//			subsetImages = new ArrayList<>();
//			for (ImageData image : trainingDataCreator.getTrainingData()) {
//				if (subset.contains(image.getMakeModel())) {
//					subsetImages.add(image);
//				}
//			}
			
			String dataId = String.format("%04d", id);
			id++;
			System.out.printf("num images: %d\n", subsetImages.size());
			DictionaryTestApp.buildDictionaryAndARFF(subsetImages, DictionaryTestApp.DICTIONARY_SIZE, OUTPUT_PATH, dataId);
			//System.out.printf("exported classification data %d / %d\n", id, subsets.size());
		//}
		System.out.println("exported dictionary with ARFF");
		new VMMRTestVehicleApp(OUTPUT_PATH + "_" + dataId + "vehicles.arff", OUTPUT_PATH + "_" + dataId + "dictionary.bytes");
		
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
	
	
	public static void main(String[] args) throws Exception {
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
