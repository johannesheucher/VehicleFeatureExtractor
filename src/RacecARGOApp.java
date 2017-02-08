import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import dataset.ImageData;

public class RacecARGOApp {
	
	private JFrame frame;
	private ImageData imageData;
	private ImageIcon imageIcon;
	private int sizeToggle;
	
	
	public RacecARGOApp() {
		frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().setPreferredSize(new Dimension(1800, 800));
		
		// side panel
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
		sidePanel.setPreferredSize(new Dimension(150, 600));
		frame.getContentPane().add(sidePanel);
		
		sizeToggle = 1;
		
		imageData = new ImageData(new File("D:/workspaces/Temp Vehicle Data/Mini_One R50_00000005_000001_.jpg"));
		imageIcon = new ImageIcon(imageData.getImage());
		JLabel imageLabel = new JLabel(imageIcon);
		
		frame.getContentPane().add(imageLabel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// build server socket
		try {
			ServerSocket server = new ServerSocket(1234);
			System.out.println("waiting for client ...");
			Socket client = server.accept();
			System.out.print("Client has connected!\n");
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			InputStream inStream = client.getInputStream();
			InputStreamReader in = new InputStreamReader(inStream);
			DataInputStream inData = new DataInputStream(inStream);
			while (!in.ready()) {
				System.out.println("not ready");
			}
			
			long startTime = new Date().getTime();
			while (true) {
				// break after 60 seconds
				long duration = new Date().getTime() - startTime;
				System.out.printf("duration: %d\n", duration / 1000);
				if (duration > 1000 * 60) {
					break;
				}
				
				byte messageType = inData.readByte();
				System.out.printf("Got message of type: %d\n", messageType);
				switch (messageType) {
				case 1:
					// VMMR Request
//					byte[] bytes = new byte[12];
//					int rb = inData.read(bytes);
					
					int numRows = 92;//inData.readInt();
					int numCols = 200;//inData.readInt();
					int numExpectedBytes = numRows * numCols;
					
					byte[] data = new byte[numExpectedBytes];
					int numReadBytes = inData.read(data);
					if (numReadBytes != numExpectedBytes) {
						System.out.printf("Expected %d bytes, read %d\n", numExpectedBytes, numReadBytes);
					}
				    Mat mat = new Mat(numRows, numCols, CvType.CV_8UC1);
				    mat.put(0, 0, data);
				    updateImage(mat);
					break;
				}
				
//				int mid5 = in.read();
//				
//				String inMessage = in.readLine();
//				if (inMessage != null) {
//				    System.out.println("got data");
//				    
//				    
//				    Scanner scanner = new Scanner(inMessage);
//				    
//				    // parse
//				    int counter = 0;
//				    while (scanner.hasNext()) {
//					    try {
//					    	if (counter == 0) {
//					    		byte messageId = scanner.nextByte();
//					    		counter = 0;
//					    	} else if (counter == 1) {
//					    		int messageId = scanner.nextInt();
//					    		counter = 0;
//					    	} else if (counter == 2) {
//					    		scanner.skip(Pattern.compile("."));
//					    		counter = 0;
//					    	}
////						    int rows = scanner.nextInt();
////						    int cols = scanner.nextInt();
////						    System.out.println("Server: " + inMessage);
////						    
////						    byte[] data = new byte[rows * cols];
////						    inMessage.getBytes(9, Integer.MAX_VALUE, data, 0);
////						    Mat mat = new Mat(rows, cols, CvType.CV_8UC1);
////						    mat.put(0, 0, data);
////						    updateImage(mat);
//					    } catch (Exception e) {
//					    	e.printStackTrace();
//					    	counter++;
//					    }
//				    }
//				}
			}
			
			//System.out.print("Sending string: '" + data + "'\n");
			//out.print(data);
			out.close();
			in.close();
			client.close();
			server.close();
		}
		catch(Exception e) {
			System.out.print("Whoops! It didn't work!\n");
			e.printStackTrace();
		}
	    System.out.println("end");
	}
	
	
	public void updateImage(Mat mat) {
		imageData.setMat(mat);
		imageIcon.setImage(imageData.getImage());
		
		// update
		frame.getContentPane().update(frame.getContentPane().getGraphics());
		frame.setSize(frame.getWidth() + sizeToggle, frame.getHeight() + sizeToggle);		// quirk fix to place images at the top
		sizeToggle *= -1;
	}
	
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new RacecARGOApp();
	}
}
