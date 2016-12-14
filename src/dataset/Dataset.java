package dataset;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

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
                try {
					imageList.add(new ImageData(f));
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
		}
	}
}
