package util;

import java.util.ArrayList;
import java.util.List;

public class WekaOptionsExpander {
	private List<WekaOption> options;
	private int numCombinations;
	private int numProgressedCombinations;
	
	public WekaOptionsExpander() {
		options = new ArrayList<>();
		numCombinations = 1;
		reset();
	}
	
	
	public void addOption(WekaOption option) {
		options.add(option);
		numCombinations *= option.getNumberOfValues();
	}
	
	
	public void reset() {
		for (WekaOption option : options) {
			option.reset();
		}
		numProgressedCombinations = 0;
	}
	
	
	public void next() {
		int curIndex = 0;
		boolean increased = false;
		while (curIndex < options.size() && !increased) {
			if (options.get(curIndex).hasNext()) {
				options.get(curIndex).next();
				increased = true;
			} else {
				options.get(curIndex).reset();
				curIndex++;
			}
		}
		if (!increased) {
			throw new IndexOutOfBoundsException();
		}
		numProgressedCombinations++;
	}
	
	
	public double getProgress() {
		return numProgressedCombinations / (double)numCombinations;
	}
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(options.size() * 2);
		for (WekaOption option : options) {
			builder.append(option.toString());
			builder.append(" ");
		}
		return builder.toString();
	}
}
