package util;

public class WekaOption {
	private String name;
	private String[] values;
	
	private int curValueIndex;
	
	private final static String OPTION_ON = "OPTION_ON";
	private final static String OPTION_OFF = "OPTION_OFF";
	
	public WekaOption(String name, String[] values) {
		if (values == null) {
			values = (OPTION_ON + " " + OPTION_OFF).split(" ");
		}
		this.name = name;
		this.values = values;
		reset();
	}
	
	
	public void reset() {
		curValueIndex = 0;
	}
	
	
	public boolean hasNext() {
		return curValueIndex < values.length - 1;
	}
	
	
	public void next() {
		if (hasNext()) {
			curValueIndex++;
		} else {
			throw new IndexOutOfBoundsException();
		}
	}
	
	
	public int getNumberOfValues() {
		return values.length;
	}
	
	
	@Override
	public String toString() {
		if (values[curValueIndex].equals(OPTION_ON)) {
			return String.format("-%s", name);
		} else if (values[curValueIndex].equals(OPTION_OFF)) {
			return "";
		}
		return String.format("-%s %s", name, values[curValueIndex]);
	}
}
