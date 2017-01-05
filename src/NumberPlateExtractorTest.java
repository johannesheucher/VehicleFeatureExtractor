import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import dataset.Dataset;
import dataset.ImageData;
import processing.NumberPlateExtractor;
import processing.RoIExtractor;

public class NumberPlateExtractorTest {
	
	ImageData sourceImage;
	ImageData grayImage;
	JFrame frame;
	
	int pictureIndex = 0;
	
	public NumberPlateExtractorTest() {
		Dataset dataset = new Dataset(new File("D:/workspaces/Vehicle Data/"), true);
		
		sourceImage = dataset.getImageList().get(pictureIndex);
		grayImage = (ImageData)sourceImage.clone();
		ImageData contourImage = new ImageData(new Mat());
		ImageData finalImage = new ImageData(new Mat());
		ImageIcon icon0 = new ImageIcon(sourceImage.getImage());
		ImageIcon icon1 = new ImageIcon(sourceImage.getImage());
		ImageIcon icon2 = new ImageIcon(sourceImage.getImage());
		ImageIcon icon3 = new ImageIcon(sourceImage.getImage());
		ImageIcon icon4 = new ImageIcon(sourceImage.getImage());
		
		JLabel icon0Label = new JLabel(icon0);
		JLabel icon1Label = new JLabel(icon1);
		JLabel icon2Label = new JLabel(icon2);
		JLabel icon3Label = new JLabel(icon3);
		JLabel icon4Label = new JLabel(icon4);
		
		frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		
		// add kernel sliders
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
		sliderPanel.setPreferredSize(new Dimension(150, 600));
		frame.getContentPane().add(sliderPanel);
		JSlider gaussKernelWidthSlider = new JSlider(JSlider.HORIZONTAL, 0, 6, 5);
		JSlider gaussKernelHeightSlider = new JSlider(JSlider.HORIZONTAL, 0, 6, 5);
		JSlider hatKernelWidthSlider = new JSlider(JSlider.HORIZONTAL, 1, 400, 200);
		JSlider hatKernelHeightSlider = new JSlider(JSlider.HORIZONTAL, 1, 400, 40);
		
		JSlider houghSliderRho = new JSlider(JSlider.HORIZONTAL, 1, 15, 1);
		JSlider houghSliderThreshold = new JSlider(JSlider.HORIZONTAL, 1, 200, 50);
		JSlider houghSliderMinLength = new JSlider(JSlider.HORIZONTAL, 0, 250, 0);
		JSlider houghSliderMaxGap = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
		// double rho, int threshold, double minlength, double maxgap
		
		JCheckBox showBinary = new JCheckBox("Binary", true);
		JCheckBox showEdges = new JCheckBox("Edges", true);
		JCheckBox showContours = new JCheckBox("Contours", true);
		JCheckBox showRect = new JCheckBox("Rect", true);
		JCheckBox showRoI = new JCheckBox("RoI", true);
		
		sliderPanel.add(showBinary);
		sliderPanel.add(showEdges);
		sliderPanel.add(showContours);
		sliderPanel.add(showRect);
		sliderPanel.add(showRoI);
		
		JButton nextPictureButton = new JButton("next");
		sliderPanel.add(nextPictureButton);
		
		sliderPanel.add(new JLabel("gauss kernel size:"));
		sliderPanel.add(gaussKernelWidthSlider);
		sliderPanel.add(gaussKernelHeightSlider);
		JLabel gaussKernelLabel = new JLabel("no value");
		sliderPanel.add(gaussKernelLabel);
		
		sliderPanel.add(new JLabel("contour bb size:"));
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
				
				//sourceImage.needsRefreshImage();
				//icon0.setImage(sourceImage.getImage());
				
				grayImage = (ImageData)sourceImage.clone();
				
				// detect number plate
				List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
				Mat binary = new Mat();
				Mat edges = new Mat();
				Mat matRoI = null;
				Range rowRange = new Range();
				Range colRange = new Range();
				NumberPlateExtractor.calculateCropOffset(grayImage.getMat(), rowRange, colRange);
				Mat sub = grayImage.getMat().submat(rowRange, colRange);
				Rect rect = NumberPlateExtractor.extract(sub, binary, edges, contours);
				Imgproc.cvtColor(grayImage.getMat(), finalImage.getMat(), Imgproc.COLOR_GRAY2BGR);
				if (rect != null) {
					Scalar color = new Scalar(255, 60, 255);
					rect.x += colRange.start;
					rect.y += rowRange.start;
					Imgproc.rectangle(finalImage.getMat(), new Point(rect.x,  rect.y), new Point(rect.x + rect.width, rect.y + rect.height), color, 2);
					matRoI = RoIExtractor.extract(grayImage.getMat(), rect);
				}
				finalImage.needsRefreshImage();
				if (showRect.isSelected()) {
					icon3Label.setVisible(true);
					icon3.setImage(finalImage.getImage());
				} else {
					icon3Label.setVisible(false);
				}
				
				// additionally, output intermediate steps
				if (showBinary.isSelected()) {
					icon0Label.setVisible(true);
					icon0.setImage(new ImageData(binary).getImage());
				} else {
					icon0Label.setVisible(false);
				}
				if (showEdges.isSelected()) {
					icon1Label.setVisible(true);
					icon1.setImage(new ImageData(edges).getImage());
				} else {
					icon1Label.setVisible(false);
				}
				
				Imgproc.cvtColor(edges, contourImage.getMat(), Imgproc.COLOR_GRAY2BGR);
				for (int i = 0; i < contours.size(); i++) {
	    			Imgproc.drawContours(contourImage.getMat(), contours, i, randomColor(), 2);
				}
				contourImage.needsRefreshImage();
				if (showContours.isSelected()) {
					icon2Label.setVisible(true);
					icon2.setImage(contourImage.getImage());
				} else {
					icon2Label.setVisible(false);
				}
				
				// show RoI
				if (showRoI.isSelected() && matRoI != null) {
					icon4Label.setVisible(true);
					icon4.setImage(new ImageData(matRoI).getImage());
				} else {
					icon4Label.setVisible(false);
				}
				
				// update
				frame.getContentPane().update(frame.getContentPane().getGraphics());
				gaussKernelLabel.setText(gaussW + " x " + gaussH);
				hatKernelLabel.setText(hatW + " x " + hatH);
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

		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pictureIndex++;
				if (pictureIndex >= dataset.getImageList().size()) {
					pictureIndex = 0;
				}
				sourceImage = dataset.getImageList().get(pictureIndex);
				sliderChangeListener.stateChanged(null);
			}
		};
		nextPictureButton.addActionListener(buttonListener);
		
		ChangeListener checkBoxListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				sliderChangeListener.stateChanged(null);
			}
		};
		showBinary.addChangeListener(checkBoxListener);
		showEdges.addChangeListener(checkBoxListener);
		showContours.addChangeListener(checkBoxListener);
		showRect.addChangeListener(checkBoxListener);
		showRoI.addChangeListener(checkBoxListener);
		
		// add resulting image
		frame.getContentPane().add(icon0Label);
		frame.getContentPane().add(icon1Label);
		frame.getContentPane().add(icon2Label);
		frame.getContentPane().add(icon3Label);
		frame.getContentPane().add(icon4Label);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sliderChangeListener.stateChanged(null);
	}
	
	
	public static Scalar randomColor() {
		return new Scalar(Math.random() * 100 + 55, Math.random() * 100 + 55, Math.random() * 100 + 55);
	}
	
	
	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		new NumberPlateExtractorTest();
	}
}
