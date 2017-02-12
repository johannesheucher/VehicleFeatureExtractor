package dataset;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;

import processing.bow.BOWKMeansTrainer;

public class Dictionary {
	private int size;
	private Mat codewords = new Mat();
	
	public Dictionary(List<Mat> descriptors, int size) {
		this.size = size;
		TermCriteria termCriteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
		BOWKMeansTrainer trainer = new BOWKMeansTrainer(getSize(), termCriteria, 1, Core.KMEANS_PP_CENTERS);
		for (Mat entry : descriptors) {
			Mat entry32f = new Mat();
			entry.convertTo(entry32f, CvType.CV_32F);
			trainer.add(entry32f);
		}
		Mat codewords32f = trainer.cluster();
		codewords32f.convertTo(codewords, CvType.CV_8UC1);
	}
	
	
	public Mat getCodewords() {
		return codewords;
	}
	
	
	public int getSize() {
		return size;
	}
	
	
	public void save(String filename) {
		//codewords.dataAddr()
	}
}
