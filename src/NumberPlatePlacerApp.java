import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import dataset.Dataset;
import dataset.ImageData;
import dataset.NumberPlateMetaFile;


public class NumberPlatePlacerApp {
	private static final String PATH = "D:/workspaces/VehicleData/selection/";
	
	private static final String NUMBER_PLATE_FILENAME = "_NumberPlates.csv";
	private static final double IMAGE_SCALE = 0.3;
	
	private JFrame frame;
	
	private ImageData sourceImage;
	private ImageData dataImage;
	private ImageIcon dataIcon;
	private int pictureIndex;
	private NumberPlateMetaFile numberPlateMeta;
	private int sizeToggle;
	private Map<String, Double> imageScale;
	
	public NumberPlatePlacerApp() {
		Dataset dataset = new Dataset(new File(PATH), false);
		imageScale = new HashMap<>();
		for (ImageData image : dataset.getImageList()) {
			if (image.getMat().cols() > 1200) {
				image.resize((int)(image.getMat().cols() * IMAGE_SCALE));
				imageScale.put(image.getName(), IMAGE_SCALE);
			}
		}
		pictureIndex = 0;
		sizeToggle = 1;
		
		numberPlateMeta = new NumberPlateMetaFile();
		try {
			numberPlateMeta.read(PATH + NUMBER_PLATE_FILENAME);
		} catch (IOException e1) {
			System.out.println("No number plate meta file found.");
		}
		
		dataImage = dataset.getImageList().get(pictureIndex);
		dataIcon = new ImageIcon(dataImage.getImage());
		JLabel dataLabel = new JLabel(dataIcon);
		
		frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().setPreferredSize(new Dimension(1800, 800));
		
		// side panel
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
		sliderPanel.setPreferredSize(new Dimension(150, 600));
		frame.getContentPane().add(sliderPanel);
		
		ActionListener nextListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pictureIndex++;
				if (pictureIndex >= dataset.getImageList().size()) {
					pictureIndex = 0;
				}
				sourceImage = dataset.getImageList().get(pictureIndex);
				
				showNumberPlate();
			}
		};
		JButton nextPictureButton = new JButton(">");
		nextPictureButton.addActionListener(nextListener);
		sliderPanel.add(nextPictureButton);
		
		ActionListener nextWithoutListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Rect rect = null;
				ImageData lastSourceImage = sourceImage;
				do {
					pictureIndex++;
					if (pictureIndex >= dataset.getImageList().size()) {
						pictureIndex = 0;
					}
					sourceImage = dataset.getImageList().get(pictureIndex);
					String vehicleKey = sourceImage.getVehicleKey();
					rect = numberPlateMeta.getRect(vehicleKey);
				} while (rect != null && sourceImage != lastSourceImage);
				
				showNumberPlate();
			}
		};
		JButton nextWithoutPictureButton = new JButton(">>");
		nextWithoutPictureButton.addActionListener(nextWithoutListener);
		sliderPanel.add(nextWithoutPictureButton);
		
		ActionListener prevListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pictureIndex--;
				if (pictureIndex < 0) {
					pictureIndex = dataset.getImageList().size() - 1;
				}
				sourceImage = dataset.getImageList().get(pictureIndex);
				
				showNumberPlate();
			}
		};
		JButton prevPictureButton = new JButton("<");
		prevPictureButton.addActionListener(prevListener);
		sliderPanel.add(prevPictureButton);
		
		MouseListener imageMouseListener = new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {
				double scale = imageScale.getOrDefault(sourceImage.getName(), 1.0);
				int x = (int)(e.getX() / scale);
				int y = (int)(e.getY() / scale);
				Rect previousRect = numberPlateMeta.getRect(sourceImage.getVehicleKey());
				if (previousRect == null) {
					previousRect = new Rect();
				}
				if (e.isControlDown()) {
					numberPlateMeta.setRect(sourceImage.getVehicleKey(), new Rect(x, y, previousRect.width, previousRect.height));
				} else {
					numberPlateMeta.setRect(sourceImage.getVehicleKey(), new Rect(new Point(previousRect.x, previousRect.y), new Point(x, y)));
				}
				showNumberPlate();
			}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {}
		};
		dataLabel.addMouseListener(imageMouseListener);
		
		// add resulting image
		frame.getContentPane().add(dataLabel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				try {
					numberPlateMeta.write(PATH + NUMBER_PLATE_FILENAME);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		nextPictureButton.doClick();
	}
	
	
	private void showNumberPlate() {
		dataImage = (ImageData)sourceImage.clone();
		String vehicleKey = sourceImage.getVehicleKey();
		Rect rect = numberPlateMeta.getRect(vehicleKey);
		if (rect != null) {
			double scale = imageScale.getOrDefault(sourceImage.getName(), 1.0);
			Rect scaledRect = new Rect(new Point(rect.x * scale, rect.y * scale), new Size(rect.width * scale, rect.height * scale));
			Imgproc.rectangle(dataImage.getMat(), new Point(scaledRect.x,  scaledRect.y), new Point(scaledRect.x + scaledRect.width, scaledRect.y + scaledRect.height), new Scalar(255, 170, 255), 2);
		}

		dataIcon.setImage(dataImage.getImage());
		
		// update
		frame.getContentPane().update(frame.getContentPane().getGraphics());
		frame.setSize(frame.getWidth() + sizeToggle, frame.getHeight() + sizeToggle);		// quirk fix to place images at the top
		sizeToggle *= -1;
	}
	
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new NumberPlatePlacerApp();
	}
}
