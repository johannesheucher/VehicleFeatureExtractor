import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import dataset.ImageData;
import util.NetworkUtil;

public class RacecARGOClientSocket implements Runnable {

	private Socket client;
	private DataOutputStream out;
	private DataInputStream inData;
	
	private String clientName;
	
	
	public RacecARGOClientSocket(Socket client) {
		this.client = client;
	}
	
	
	@Override
	public void run() {
		try {
			out = new DataOutputStream(client.getOutputStream());
			InputStream inStream = client.getInputStream();
			inData = new DataInputStream(inStream);
			
			byte[] data;
			int numExpectedBytes;
			int numReadBytes;
			
			// load classifier
			VMMRTestVehicleApp vmmrApp = new VMMRTestVehicleApp();
			
			while (true) {
				int messageType = inData.readUnsignedByte();
				System.out.printf("Got message of type: %d\n", messageType);
				switch (messageType) {
				case 1:
					// VMMR Request
					int numRows = NetworkUtil.swapShortEndian(inData.readUnsignedShort());
					int numCols = NetworkUtil.swapShortEndian(inData.readUnsignedShort());
					numExpectedBytes = numRows * numCols;
					
					data = new byte[numExpectedBytes];
					numReadBytes = 0;
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
				    //updateImage(mat);
				    
				    // classify
					String label = vmmrApp.classifyImage(new ImageData(mat));
					System.out.printf("classified as:\t\t%s\n", label);
				    
				    
					// send response ("0" is "no error")
				    String response = Integer.toString(messageType) + "0" + label;
				    
				    byte numBytes = (byte)response.length();
				    out.writeShort(numBytes + 2);		// + 2 bytes (short) for message length
					out.writeBytes(response);
				    
					break;
				case 2:
					// User name
					numExpectedBytes = NetworkUtil.swapShortEndian(inData.readUnsignedShort());
					
					data = new byte[numExpectedBytes];
					numReadBytes = 0;
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
					
				    clientName = new String(data);
				    
				    
					// send response
				    // FIXME nothing to do here?
				    
					break;
				default:
					System.out.printf(">>> WARNING: UNKNOWN MESSAGE TYPE %d\n", messageType);
				}
			}
		} catch (Exception e) {
			System.out.printf("Error for client %s\n", clientName);
			e.printStackTrace();
		} finally {
			try {
				out.close();
				inData.close();
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.printf("Finished Socket for client %s\n", clientName);
	}
}
