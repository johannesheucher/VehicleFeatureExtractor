import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import dataset.Dataset;
import dataset.ImageData;
import dataset.NumberPlateMetaFile;
import processing.RoIExtractor;

public class TrainingDataCreatorApp {
	private static final String INPUT_PATH = "D:/workspaces/VehicleData/raw/";
	private static final String OUTPUT_PATH = "D:/workspaces/VehicleData/training2/";
	private static final String NUMBER_PLATE_FILENAME = "_NumberPlates.csv";
	private static final int ROI_WIDTH = 400;
	
	private static final int MIN_NUM_VEHICLES = 5;
	private static final List<String> ALLOWED_VEHICLE_NAMES = Arrays.asList("vw_kaefer", "vw_polo_9n3", "mazda_mx-5_nd", "opel_mokka", "porsche_911", "vw_golf_iv", "opel_corsa_d_fl", "citroen_c4_cactus", "ford_fiesta", "tesla_model_s");
	private static final boolean USE_CONSTRAINTS = true;
	
	private NumberPlateMetaFile numberPlateMeta;
	
	private List<ImageData> trainingData;
	private Dataset dataset;
	
	public TrainingDataCreatorApp(String inputPath) {
		dataset = new Dataset(new File(inputPath), false, false);
		trainingData = new ArrayList<>(dataset.getImageList().size());
		
		numberPlateMeta = new NumberPlateMetaFile();
		try {
			numberPlateMeta.read(inputPath + NUMBER_PLATE_FILENAME);
		} catch (IOException e1) {
			System.out.println("No number plate meta file found.");
		}
		
		for (ImageData image : dataset.getImageList()) {
			// there must be at least MIN_NUM_VEHICLES vehicles of this make-model to be proper for training data
			if (!USE_CONSTRAINTS || (dataset.getVehicleCounts().get(image.getMakeModel()) >= MIN_NUM_VEHICLES && ALLOWED_VEHICLE_NAMES.contains(image.getMakeModel()))) {
				image.toGray();
				Rect rect = numberPlateMeta.getRect(image.getVehicleKey());
				if (rect != null) {
					Mat roi = RoIExtractor.extract(image.getMat(), rect);
					Imgproc.equalizeHist(roi, roi);
					ImageData result = new ImageData(roi);
					result.resize(ROI_WIDTH);
					result.setName(image.getName());
					trainingData.add(result);
					
					System.out.println("created training data for vehicle " + image.getName());
				} else {
					System.out.println("=== WARNING: No number plate found for vehicle " + image.getName() + " ===");
				}
			}
		}
		System.out.println("done");
	}
	
	
	public List<ImageData> getTrainingData() {
		return trainingData;
	}
	
	
	public Dataset getDataset() {
		return dataset;
	}
	
	
	public void saveTrainingData(String outputPath) {
		for (ImageData image : trainingData) {
			image.save(new File(outputPath + image.getName()));
			System.out.println("exported vehicle " + image.getName());
		}
	}
	
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		TrainingDataCreatorApp app = new TrainingDataCreatorApp(INPUT_PATH);
		app.saveTrainingData(OUTPUT_PATH);
	}
}
