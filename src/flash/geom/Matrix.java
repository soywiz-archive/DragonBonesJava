package flash.geom;

public class Matrix {
	public double a;
	public double b;
	public double c;
	public double d;
	public double tx;
	public double ty;

	public void setTo(double a, double b, double c, double d, double tx, double ty) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.tx = tx;
		this.ty = ty;
	}

	public void copyFrom(Matrix that) {
		setTo(that.a, that.b, that.c, that.d, that.tx, that.ty);
	}

	public void invert() {
		throw new RuntimeException("Not implemented");
	}

	public void concat(Matrix that) {
		throw new RuntimeException("Not implemented");
	}

	public void scale(double sx, double sy) {
		throw new Error();
	}
}
