import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import dataset.Dataset;

public class Main {

	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Mat mat = Mat.eye( 3, 3, CvType.CV_8UC1 );
		System.out.println( "mat = " + mat.dump() );
		
		Dataset dataset = new Dataset(new File("D:/workspaces/Vehicle Data/"));
		dataset.getImageList().get(1).display();
	}
}
