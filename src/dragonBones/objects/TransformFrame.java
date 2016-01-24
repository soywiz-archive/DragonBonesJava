package dragonBones.objects;

import flash.geom.Point;

/** @private */
final public class TransformFrame extends Frame
{
	//NaN:no tween, 10:auto tween, [-1, 0):ease in, 0:line easing, (0, 1]:ease out, (1, 2]:ease in out
	public double tweenEasing;
	public int tweenRotate;
	public boolean tweenScale;
//		public var displayIndex:int;
	public boolean visible;

	public DBTransform global;
	public DBTransform transform;
	public Point pivot;
	public Point scaleOffset;


	public TransformFrame()
	{
		super();

		tweenEasing = 10;
		tweenRotate = 0;
//			tweenScale = true;
//			displayIndex = 0;
		visible = true;

		global = new DBTransform();
		transform = new DBTransform();
		pivot = new Point();
		scaleOffset = new Point();
	}

	@Override
	public void dispose()
	{
		super.dispose();
		global = null;
		transform = null;
		pivot = null;
		scaleOffset = null;
	}
}
