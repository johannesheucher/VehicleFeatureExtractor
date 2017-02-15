package dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;

import processing.bow.BOWKMeansTrainer;
import util.NetworkUtil;

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
	
	
	public Dictionary(String filename) {
		load(filename);
	}
	
	
	public Mat getCodewords() {
		return codewords;
	}
	
	
	public int getSize() {
		return size;
	}
	
	
	public void load(String filename) {
		try {
			File file = new File(filename);
			byte[] data = new byte[(int)file.length()];
			FileInputStream input = new FileInputStream(filename);
			input.read(data);
			input.close();
			
			byte[] rowsBytes = Arrays.copyOfRange(data, 0, 4);
			byte[] colsBytes = Arrays.copyOfRange(data, 4, 8);
			byte[] typeBytes = Arrays.copyOfRange(data, 8, 12);
			int rows = NetworkUtil.bytesToInt(rowsBytes);
			int cols = NetworkUtil.bytesToInt(colsBytes);
			int type = NetworkUtil.bytesToInt(typeBytes);
			
			codewords = new Mat(rows, cols, type);
			codewords.put(0, 0, Arrays.copyOfRange(data, 12, data.length));
			size = rows;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void save(String filename) {
		try {
			int length = (int)(getCodewords().total() * getCodewords().elemSize());
			byte matData[] = new byte[length];
			getCodewords().get(0, 0, matData);
			
			byte[] rowsBytes = NetworkUtil.intToBytes(getCodewords().rows());
			byte[] colsBytes = NetworkUtil.intToBytes(getCodewords().cols());
			byte[] typeBytes = NetworkUtil.intToBytes(getCodewords().type());
			
			byte[] buffer = new byte[matData.length + 12];
			System.arraycopy(rowsBytes, 0, buffer, 0, 4);
			System.arraycopy(colsBytes, 0, buffer, 4, 4);
			System.arraycopy(typeBytes, 0, buffer, 8, 4);
			System.arraycopy(matData,   0, buffer, 12, matData.length);
			
			FileOutputStream output = new FileOutputStream(filename);
			output.write(buffer);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
