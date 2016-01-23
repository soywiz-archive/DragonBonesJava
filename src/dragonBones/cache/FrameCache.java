package dragonBones.cache;

import flash.geom.Matrix;

import dragonBones.objects.DBTransform;

public class FrameCache
{
	private static final DBTransform ORIGIN_TRAMSFORM = new DBTransform();
	private static final Matrix ORIGIN_MATRIX = new Matrix();

	public DBTransform globalTransform = new DBTransform();
	public Matrix globalTransformMatrix = new Matrix();

	public FrameCache()
	{
	}

	//浅拷贝提高效率
	public void copy(FrameCache frameCache)
	{
		globalTransform = frameCache.globalTransform;
		globalTransformMatrix = frameCache.globalTransformMatrix;
	}

	public void clear()
	{
		globalTransform = ORIGIN_TRAMSFORM;
		globalTransformMatrix = ORIGIN_MATRIX;
	}
}
