import java.io.BufferedWriter;
import java.io.FileWriter;

import processing.vmmr.ClassifierType;
import processing.vmmr.VMMRecognizer;
import util.WekaOption;
import util.WekaOptionsExpander;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class VMMRParameterTestApp {
	//private static String ARFF_FILENAME = "D:/workspaces/VehicleData/training/_vehicles_30.arff";
	private static String ARFF_FILENAME = "D:/workspaces/VehicleData/ARFF_original_seven/_0043vehicles.arff";
	private static String OUTPUT_FILENAME = "D:/workspaces/VehicleData/training/_parametersEval.txt";
	
	public static void main(String[] args) throws Exception {
		DataSource source = new DataSource(ARFF_FILENAME);
		Instances instances = source.getDataSet(0);
		
		VMMRecognizer recognizer = new VMMRecognizer();
		
		WekaOptionsExpander optionsExpander = new WekaOptionsExpander();
		optionsExpander.addOption(new WekaOption("C", "2 3".split(" ")));
		optionsExpander.addOption(new WekaOption("N", "2 3".split(" ")));
		optionsExpander.addOption(new WekaOption("L", "0.5e-3 1.0e-3 2.0e-3 1.0e-2".split(" ")));
		optionsExpander.addOption(new WekaOption("P", "1.0e-5 1.0e-8 1.0e-11".split(" ")));
		//optionsExpander.addOption(new WekaOption("M", null));
		//optionsExpander.addOption(new WekaOption("V", "-1 2 5 8".split(" ")));
		//optionsExpander.addOption(new WekaOption("W", "1 2 4 15 36".split(" ")));
		
		// kernels
		String[] kernels = new String[] {
//				"\"weka.classifiers.functions.supportVector.PolyKernel",
//				"\"weka.classifiers.functions.supportVector.RBFKernel",
//				"\"weka.classifiers.functions.supportVector.Puk",
				"\"weka.classifiers.functions.supportVector.NormalizedPolyKernel"
		};
		optionsExpander.addOption(new WekaOption("K", kernels));
		optionsExpander.addOption(new WekaOption("E", "0.99 1.0 1.11 2.0".split(" ")));
		//optionsExpander.addOption(new WekaOption("L", "no yes".split(" ")));
		optionsExpander.addOption(new WekaOption("C", "-1\" 25007\"".split(" ")));
		// end kernels
		
		boolean hasNextOptions = true;
		double bestCorrect = 0;
		String bestOptions = "";
		StringBuilder output = new StringBuilder();
		while (hasNextOptions) {
			String options = optionsExpander.toString();
			double correct = recognizer.train(ClassifierType.SVM, instances, weka.core.Utils.splitOptions(options));
			
			String s = String.format("correct: %.0f\t\toptions: %s\t\tprogress: %.2f\n", correct, options, (optionsExpander.getProgress() * 100));
			output.append(s);
			System.out.print(s);
			
			if (correct > bestCorrect) {
				bestCorrect = correct;
				bestOptions = options;
			}
			
			try {
				optionsExpander.next();
			} catch (IndexOutOfBoundsException e) {
				hasNextOptions = false;
			}
		}
		String s = String.format("=== BEST ===================\ncorrect: %.0f\t\toptions: %s\t\t%.1f%%\n", bestCorrect, bestOptions, bestCorrect/instances.size() * 100);
		output.append(s);
		System.out.print(s);
		BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILENAME));
		writer.write(output.toString());
		writer.close();
	}
}
