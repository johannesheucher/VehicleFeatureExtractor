import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import dataset.ImageData;
import util.NetworkUtil;

public class RacecARGOClientSocket implements Runnable {

	private Socket client;
	private DataOutputStream out;
	private DataInputStream inData;
	
	private String clientName;
	private String filename;
	
	private double lastLatitude = 0;
	private double lastLongitude = 0;
	private double walkedDistance = 0;
	
	private DecimalFormat numberFormat;
	
	private RacecARGOApp app;
	
	
	public RacecARGOClientSocket(Socket client, RacecARGOApp app) {
		this.client = client;
		this.app = app;
		
		NumberFormat formatUS = NumberFormat.getInstance(Locale.US);
		numberFormat = (DecimalFormat)formatUS;
		numberFormat.applyPattern("###.########");
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
			
			
			/**
			 * TODO:
			 * playername wird nicht mehr geschickt. Das passiert nur in den Settings, nicht aber im GameNavController.
			 * Dort muss es auch gemacht werden (nach kurzer Pause nach open).
			 * Dann darf es allerdings bei Settings nicht mehr geschickt werden. Oder doch? Hier darf dann eben nicht doppelt eine Datei angelegt werden.
			 * 
			 * TODO:
			 * Log anlegen (CSV):
			 * type 2: Wenn Datei nicht schon existiert, lege sie an. Name: Playername_Uhrzeit.log.csv.
			 * 		open
			 * 		schreibe: type 2, Uhrzeit, player name
			 * 		close
			 * type 1: Wenn Datei schon existiert,
			 * 		open
			 * 		schreibe: type 1, Uhrzeit, make, model
			 * 		close
			 * type 3: Wenn Datei schon existiert,
			 * 		open
			 * 		schreibe: type 3, Uhrzeit, lat, lon
			 * 		close
			 */
			
			/**
			 * Log file (CSV), filename = <playername>_<time>.log.csv:
			 * content: time, message type, message data
			 * <time>, 2, <playername>
			 * <time>, 1, <make and model>
			 * <time>, 3, <lat>, <lon>, <overall session distance>
			 */
			
			
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
				    
				    // show image if there is only one running thread to prevent conflicts
				    if (app.clientThreads.size() == 1) {
				    	app.updateImage(mat);
				    }
				    
				    // classify
					String label = vmmrApp.classifyImage(new ImageData(mat));
					System.out.printf("classified as:\t\t%s\n", label);
					
					
					// write make and model into log
				    if (filename != null) {
				    	writeLog(1, label);
				    }
				    
				    
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
				    clientName = clientName.replaceAll("[^a-zA-Z0-9]", "");
				    System.out.printf("Read player name: %s\n", clientName);
				    
				    // create log file for this client
				    if (filename == null) {
				    	filename = String.format("log/%s_%s.log.csv", clientName, timeStamp());
				    	File f = new File(filename);
				    	f.createNewFile();
				    	writeLog(2, clientName);
				    }
				    
					// send response
				    // FIXME nothing to do here?
				    
					break;
				case 3:
					// GPS data
					numExpectedBytes = 2 * Integer.BYTES;
					
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
					
					
					ByteBuffer location = ByteBuffer.wrap(data);
					int intLatitude = NetworkUtil.swapIntEndian(location.getInt());
					int intLongitude = NetworkUtil.swapIntEndian(location.getInt());
					double latitude  = intLatitude / 1000000.0;
					double longitude = intLongitude / 1000000.0;
					System.out.printf("Read position: (%s, %s)\n", numberFormat.format(latitude), numberFormat.format(longitude));
					
					
					// write lat, lon and distance into log
				    if (filename != null) {
				    	double distanceStep = distanceInKmBetweenEarthCoordinates(lastLatitude, lastLongitude, latitude, longitude);
				    	if (distanceStep < 0.1) {
				    		walkedDistance += distanceStep;
				    	}
				    	lastLatitude = latitude;
				    	lastLongitude = longitude;
				    	writeLog(3, String.format("%s,%s,%s", numberFormat.format(latitude), numberFormat.format(longitude), numberFormat.format(walkedDistance)));
				    }
				    
				    
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
	
	
	
	private double distanceInKmBetweenEarthCoordinates(double lat1, double lon1, double lat2, double lon2) {
		int earthRadiusKm = 6371;
		
		double dLat = Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(lon2-lon1);
		
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return earthRadiusKm * c;
	}
	
	
	
	private void writeLog(int messageType, String data) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
			writer.write(String.format("%s,%d,%s\n", timeStamp(), messageType, data));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private String timeStamp() {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		String dateTime = formatter.format(LocalDateTime.now());
		dateTime = dateTime.replaceAll(":", ".");
		return dateTime;
	}
}
