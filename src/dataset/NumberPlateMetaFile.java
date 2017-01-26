package dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;

public class NumberPlateMetaFile {
	private Map<String, Rect> numberPlates;
	
	
	public NumberPlateMetaFile() {
		numberPlates = new HashMap<>();
	}
	
	
	public void clear() {
		numberPlates.clear();
	}
	
	
	public Rect getRect(String vehicleKey) {
		return numberPlates.get(vehicleKey);
	}
	
	
	public void setRect(String vehicleKey, Rect rect) {
		numberPlates.put(vehicleKey, rect);
	}
	
	
	public void read(String filename) throws IOException {
		clear();
		
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = reader.readLine();
		while (line != null) {
			String[] tokens = line.split(",");
			Point p = new Point(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
			Size s = new Size(Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
			Rect rect = new Rect(p, s);
			numberPlates.put(tokens[0], rect);
			
			line = reader.readLine();
		}
		reader.close();
	}
	
	
	public void write(String filename) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		Iterator<Entry<String, Rect>> entryIterator = numberPlates.entrySet().iterator();
		while (entryIterator.hasNext()) {
			Entry<String, Rect> entry = entryIterator.next();
			StringBuilder builder = new StringBuilder();
			builder.append(entry.getKey());
			builder.append(",");
			builder.append(entry.getValue().x);
			builder.append(",");
			builder.append(entry.getValue().y);
			builder.append(",");
			builder.append(entry.getValue().width);
			builder.append(",");
			builder.append(entry.getValue().height);
			
			writer.write(builder.toString());
			writer.newLine();
		}
		writer.close();
	}
}
