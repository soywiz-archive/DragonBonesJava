package dragonBones.core;

import flash.geom.ColorTransform;
import flash.geom.Matrix;

import dragonBones.objects.DBTransform;

public interface ISlotCacheGenerator extends ICacheUser
{
	DBTransform getGlobal();
	Matrix getGlobalTransformMatrix();
	boolean getColorChanged();
	ColorTransform getColorTransform();
	int getDisplayIndex();
}
