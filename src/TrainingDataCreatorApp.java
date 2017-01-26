import java.io.File;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import dataset.Dataset;
import dataset.ImageData;
import dataset.NumberPlateMetaFile;
import processing.RoIExtractor;

public class TrainingDataCreatorApp {
	private static final String INPUT_PATH = "D:/workspaces/VehicleData/selection/";
	private static final String OUTPUT_PATH = "D:/workspaces/VehicleData/training/";
	private static final String NUMBER_PLATE_FILENAME = "_NumberPlates.csv";
	private static final int MIN_NUM_VEHICLES = 1;
	private static final int ROI_WIDTH = 500;
	
	private NumberPlateMetaFile numberPlateMeta;
	
	
	public TrainingDataCreatorApp() {
		Dataset dataset = new Dataset(new File(INPUT_PATH), false);
		
		numberPlateMeta = new NumberPlateMetaFile();
		try {
			numberPlateMeta.read(INPUT_PATH + NUMBER_PLATE_FILENAME);
		} catch (IOException e1) {
			System.out.println("No number plate meta file found.");
		}
		
		for (ImageData image : dataset.getImageList()) {
			// there must be at least MIN_NUM_VEHICLES vehicles of this makemodel to be proper for training data
			if (dataset.getVehicleCounts().get(image.getMakeModel()) >= MIN_NUM_VEHICLES) {
				image.toGray();
				Rect rect = numberPlateMeta.getRect(image.getVehicleKey());
				if (rect != null) {
					Mat roi = RoIExtractor.extract(image.getMat(), rect);
					Imgproc.equalizeHist(roi, roi);
					ImageData result = new ImageData(roi);
					result.resize(ROI_WIDTH);
					result.save(new File(OUTPUT_PATH + image.getName()));
					System.out.println("exported vehicle " + image.getName());
				} else {
					System.out.println("=== WARNING: No number plate found for vehicle " + image.getName() + " ===");
				}
			}
		}
	}
	
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new TrainingDataCreatorApp();
	}
}
