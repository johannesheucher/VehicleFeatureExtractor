package dataset;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Point;

import processing.NumberPlateExtractor;

public class Dataset {
	private ArrayList<ImageData> imageList;
	
	private static final int IMAGE_WIDTH = 800;
	
	private static final String[] EXTENSIONS = new String[] { "png", "jpg", "jpeg" };
	private static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : EXTENSIONS) {
                if (name.endsWith("." + ext)) {
                    return true;
                }
            }
            return false;
        }
    };
	
    
	public Dataset(File imagePath) {
		imageList = new ArrayList<>();
		
		// load all images from image path
		boolean b = true;
		if (imagePath.isDirectory()) {
            for (final File f : imagePath.listFiles(IMAGE_FILTER)) {
            	if (b) {
            		b = false;
	            	ImageData image = new ImageData(f);
	            	
	            	// resize all images so that they have the same size (at least width, aspect ratio not important, only for RoI)
	        		// This is important because the filter kernels have a distinct size and the car in each image should cover a more or less
	        		// constant percentage of area of the image.
	            	image.resize(IMAGE_WIDTH);
	            	
	            	// extract RoI
	            	// subtask - extract number plate rectangle
	            	//List<Point> rectangle = NumberPlateExtractor.extract(image);
	            	
	            	imageList.add(image);
            	}
            }
		}
	}
	
	
	public List<ImageData> getImageList() {
		return Collections.unmodifiableList(imageList);
	}
}
