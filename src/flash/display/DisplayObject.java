package flash.display;

public class DisplayObject {
	public DisplayObjectContainer getParent() {
		throw new RuntimeException();
	}
	public int getHeight() {
		throw new Error();
	}

	public int getWidth() {
		throw new Error();
	}
}
