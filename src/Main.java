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
		ImageIcon icon = new ImageIcon(sourceImage.getImage());
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		
		// add kernel sliders
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
		frame.getContentPane().add(sliderPanel);
		JSlider kernelWidthSlider = new JSlider(JSlider.HORIZONTAL, 1, 25, 3);
		JSlider kernelHeightSlider = new JSlider(JSlider.HORIZONTAL, 1, 25, 3);
		JLabel kernelLabel = new JLabel("no value");
		sliderPanel.add(new JLabel("kernel size:"));
		sliderPanel.add(kernelWidthSlider);
		sliderPanel.add(kernelHeightSlider);
		sliderPanel.add(kernelLabel);
		
		ChangeListener sliderChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int w = kernelWidthSlider.getValue();
				int h = kernelHeightSlider.getValue();
				NumberPlateExtractor.toGray(sourceImage.getMat(), destImage.getMat());
				NumberPlateExtractor.topHat(destImage.getMat(), destImage.getMat(), new Size(w, h));
				destImage.needsRefreshImage();
				icon.setImage(destImage.getImage());
				frame.getContentPane().update(frame.getContentPane().getGraphics());
				kernelLabel.setText(w + " x " + h);
			}
		};
		kernelWidthSlider.addChangeListener(sliderChangeListener);
		kernelHeightSlider.addChangeListener(sliderChangeListener);
		
		// add resulting image
		frame.getContentPane().add(new JLabel(icon));
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// @TODO: add result image for next step besides
	}
	
	
	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		new Main();
	}
}
