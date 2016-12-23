package processing;

public class Const {
	public static final int IMAGE_WIDTH = 600;
	
	public static final float NUMBER_PLATE_ASPECT_RATIO = 4.5f;
	public static final float NUMBER_PLATE_ASPECT_RATIO_EPSILON = 0.55f;
	
	// originally for an image width of 800: {180, 300} x {30, 75}
	public static final int NUMBER_PLATE_MIN_WIDTH = (int)(IMAGE_WIDTH * 0.225);
	public static final int NUMBER_PLATE_MAX_WIDTH = (int)(IMAGE_WIDTH * 0.375);
	public static final int NUMBER_PLATE_MIN_HEIGHT = (int)(NUMBER_PLATE_MIN_WIDTH / (NUMBER_PLATE_ASPECT_RATIO + NUMBER_PLATE_ASPECT_RATIO_EPSILON));
	public static final int NUMBER_PLATE_MAX_HEIGHT = (int)(NUMBER_PLATE_MAX_WIDTH / (NUMBER_PLATE_ASPECT_RATIO - NUMBER_PLATE_ASPECT_RATIO_EPSILON));
}
