import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.opencv.core.MatOfPoint;
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
		
		sourceImage = dataset.getImageList().get(1);
		destImage = (ImageData)sourceImage.clone();
		ImageIcon icon0 = new ImageIcon(sourceImage.getImage());
		ImageIcon icon1 = new ImageIcon(sourceImage.getImage());
		ImageIcon icon2 = new ImageIcon(sourceImage.getImage());
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		
		// add kernel sliders
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
		sliderPanel.setPreferredSize(new Dimension(200, 600));
		frame.getContentPane().add(sliderPanel);
		JSlider gaussKernelWidthSlider = new JSlider(JSlider.HORIZONTAL, 0, 6, 2);
		JSlider gaussKernelHeightSlider = new JSlider(JSlider.HORIZONTAL, 0, 6, 2);
		JSlider hatKernelWidthSlider = new JSlider(JSlider.HORIZONTAL, 1, 25, 3);
		JSlider hatKernelHeightSlider = new JSlider(JSlider.HORIZONTAL, 1, 25, 3);
		
		JSlider houghSliderRho = new JSlider(JSlider.HORIZONTAL, 1, 15, 1);
		JSlider houghSliderThreshold = new JSlider(JSlider.HORIZONTAL, 1, 200, 50);
		JSlider houghSliderMinLength = new JSlider(JSlider.HORIZONTAL, 0, 250, 0);
		JSlider houghSliderMaxGap = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
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
		
		
		ChangeListener sliderChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int gaussW = gaussKernelWidthSlider.getValue() * 2 + 1;
				int gaussH = gaussKernelHeightSlider.getValue() * 2 + 1;
				int hatW = hatKernelWidthSlider.getValue();
				int hatH = hatKernelHeightSlider.getValue();
				//destImage = (ImageData)sourceImage.clone();
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
				int houghRho = houghSliderRho.getValue();
				int houghThreshold = houghSliderThreshold.getValue();
				int houghMinLength = houghSliderMinLength.getValue();
				int houghMaxGap = houghSliderMaxGap.getValue();
				if (false) {
					Mat linesMat = new Mat();
					
					Imgproc.HoughLinesP(destImage.getMat(), linesMat, houghRho, Math.PI/180, houghThreshold, houghMinLength, houghMaxGap);
					Imgproc.cvtColor(destImage.getMat(), destImage.getMat(), Imgproc.COLOR_GRAY2BGR);
					int numLines = linesMat.rows();
					
					for (int x = 0; x < numLines; x++) {
						// for HoughLines
						double[] rhoTheta = linesMat.get(x, 0);
						//float rho = linesMat[x][0], theta = linesMat[i][1];
						double a = Math.cos(rhoTheta[1]);
						double b = Math.sin(rhoTheta[1]);
						double x0 = a*rhoTheta[0];
						double y0 = b*rhoTheta[0];
						Size size = destImage.getMat().size();
						Point start = new Point(Math.round(x0 + size.width*(-b)), Math.round(y0 + size.height*(a)));
						Point end = new Point(Math.round(x0 - size.width*(-b)), Math.round(y0 - size.height*(a)));
						
						
						// for HoughLinesP
	//					double[] vec = linesMat.get(x, 0);
	//					double x1 = vec[0],
	//			               y1 = vec[1],
	//			               x2 = vec[2],
	//			               y2 = vec[3];
	//					Point start = new Point(x1, y1);
	//					Point end = new Point(x2, y2);
						Imgproc.line(destImage.getMat(), start, end, randomColor(), 2);
				    }
					destImage.needsRefreshImage();
					icon2.setImage(destImage.getImage());
				}
				
				// contour detection
				if (true) {
					List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

				    Imgproc.findContours(destImage.getMat(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
				    Imgproc.cvtColor(destImage.getMat(), destImage.getMat(), Imgproc.COLOR_GRAY2BGR);
				    for (int i = 0; i < contours.size(); i++) {
				    	Imgproc.drawContours(destImage.getMat(), contours, i, randomColor(), 2);
				    }
				    
//				    Iterator<MatOfPoint> each = contours.iterator();
//				    while (each.hasNext()) {
//				        MatOfPoint wrapper = each.next();
//				    }
				    destImage.needsRefreshImage();
					icon2.setImage(destImage.getImage());
				}
				
				
				// TODO
//				* Try HoughLines (without P). Maybe the included step for finding the line's start and end is bad !!!
//				* look at hough space and identify points of number plate lines. Should work!!
//				* If the image is too big to identify the short lines of the number plate, make the image smaller:
//					Split the image into overlapping rectangles (as wide as the expected number plate width) and search inside these.
				
				
				// update
				frame.getContentPane().update(frame.getContentPane().getGraphics());
				gaussKernelLabel.setText(gaussW + " x " + gaussH);
				hatKernelLabel.setText(hatW + " x " + hatH);
				houghLabel.setText("r: " + houghRho + ", t: " + houghThreshold + ", min: " + houghMinLength + ", max: " + houghMaxGap);
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
	
	
	public static Scalar randomColor() {
		return new Scalar(Math.random() * 100 + 55, Math.random() * 100 + 55, Math.random() * 100 + 55);
	}
	
	
	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		new Main();
	}
}
