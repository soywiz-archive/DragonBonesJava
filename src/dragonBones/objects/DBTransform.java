package dragonBones.objects;

import flash.geom.Matrix;
import dragonBones.utils.TransformUtil;

/**
* Copyright 2012-2013. DragonBones. All Rights Reserved.
* @playerversion Flash 10.0
* @langversion 3.0
* @version 2.0
*/
public class DBTransform
{
	/**
	 * Position on the x axis.
	 */
	public double x;
	/**
	 * Position on the y axis.
	 */
	public double y;
	/**
	 * Skew on the x axis.
	 */
	public double skewX;
	/**
	 * skew on the y axis.
	 */
	public double skewY;
	/**
	 * Scale on the x axis.
	 */
	public double scaleX;
	/**
	 * Scale on the y axis.
	 */
	public double scaleY;
	/**
	 * The rotation of that DBTransform instance.
	 */
	public double getRotation()
	{
		return skewX;
	}
	public void setRotation(double value)
	{
		skewX = skewY = value;
	}
	/**
	 * Creat a new DBTransform instance.
	 */
	public DBTransform()
	{
		x = 0;
		y = 0;
		skewX = 0;
		skewY = 0;
		scaleX = 1;
		scaleY = 1;
	}
	/**
	 * Copy all properties from this DBTransform instance to the passed DBTransform instance.
	 * @param node
	 */
	public void copy(DBTransform transform)
	{
		x = transform.x;
		y = transform.y;
		skewX = transform.skewX;
		skewY = transform.skewY;
		scaleX = transform.scaleX;
		scaleY = transform.scaleY;
	}

	public void add(DBTransform transform)
	{
		x += transform.x;
		y += transform.y;
		skewX += transform.skewX;
		skewY += transform.skewY;
		scaleX *= transform.scaleX;
		scaleY *= transform.scaleY;
	}

	public void minus(DBTransform transform)
	{
		x -= transform.x;
		y -= transform.y;
		skewX -= transform.skewX;
		skewY -= transform.skewY;
		scaleX /= transform.scaleX;
		scaleY /= transform.scaleY;
	}

	public DBTransform divParent(DBTransform transform, boolean createNew = false)
	{
		DBTransform output = createNew ? new DBTransform() : this;
		Matrix parentMatrix = new Matrix();

		TransformUtil.transformToMatrix(transform, parentMatrix);
		double xtx = x - parentMatrix.tx;
		double yty = y - parentMatrix.ty;
		double adcb = parentMatrix.a * parentMatrix.d - parentMatrix.c * parentMatrix.b;

		output.x = (xtx * parentMatrix.d - yty * parentMatrix.c)/adcb;
		output.y = (yty * parentMatrix.a - xtx * parentMatrix.b)/adcb;
		output.scaleX = scaleX / transform.scaleX;
		output.scaleY = scaleY / transform.scaleY;
		output.skewX = skewX - transform.skewX;
		output.skewY = skewY - transform.skewY;
		return output;
	}

	public void normalizeRotation()
	{
		skewX = TransformUtil.normalizeRotation(skewX);
		skewY = TransformUtil.normalizeRotation(skewY);
	}

	/**
	 * Get a string representing all DBTransform property values.
	 * @return String All property values in a formatted string.
	 */
	public String toString()
	{
		return "x:" + x + " y:" + y + " skewX:" + skewX + " skewY:" + skewY + " scaleX:" + scaleX + " scaleY:" + scaleY;
	}
}
