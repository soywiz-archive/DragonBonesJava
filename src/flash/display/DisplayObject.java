package flash.display;

import flash.geom.Transform;

public class DisplayObject {
	private String blendMode;
	private boolean visible;

	public DisplayObjectContainer getParent() {
		throw new RuntimeException();
	}
	public int getHeight() {
		throw new Error();
	}

	public int getWidth() {
		throw new Error();
	}

	public Transform getTransform() {
		throw new Error();
	}

	public void setBlendMode(String blendMode) {
		throw new Error();
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
