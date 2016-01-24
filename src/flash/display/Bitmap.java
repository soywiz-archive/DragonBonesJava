package flash.display;

public class Bitmap extends DisplayObject {
	private Object bitmapData;

	public Bitmap(Object bitmapData) {
		this.bitmapData = bitmapData;
	}

	public Object getBitmapData() {
		return bitmapData;
	}
}
