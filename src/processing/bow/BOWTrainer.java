package processing.bow;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

/**
 * Translated to java from https://github.com/opencv/opencv/blob/master/modules/features2d/src/bagofwords.cpp
 */
public abstract class BOWTrainer {
	/**
	 * Each Mat contains one descriptor per row
	 */
	private List<Mat> descriptors;
	int size = 0;
	
	
	public BOWTrainer() {
		descriptors = new ArrayList<>();
	}
	
	
	public List<Mat> getDescriptors() {
		return descriptors;
	}
	
	
	public void add(Mat descriptors) {
		this.descriptors.add(descriptors);
		
		size += descriptors.rows();
	}
	
	
	public void clear() {
		descriptors.clear();
	}
	
	
	public int getSize() {
		return size;
	}
	
	
	public abstract Mat cluster();
	
	
	public abstract Mat cluster(Mat descriptors);
}
