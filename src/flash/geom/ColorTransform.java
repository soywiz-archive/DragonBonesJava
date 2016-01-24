package flash.geom;

public class ColorTransform {
	public double redOffset;
	public double greenOffset;
	public double blueOffset;
	public double alphaOffset;
	public double redMultiplier;
	public double greenMultiplier;
	public double blueMultiplier;
	public double alphaMultiplier;

	public ColorTransform() {
		this(1, 1, 1, 1, 0, 0, 0, 0);
	}

	public ColorTransform(double redMultiplier, double greenMultiplier, double blueMultiplier, double alphaMultiplier, double redOffset, double greenOffset, double blueOffset, double alphaOffset) {
		this.redMultiplier = redMultiplier;
		this.greenMultiplier = greenMultiplier;
		this.blueMultiplier = blueMultiplier;
		this.alphaMultiplier = alphaMultiplier;
		this.redOffset = redOffset;
		this.greenOffset = greenOffset;
		this.blueOffset = blueOffset;
		this.alphaOffset = alphaOffset;
	}
}
