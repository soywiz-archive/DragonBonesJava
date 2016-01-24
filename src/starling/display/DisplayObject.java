package starling.display;

import flash.geom.Matrix;

public class DisplayObject {
	public void dispose() {
		throw new Error();
	}

	public DisplayObjectContainer getParent() {
		throw new Error();
	}

	public void setBlendMode(String blendMode) {
		throw new Error();
	}

	public void setAlpha(double alpha) {
		throw new Error();
	}

	public void setPivotX(double value) {
		throw new Error();
	}

	public void setPivotY(double value) {
		throw new Error();
	}

	public double getPivotX() {
		throw new Error();
	}

	public double getPivotY() {
		throw new Error();
	}

	public Matrix getTransformationMatrix() {
		throw new Error();
	}

	public void setTransformationMatrix(Matrix matrix) {
		throw new Error();
	}

	public void setVisible(boolean visible) {
		throw new Error();
	}
}
