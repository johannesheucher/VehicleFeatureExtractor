import java.awt.FlowLayout;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import dataset.Dataset;
import dataset.ImageData;
import processing.NumberPlateExtractor;

public class Main {
	
	ImageData sourceImage;
	ImageData destImage;
	
	public Main() {
		Dataset dataset = new Dataset(new File("D:/workspaces/Vehicle Data/"));
		
		sourceImage = dataset.getImageList().get(0);
		destImage = (ImageData)sourceImage.clone();
		ImageIcon icon0 = new ImageIcon(sourceImage.getImage());
		ImageIcon icon1 = new ImageIcon(sourceImage.getImage());
		ImageIcon icon2 = new ImageIcon(sourceImage.getImage());
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		
		// add kernel sliders
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
		frame.getContentPane().add(sliderPanel);
		JSlider gaussKernelWidthSlider = new JSlider(JSlider.HORIZONTAL, 0, 6, 2);
		JSlider gaussKernelHeightSlider = new JSlider(JSlider.HORIZONTAL, 0, 6, 2);
		JSlider hatKernelWidthSlider = new JSlider(JSlider.HORIZONTAL, 1, 25, 3);
		JSlider hatKernelHeightSlider = new JSlider(JSlider.HORIZONTAL, 1, 25, 3);
		
		JSlider houghSliderRho = new JSlider(JSlider.HORIZONTAL, 0, 10, 1);
		JSlider houghSliderThreshold = new JSlider(JSlider.HORIZONTAL, 5, 120, 50);
		JSlider houghSliderMinLength = new JSlider(JSlider.HORIZONTAL, 5, 120, 50);
		JSlider houghSliderMaxGap = new JSlider(JSlider.HORIZONTAL, 5, 120, 10);
		// double rho, int threshold, double minlength, double maxgap
		
		sliderPanel.add(new JLabel("gauss kernel size:"));
		sliderPanel.add(gaussKernelWidthSlider);
		sliderPanel.add(gaussKernelHeightSlider);
		JLabel gaussKernelLabel = new JLabel("no value");
		sliderPanel.add(gaussKernelLabel);
		
		sliderPanel.add(new JLabel("hat kernel size:"));
		sliderPanel.add(hatKernelWidthSlider);
		sliderPanel.add(hatKernelHeightSlider);
		JLabel hatKernelLabel = new JLabel("no value");
		sliderPanel.add(hatKernelLabel);
		
		sliderPanel.add(new JLabel("hough parameters:"));
		sliderPanel.add(houghSliderRho);
		sliderPanel.add(houghSliderThreshold);
		sliderPanel.add(houghSliderMinLength);
		sliderPanel.add(houghSliderMaxGap);
		JLabel houghLabel = new JLabel("no value");
		sliderPanel.add(houghLabel);
		
		maybe find contour would do better here
		
		
		ChangeListener sliderChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int gaussW = gaussKernelWidthSlider.getValue() * 2 + 1;
				int gaussH = gaussKernelHeightSlider.getValue() * 2 + 1;
				int hatW = hatKernelWidthSlider.getValue();
				int hatH = hatKernelHeightSlider.getValue();
				destImage = (ImageData)sourceImage.clone();
				NumberPlateExtractor.toGray(sourceImage.getMat(), destImage.getMat());
				Imgproc.GaussianBlur(destImage.getMat(), destImage.getMat(), new Size(gaussW, gaussH), 1, 1);
				//NumberPlateExtractor.topHat(destImage.getMat(), destImage.getMat(), new Size(hatW, hatH));
//				destImage.needsRefreshImage();
//				icon0.setImage(destImage.getImage());
				
				// binarize
				Imgproc.threshold(destImage.getMat(), destImage.getMat(), 130, 255, Imgproc.THRESH_BINARY);
				destImage.needsRefreshImage();
				icon0.setImage(destImage.getImage());
				
				// edge detection
				Imgproc.Canny(destImage.getMat(), destImage.getMat(), 50, 200, 3, true);
				destImage.needsRefreshImage();
				icon1.setImage(destImage.getImage());
				
				// line detection
				Mat linesMat = new Mat();
				
				int houghRho = houghSliderRho.getValue();
				int houghThreshold = houghSliderThreshold.getValue();
				int houghMinLength = houghSliderMinLength.getValue();
				int houghMaxGap = houghSliderMaxGap.getValue();
				Imgproc.HoughLinesP(destImage.getMat(), linesMat, houghRho, Math.PI/180, houghThreshold, houghMinLength, houghMaxGap);
				Imgproc.cvtColor(destImage.getMat(), destImage.getMat(), Imgproc.COLOR_GRAY2BGR);
				int numLines = linesMat.rows();
				for (int x = 0; x < numLines; x++) {
					double[] vec = linesMat.get(x, 0);
					double x1 = vec[0], 
			               y1 = vec[1],
			               x2 = vec[2],
			               y2 = vec[3];
					Point start = new Point(x1, y1);
					Point end = new Point(x2, y2);
					Imgproc.line(destImage.getMat(), start, end, new Scalar(255,0,0), 3);
			    }
				destImage.needsRefreshImage();
				icon2.setImage(destImage.getImage());
				
				// update
				frame.getContentPane().update(frame.getContentPane().getGraphics());
				gaussKernelLabel.setText(gaussW + " x " + gaussH);
				hatKernelLabel.setText(hatW + " x " + hatH);
				//houghLabel.setText("rho: " + houghRho + ", threshold: " + houghThreshold + "\nmin: " + houghMinLength + ", max: " + houghMaxGap);
			}
		};
		gaussKernelWidthSlider.addChangeListener(sliderChangeListener);
		gaussKernelHeightSlider.addChangeListener(sliderChangeListener);
		hatKernelWidthSlider.addChangeListener(sliderChangeListener);
		hatKernelHeightSlider.addChangeListener(sliderChangeListener);
		houghSliderRho.addChangeListener(sliderChangeListener);
		houghSliderThreshold.addChangeListener(sliderChangeListener);
		houghSliderMinLength.addChangeListener(sliderChangeListener);
		houghSliderMaxGap.addChangeListener(sliderChangeListener);
		
		// add resulting image
		frame.getContentPane().add(new JLabel(icon0));
		frame.getContentPane().add(new JLabel(icon1));
		frame.getContentPane().add(new JLabel(icon2));
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
	}
	
	
	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		new Main();
	}
}
