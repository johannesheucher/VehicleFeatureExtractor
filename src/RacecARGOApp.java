import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

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
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
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
				String inMessage = in.readLine();
				if (inMessage != null) {
				    System.out.println("got data");
				    Scanner scanner = new Scanner(inMessage);
				    
				    // parse
				    try {
					    byte messageId = scanner.nextByte();
					    int rows = scanner.nextInt();
					    int cols = scanner.nextInt();
					    System.out.println("Server: " + inMessage);
					    
					    byte[] data = new byte[rows * cols];
					    inMessage.getBytes(9, Integer.MAX_VALUE, data, 0);
					    Mat mat = new Mat(rows, cols, CvType.CV_8UC1);
					    mat.put(0, 0, data);
					    updateImage(mat);
				    } catch (Exception e) {
				    	e.printStackTrace();
				    }
				}
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
