package dragonBones.objects;

import flash.geom.Point;

/** @private */
final public class DisplayData
{
	public static final String ARMATURE = "armature";
	public static final String IMAGE = "image";

	public String name;
	public String slotName;
	public String type;
	public DBTransform transform;
	public Point pivot;

	public DisplayData()
	{
		transform = new DBTransform();
		pivot = new Point();
	}

	public void dispose()
	{
		transform = null;
		pivot = null;
	}
}
