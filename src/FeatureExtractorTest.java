import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import dataset.ImageData;
import processing.FeatureExtractor;

public class FeatureExtractorTest {
	
	public FeatureExtractorTest() {
		// load two similar images (maybe already a vehicle RoI)
		ImageData dataImage = new ImageData(new File("D:/workspaces/Vehicle Data/0_data.png"));
		ImageData testImage = new ImageData(new File("D:/workspaces/Vehicle Data/0_test.png"));
		
		ImageIcon dataIcon = new ImageIcon(dataImage.getImage());
		ImageIcon testIcon = new ImageIcon(testImage.getImage());
		ImageIcon matchIcon = new ImageIcon(dataImage.getImage());
		
		JLabel dataLabel = new JLabel(dataIcon);
		JLabel testLabel = new JLabel(testIcon);
		JLabel matchLabel = new JLabel(matchIcon);
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		
		// side panel
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
		sliderPanel.setPreferredSize(new Dimension(150, 600));
		frame.getContentPane().add(sliderPanel);
		
		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// extract features of each
				MatOfKeyPoint dataKeyPoints = new MatOfKeyPoint();
				MatOfKeyPoint testKeyPoints = new MatOfKeyPoint();
				Mat dataDescriptors = new Mat();
				Mat testDescriptors = new Mat();
				FeatureExtractor.extract(dataImage.getMat(), dataKeyPoints, dataDescriptors);
				FeatureExtractor.extract(testImage.getMat(), testKeyPoints, testDescriptors);
				
				// draw features
				Mat rgb = new Mat();
				Imgproc.cvtColor(dataImage.getMat(), rgb, Imgproc.COLOR_BGR2RGB);
				Features2d.drawKeypoints(rgb, dataKeyPoints, rgb, new Scalar(255, 0, 0), 0);
				dataIcon.setImage(new ImageData(rgb).getImage());
				
				// match features
				
				// draw matches
				
				// compare SURF with ORB
				
				// update
				frame.getContentPane().update(frame.getContentPane().getGraphics());
			}
		};
		JButton nextPictureButton = new JButton("next");
		nextPictureButton.addActionListener(buttonListener);
		sliderPanel.add(nextPictureButton);
		
		// add resulting image
		frame.getContentPane().add(dataLabel);
		frame.getContentPane().add(testLabel);
		frame.getContentPane().add(matchLabel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		new FeatureExtractorTest();
	}
}
