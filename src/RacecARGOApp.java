import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import dataset.ImageData;
import util.NetworkUtil;

public class RacecARGOApp {
	
	private JFrame frame;
	private ImageData imageData;
	private ImageIcon imageIcon;
	private int sizeToggle;
	private boolean running = true;
	
	
	ServerSocket server;
	Socket client;
	DataOutputStream out;
	DataInputStream inData;
	
	public RacecARGOApp() {
		frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().setPreferredSize(new Dimension(1800, 700));
		
		// side panel
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
		sidePanel.setPreferredSize(new Dimension(150, 600));
		frame.getContentPane().add(sidePanel);
		
		sizeToggle = 1;
		
		imageData = new ImageData(new Mat(100, 100, CvType.CV_8UC1, new Scalar(128)));
		imageIcon = new ImageIcon(imageData.getImage());
		JLabel imageLabel = new JLabel(imageIcon);
		
		frame.getContentPane().add(imageLabel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				running = false;
			}
		});
		
		// build server socket
		while (running) {
			try {
				runServerSocket();
			}
			catch(Exception e) {
				System.out.print("Whoops! It didn't work!\n");
				e.printStackTrace();
				System.out.println("restarting");
			}
			finally {
				try {
					out.close();
					inData.close();
					client.close();
					server.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    System.out.println("end");
	}
	
	
	private void runServerSocket() throws Exception {
		server = new ServerSocket(1234);
		System.out.println("waiting for client ...");
		client = server.accept();
		System.out.print("Client has connected!\n");
		out = new DataOutputStream(client.getOutputStream());
		InputStream inStream = client.getInputStream();
		inData = new DataInputStream(inStream);
		
		// load classifier
		VMMRTestVehicleApp vmmrApp = new VMMRTestVehicleApp();
		
		while (running) {
			int messageType = inData.readUnsignedByte();
			System.out.printf("Got message of type: %d\n", messageType);
			switch (messageType) {
			case 1:
				// VMMR Request
				int numRows = NetworkUtil.swapShortEndian(inData.readUnsignedShort());
				int numCols = NetworkUtil.swapShortEndian(inData.readUnsignedShort());
				int numExpectedBytes = numRows * numCols;
				
				byte[] data = new byte[numExpectedBytes];
				int numReadBytes = 0;
				do {
					numReadBytes += inData.read(data, numReadBytes, numExpectedBytes - numReadBytes);
				} while (numReadBytes < numExpectedBytes || numReadBytes == 0);
				
				if (numReadBytes != numExpectedBytes) {
					System.out.printf(">>> WARNING: EXPECTED %d BYTES, READ %d\n", numExpectedBytes, numReadBytes);
					// TODO: Skip all bytes left in the stream to prevent reading them next time
				} else {
				// clear line break at the end of the stream
					inData.skip(1);
				}
				
			    Mat mat = new Mat(numRows, numCols, CvType.CV_8UC1);
			    mat.put(0, 0, data);
			    updateImage(mat);
			    
			    // classify
				String label = vmmrApp.classifyImage(new ImageData(mat));
				System.out.printf("classified as:\t\t%s\n", label);
			    
			    
				// send response
			    String response = Integer.toString(messageType) + "0" + label;
			    
			    byte numBytes = (byte)response.length();
			    out.writeShort(numBytes + 2);		// + 2 bytes (short) for message length
				out.writeBytes(response);
			    
				break;
			default:
				System.out.printf(">>> WARNING: UNKNOWN MESSAGE TYPE %d\n", messageType);
			}
		}
		
		//System.out.print("Sending string: '" + data + "'\n");
		//out.print(data);
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
