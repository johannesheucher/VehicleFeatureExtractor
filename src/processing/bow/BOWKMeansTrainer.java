package processing.bow;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;

/**
 * Basis translated to java from https://github.com/opencv/opencv/blob/master/modules/features2d/src/bagofwords.cpp
 * Adjusted by JH
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
	public Mat cluster(int minClusterMembers) {
		if (getSize() > 0) {
			Mat anyDescriptor = getDescriptors().get(0);
			Mat mergedDescriptors = new Mat(getSize(), anyDescriptor.cols(), anyDescriptor.type());
			
			int start = 0;
			for (int i = 0; i < getDescriptors().size(); i++) {
				Mat descriptor = getDescriptors().get(i);
		        Mat submat = mergedDescriptors.rowRange(start, start + descriptor.rows());
		        descriptor.copyTo(submat);
		        start += descriptor.rows();
		    }
			return cluster(mergedDescriptors, minClusterMembers);
		}
		return null;
	}
	
	
	@Override
	public Mat cluster(Mat descriptors, int minClusterMembers) {
		Mat labels = new Mat();
		Mat vocabulary = new Mat();
	    Core.kmeans(descriptors, k, labels, termCriteria, attempts, flags, vocabulary);
	    
	    // Odd out those clusters, that have less then numClusterMembers
	    // -----
	    int[] clusterMembers = new int[k];
	    for (int i = 0; i < labels.rows(); i++) {
	    	double[] entry = labels.get(i, 0);
	    	int index = (int)entry[0];
	    	clusterMembers[index]++;
	    }
	    
	    // store rows, that shall be kept, in a list
	    List<Mat> rowsToKeep = new ArrayList<Mat>(k);
	    for (int i = 0; i < k; i++) {
	    	if (clusterMembers[i] >= minClusterMembers) {
	    		Mat row = vocabulary.row(i);
	    		rowsToKeep.add(row);
	    	}
	    }
	    
	    // merge single rows again
	    Mat mergedRows = new Mat(rowsToKeep.size(), vocabulary.cols(), vocabulary.type());
		
		int start = 0;
		for (int i = 0; i < rowsToKeep.size(); i++) {
			Mat row = rowsToKeep.get(i);
	        Mat submat = mergedRows.rowRange(start, start + 1);
	        row.copyTo(submat);
	        start++;
	    }
		
		System.out.printf("  removed %d clusters from dictionary, leaving %d\n", vocabulary.rows() - rowsToKeep.size(), rowsToKeep.size());
	    
	    return mergedRows;
	}
}
