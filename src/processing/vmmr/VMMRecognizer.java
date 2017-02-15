package processing.vmmr;

import java.util.Arrays;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;

public class VMMRecognizer {
	
	private Instances instances;
	private Classifier classifier;
	
	public VMMRecognizer() {
	}
	
	
	public double train(ClassifierType classifierType, Instances instances, String[] options) {
		classifier = null;
		this.instances = instances;
		
		switch (classifierType) {
		case NeuralNetwork:
			break;
		case RandomForest:
			break;
		case SVM:
			SMO smo = new SMO();
			try {
				if (options != null) {
					smo.setOptions(options);
				}
				smo.buildClassifier(instances);
			} catch (Exception e) {
				e.printStackTrace();
			}
			classifier = smo;
			break;
		}
		
		if (classifier != null) {
			Evaluation evaluation;
			try {
				evaluation = new Evaluation(instances);
				evaluation.crossValidateModel(classifier, instances, 5, new Random(1));
				return evaluation.correct();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	
	public String classify(Instance instance) {
		if (instance.dataset() == null) {
//			System.out.println("Instance has no dataset");
			instance.setDataset(instances);
		}
//		instance.setClassMissing();
		
		double[] distribution = null;
		try {
			distribution = classifier.distributionForInstance(instance);
			double sum = 0;
			for (double value : distribution) {
				sum += value;
			}
			double classificationIndex = classifier.classifyInstance(instance);
			System.out.printf("\ndistribution: %s\tsum: %.2f\tclassIndex: %.2f\n", Arrays.toString(distribution), sum, classificationIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		double maxValue = 0;
		int maxIndex = 0;
		for (int i = 0; i < distribution.length; i++) {
			if (distribution[i] > maxValue) {
				maxValue = distribution[i];
				maxIndex = i;
			}
		}
		return instances.classAttribute().value(maxIndex);
	}
}
