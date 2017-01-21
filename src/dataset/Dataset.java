package dataset;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.Const;

public class Dataset {
	private ArrayList<ImageData> imageList;
	
	private static final String[] EXTENSIONS = new String[] { "png", "PNG", "jpg", "JPG", "jpeg", "JPEG" };
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
	
    
    /**
     * 
     * @param imagePath
     * @param normalize Turns images to gray and resizes them to the standard width specified in Const
     */
	public Dataset(File imagePath, boolean normalize) {
		imageList = new ArrayList<>();
		addData(imagePath, normalize);
	}
	
	
	/**
     * 
     * @param imagePath
     * @param normalize Turns images to gray and resizes them to the standard width specified in Const
     */
	public void addData(File imagePath, boolean normalize) {
		// load all images from image path
		if (imagePath.isDirectory()) {
            for (final File f : imagePath.listFiles(IMAGE_FILTER)) {
            	ImageData image = new ImageData(f);
            	
            	if (normalize) {
	            	// resize all images so that they have the same size (at least width, aspect ratio not important, only for RoI)
	        		// This is important because the filter kernels have a distinct size and the car in each image should cover a more or less
	        		// constant percentage of area of the image.
	            	image.resize(Const.IMAGE_WIDTH);
	            	
	            	image.toGray();
            	}
            	
            	// extract RoI
            	// subtask - extract number plate rectangle
            	//List<Point> rectangle = NumberPlateExtractor.extract(image);
            	
            	imageList.add(image);
            }
		}
	}
	
	
	public List<ImageData> getImageList() {
		return Collections.unmodifiableList(imageList);
	}
}
