package dragonBones.utils;

import flash.geom.Matrix;

import dragonBones.objects.DBTransform;

/**
 * @author CG
 */
final public class TransformUtil
{
	public static final double ANGLE_TO_RADIAN = Math.PI / 180;
	public static final double RADIAN_TO_ANGLE = 180 / Math.PI;

	private static final double HALF_PI = Math.PI * 0.5;
	private static final double DOUBLE_PI = Math.PI * 2;

	private static final Matrix _helpTransformMatrix = new Matrix();
	private static final Matrix _helpParentTransformMatrix = new Matrix();

	public static void transformToMatrix(DBTransform transform, Matrix matrix)
	{
		matrix.a = transform.scaleX * Math.cos(transform.skewY);
		matrix.b = transform.scaleX * Math.sin(transform.skewY);
		matrix.c = -transform.scaleY * Math.sin(transform.skewX);
		matrix.d = transform.scaleY * Math.cos(transform.skewX);
		matrix.tx = transform.x;
		matrix.ty = transform.y;
	}

	public static double formatRadian(double radian)
	{
		//radian %= DOUBLE_PI;
		if (radian > Math.PI)
		{
			radian -= DOUBLE_PI;
		}
		if (radian < -Math.PI)
		{
			radian += DOUBLE_PI;
		}
		return radian;
	}

	//这个算法如果用于骨骼间的绝对转相对请改为DBTransform.divParent()方法
	public static void globalToLocal(DBTransform transform, DBTransform parent)
	{
		transformToMatrix(transform, _helpTransformMatrix);
		transformToMatrix(parent, _helpParentTransformMatrix);

		_helpParentTransformMatrix.invert();
		_helpTransformMatrix.concat(_helpParentTransformMatrix);

		matrixToTransform(_helpTransformMatrix, transform, transform.scaleX * parent.scaleX >= 0, transform.scaleY * parent.scaleY >= 0);
	}

	static private boolean equalsEpsilon(double a, double b) {
		return Math.abs(a - b) <= 0.0001;
	}

	public static void matrixToTransform(Matrix matrix, DBTransform transform, boolean scaleXF, boolean scaleYF)
	{
		transform.x = matrix.tx;
		transform.y = matrix.ty;
		transform.scaleX = Math.sqrt(matrix.a * matrix.a + matrix.b * matrix.b) * (scaleXF ? 1 : -1);
		transform.scaleY = Math.sqrt(matrix.d * matrix.d + matrix.c * matrix.c) * (scaleYF ? 1 : -1);

		// @TODO: Remove allocation!
		double[] skewXArray = new double[4];
		skewXArray[0] = Math.acos(matrix.d / transform.scaleY);
		skewXArray[1] = -skewXArray[0];
		skewXArray[2] = Math.asin(-matrix.c / transform.scaleY);
		skewXArray[3] = skewXArray[2] >= 0 ? Math.PI - skewXArray[2] : skewXArray[2] - Math.PI;

		if(equalsEpsilon(skewXArray[0], skewXArray[2]) || equalsEpsilon(skewXArray[0], skewXArray[3]))
		{
			transform.skewX = skewXArray[0];
		}
		else
		{
			transform.skewX = skewXArray[1];
		}

		// @TODO: Remove allocation!
		double[] skewYArray = new double[4];
		skewYArray[0] = Math.acos(matrix.a / transform.scaleX);
		skewYArray[1] = -skewYArray[0];
		skewYArray[2] = Math.asin(matrix.b / transform.scaleX);
		skewYArray[3] = skewYArray[2] >= 0 ? Math.PI - skewYArray[2] : skewYArray[2] - Math.PI;

		if(equalsEpsilon(skewYArray[0], skewYArray[2]) || equalsEpsilon(skewYArray[0], skewYArray[3]))
		{
			transform.skewY = skewYArray[0];
		}
		else
		{
			transform.skewY = skewYArray[1];
		}
	}
	//确保角度在-180到180之间
	public static double normalizeRotation(double rotation)
	{
		rotation = (rotation + Math.PI)%(2*Math.PI);
		rotation = rotation > 0 ? rotation : 2*Math.PI + rotation;
		return rotation - Math.PI;
	}
}
