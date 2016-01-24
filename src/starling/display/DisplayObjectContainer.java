package starling.display;

public class DisplayObjectContainer extends DisplayObject {
	private int numChildren;

	public int getChildIndex(DisplayObject child) {
		throw new Error();
	}


	public int getNumChildren() {
		throw new Error();
	}

	public void addChild(DisplayObject child) {
		throw new Error();
	}

	public void addChildAt(DisplayObject child, int index) {
		throw new Error();
	}

	public void removeChild(DisplayObject child) {
		throw new Error();
	}
}
