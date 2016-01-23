package dragonBones.objects;

import flash.geom.Point;

public final class TransformTimeline extends Timeline
{
	public String name;
	public boolean transformed;

	//第一帧的Transform
	public DBTransform originTransform;

	//第一帧的骨头的轴点
	public Point originPivot;

	public double offset;

	public TransformTimeline()
	{
		super();

		originTransform = new DBTransform();
		originTransform.scaleX = 1;
		originTransform.scaleY = 1;

		originPivot = new Point();
		offset = 0;
	}

	@Override
	public void dispose()
	{
		super.dispose();

		originTransform = null;
		originPivot = null;
	}
}
