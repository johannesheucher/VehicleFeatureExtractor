import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;

import org.opencv.core.Core;

import dataset.Dataset;

public class VehicleCounterApp {
	private static final String PATH = "D:/workspaces/VehicleData/test/";
	
	
	
	public VehicleCounterApp() {
		Dataset dataset = new Dataset(new File(PATH), false);
		
		String[] output = new String[dataset.getVehicleCounts().size()];
		Iterator<Entry<String, Integer>> entryIterator = dataset.getVehicleCounts().entrySet().iterator();
		int i = 0;
		while (entryIterator.hasNext()) {
			Entry<String, Integer> entry = entryIterator.next();
			output[i] = entry.getKey() + "," + entry.getValue();
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
