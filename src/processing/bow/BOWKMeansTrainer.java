package processing.bow;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;

/**
 * Translated to java from https://github.com/opencv/opencv/blob/master/modules/features2d/src/bagofwords.cpp
 */
public class BOWKMeansTrainer extends BOWTrainer {
	private int k;
	private TermCriteria termCriteria;
	int attempts;
	int flags;
	
	
	public BOWKMeansTrainer(int k, TermCriteria termCriteria, int attempts, int flags) {
		this.k = k;
		this.termCriteria = termCriteria;
		this.attempts = attempts;
		this.flags = flags;
	}
	
	
	@Override
	public Mat cluster() {
		if (getSize() > 0) {
			Mat anyDescriptor = getDescriptors().get(0);
			Mat mergedDescriptors = new Mat(getSize(), anyDescriptor.cols(), anyDescriptor.type());
			
			int start = 0;
			for(int i = 0; i < getDescriptors().size(); i++) {
				Mat descriptor = getDescriptors().get(i);
		        Mat submat = mergedDescriptors.rowRange(start, start + descriptor.rows());
		        descriptor.copyTo(submat);
		        start += descriptor.rows();
		    }
			return cluster(mergedDescriptors);
		}
		return null;
	}
	
	
	@Override
	public Mat cluster(Mat descriptors) {
		Mat labels = new Mat();
		Mat vocabulary = new Mat();
	    Core.kmeans(descriptors, k, labels, termCriteria, attempts, flags, vocabulary);
	    return vocabulary;
	}
}
