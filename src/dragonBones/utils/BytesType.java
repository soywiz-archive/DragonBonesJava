package dragonBones.utils;

import flash.utils.ByteArray;

/**
 * @private
 */
public class BytesType {
	public static final String SWF = "swf";
	public static final String PNG = "png";
	public static final String JPG = "jpg";
	public static final String ATF = "atf";
	public static final String ZIP = "zip";

	public static String getType(ByteArray bytes) {
		String outputType = null;
		int b1 = ((int) bytes.get(0)) & 0xFF;
		int b2 = ((int) bytes.get(1)) & 0xFF;
		int b3 = ((int) bytes.get(2)) & 0xFF;
		int b4 = ((int) bytes.get(3)) & 0xFF;
		if ((b1 == 0x46 || b1 == 0x43 || b1 == 0x5A) && b2 == 0x57 && b3 == 0x53) {
			//CWS FWS ZWS
			outputType = SWF;
		} else if (b1 == 0x89 && b2 == 0x50 && b3 == 0x4E && b4 == 0x47) {
			//89 50 4e 47 0d 0a 1a 0a
			outputType = PNG;
		} else if (b1 == 0xFF) {
			outputType = JPG;
		} else if (b1 == 0x41 && b2 == 0x54 && b3 == 0x46) {
			outputType = ATF;
		} else if (b1 == 0x50 && b2 == 0x4B) {
			outputType = ZIP;
		}
		return outputType;
	}
}
