import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.opencv.core.Core;

import dataset.Dataset;
import dataset.ImageData;

public class VehicleCounterApp {
	private static final String PATH = "D:/workspaces/VehicleData/";
	Map<String, Integer> vehicleCounts;
	
	
	public VehicleCounterApp() {
		vehicleCounts = new HashMap<>();
		Dataset dataset = new Dataset(new File(PATH), false);
		for (ImageData image : dataset.getImageList()) {
			String makemodel = image.getMakeModel();
			Integer count = vehicleCounts.get(makemodel);
			if (count == null) {
				count = 0;
			}
			count++;
			vehicleCounts.put(makemodel, count);
		}
		
		String[] output = new String[vehicleCounts.size()];
		Iterator<Entry<String, Integer>> entryIterator = vehicleCounts.entrySet().iterator();
		int i = 0;
		while (entryIterator.hasNext()) {
			Entry<String, Integer> entry = entryIterator.next();
			output[i] = entry.getKey() + ":    " + entry.getValue();
			i++;
		}
		Arrays.sort(output);
		for (String line : output) {
			System.out.println(line);
		}
	}
	
	
	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		new VehicleCounterApp();
	}
}
