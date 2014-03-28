package dnt.diag;

public final class Settings {
	
	public final static int TOMIC_DEVICE_V1 = 0;
	public final static String zh_CN = "zh-CN";
	public final static String en_US = "en-US";
	
	public static String language;
	public static int device;

	static {
		language = zh_CN;
		device = TOMIC_DEVICE_V1;
	}
}
