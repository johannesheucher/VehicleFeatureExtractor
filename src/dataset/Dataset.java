package dataset;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dataset {
	private ArrayList<ImageData> imageList;
	
	static final String[] EXTENSIONS = new String[] { "png", "jpg", "jpeg" };
	static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {
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
		if (imagePath.isDirectory()) {
            for (final File f : imagePath.listFiles(IMAGE_FILTER)) {
            	imageList.add(new ImageData(f));
            }
		}
	}
	
	
	public List<ImageData> getImageList() {
		return Collections.unmodifiableList(imageList);
	}
}
