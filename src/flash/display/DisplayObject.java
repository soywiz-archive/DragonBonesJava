package flash.display;

import flash.geom.Transform;

public class DisplayObject {
	private String blendMode;
	private boolean visible;
	private int x;
	private int y;

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

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}
}
