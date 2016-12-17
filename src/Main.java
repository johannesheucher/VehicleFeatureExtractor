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
		
		ChangeListener sliderChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int gaussW = gaussKernelWidthSlider.getValue() * 2 + 1;
				int gaussH = gaussKernelHeightSlider.getValue() * 2 + 1;
				int hatW = hatKernelWidthSlider.getValue();
				int hatH = hatKernelHeightSlider.getValue();
				NumberPlateExtractor.toGray(sourceImage.getMat(), destImage.getMat());
				Imgproc.GaussianBlur(destImage.getMat(), destImage.getMat(), new Size(gaussW, gaussH), 1, 1);
				//NumberPlateExtractor.topHat(destImage.getMat(), destImage.getMat(), new Size(hatW, hatH));
				destImage.needsRefreshImage();
				icon0.setImage(destImage.getImage());
				
				// binarize
				Imgproc.threshold(destImage.getMat(), destImage.getMat(), 130, 255, Imgproc.THRESH_BINARY);
				destImage.needsRefreshImage();
				icon1.setImage(destImage.getImage());
				
				// line detection
				Imgproc.Canny(destImage.getMat(), destImage.getMat(), 50, 200, 3, true);
				destImage.needsRefreshImage();
				icon2.setImage(destImage.getImage());
				
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
