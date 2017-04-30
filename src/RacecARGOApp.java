import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import dataset.ImageData;

public class RacecARGOApp {
	
	private JFrame frame;
	private ImageData imageData;
	private ImageIcon imageIcon;
	private int sizeToggle;
	private boolean running = true;
	
	
	ServerSocket server;
	Socket client;
	ArrayList<Thread> clientThreads = new ArrayList<>();
	
	public RacecARGOApp() throws InterruptedException {
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
//		while (running) {
			try {
				// wait for client
				server = new ServerSocket(1234);
				runWaitingLoop();
			} catch(Exception e) {
				System.out.printf("Error while waiting\n");
				e.printStackTrace();
				System.out.println("restarting");
			} finally {
				try {
					server.close();
					
					// close all client threads
					for (Thread clientThread : clientThreads) {
						clientThread.interrupt();
						clientThread.join(100);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
	    System.out.println("end");
	}
	
	
	private void runWaitingLoop() throws IOException {
		while (running) {
			System.out.println("waiting for client ...");
			client = server.accept();
			
			// clean up stored threads
			int threadIndex = 0;
			while (threadIndex < clientThreads.size()) {
				if (!clientThreads.get(threadIndex).isAlive()) {
					clientThreads.remove(threadIndex);
				} else {
					threadIndex++;
				}
			}
			
			System.out.printf("Client %d has connected!\n", clientThreads.size());
			
			// move client to separate thread
			Thread clientThread = new Thread(new VMMRSocket(client));
			clientThreads.add(clientThread);
			clientThread.start();
		}
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
		try {
			new RacecARGOApp();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
