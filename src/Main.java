import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import dataset.Dataset;
import dataset.ImageData;
import processing.NumberPlateExtractor;

public class Main {
	
	ImageData sourceImage;
	ImageData destImage;
	JFrame frame;
	
	ImageIcon icon0;
	ImageIcon icon1;
	ImageIcon icon2;
	ImageIcon icon3;
	int pictureIndex = 0;
	
	public Main() {
		Dataset dataset = new Dataset(new File("D:/workspaces/Vehicle Data/"));
		
		sourceImage = dataset.getImageList().get(pictureIndex);
		destImage = (ImageData)sourceImage.clone();
		ImageData contourImage = new ImageData(new Mat());
		icon0 = new ImageIcon(sourceImage.getImage());
		icon1 = new ImageIcon(sourceImage.getImage());
		icon2 = new ImageIcon(sourceImage.getImage());
		icon3 = new ImageIcon(sourceImage.getImage());
		
		frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		
		// add kernel sliders
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
		sliderPanel.setPreferredSize(new Dimension(200, 600));
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
				
				sourceImage.needsRefreshImage();
				icon0.setImage(sourceImage.getImage());
				
				NumberPlateExtractor.toGray(sourceImage.getMat(), destImage.getMat());
				
				// detect number plate
				Rect rect = NumberPlateExtractor.extract(destImage.getMat());
				Imgproc.cvtColor(destImage.getMat(), contourImage.getMat(), Imgproc.COLOR_GRAY2BGR);
				if (rect != null) {
					Scalar color = new Scalar(255, 60, 255);
					Imgproc.rectangle(contourImage.getMat(), new Point(rect.x,  rect.y), new Point(rect.x + rect.width, rect.y + rect.height), color, 2);
				}
				contourImage.needsRefreshImage();
				icon1.setImage(contourImage.getImage());
				
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
		
		// add resulting image
		frame.getContentPane().add(new JLabel(icon0));
		frame.getContentPane().add(new JLabel(icon1));
		//frame.getContentPane().add(new JLabel(icon2));
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
		new Main();
	}
}
