package dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class Dictionary {
	// TODO: Codewords
	private List<Mat> codewords;
	
	public Dictionary(List<Mat> descriptors, int size) {
		codewords = new ArrayList<>(size);
		
		Core.kmeans(data, K, bestLabels, criteria, attempts, flags, centers)
//		
//		don't lose time to find a way to use custom attributes.
//		Split ORB descriptor into 32 values (32 double attributes).
//		Maybe a custom distance function is necessary, because d(descMat, descMat) might be calculated other than by euklidean distance.
		
		// TODO
		// cluster descriptors (with kNN) into \size clusters (each cluster is one codeword of the dictionary)
		Attribute att0 = new Attribute("horsePower");
		Attribute att1 = new Attribute("weight");
		Attribute att2 = new Attribute("make", Arrays.asList("Porsche", "Mazda", "VW"));
		
		Instances data = new Instances("cars", new ArrayList<Attribute>(Arrays.asList(att0, att1, att2)), 7);
		
		Instance instance = new DenseInstance(3);
		instance.setValue(att0, 400);
		instance.setValue(att1, 1300);
		instance.setValue(att2, "Porsche");
		data.add(instance);
		instance = new DenseInstance(3);
		instance.setValue(att0, 296);
		instance.setValue(att1, 1200);
		instance.setValue(att2, "Porsche");
		data.add(instance);
		instance = new DenseInstance(3);
		instance.setValue(att0, 160);
		instance.setValue(att1, 1030);
		instance.setValue(att2, "Mazda");
		data.add(instance);
		instance = new DenseInstance(3);
		instance.setValue(att0, 105);
		instance.setValue(att1, 1300);
		instance.setValue(att2, "VW");
		data.add(instance);
		instance = new DenseInstance(3);
		instance.setValue(att0, 90);
		instance.setValue(att1, 800);
		instance.setValue(att2, "Porsche");
		data.add(instance);
		instance = new DenseInstance(3);
		instance.setValue(att0, 220);
		instance.setValue(att1, 1300);
		instance.setValue(att2, "VW");
		data.add(instance);
		instance = new DenseInstance(3);
		instance.setValue(att0, 131);
		instance.setValue(att1, 1000);
		instance.setValue(att2, "Mazda");
		data.add(instance);
		
		SimpleKMeans kmeans = new SimpleKMeans();
		kmeans.setPreserveInstancesOrder(true);
		try {
			kmeans.setNumClusters(3);
			kmeans.buildClusterer(data);
			
			int[] assignments = kmeans.getAssignments();
			
			int i = 0;
			for (int clusterNum : assignments) {
				System.out.printf("Instance %d %s -> Cluster %d\n", i, ((DenseInstance)data.get(i)).stringValue(2), clusterNum);
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
