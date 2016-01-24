package dragonBones.fast;

import flash.geom.Matrix;

import dragonBones.cache.FrameCache;
import dragonBones.core.DBObject;
import dragonBones.objects.DBTransform;
import dragonBones.utils.TransformUtil;

//use namespace dragonBones_internal;


public class FastDBObject
{
	private String _name;

	/**
	 * An object that can contain any user extra data.
	 */
	public Object userData;

	/**
	 *
	 */
	public boolean inheritRotation;

	/**
	 *
	 */
	public boolean inheritScale;

	/**
	 *
	 */
	public boolean inheritTranslation;



	/** @private */
	DBTransform _global;
	/** @private */
	Matrix _globalTransformMatrix;

	/** @private */
	DBTransform _globalBackup;
	/** @private */
	Matrix _globalTransformMatrixBackup;

	static DBTransform _tempParentGlobalTransform = new DBTransform();

	FrameCache _frameCache;

	/** @private */
	void updateByCache()
	{
		_global = _frameCache.globalTransform;
		_globalTransformMatrix = _frameCache.globalTransformMatrix;
	}

	/** @private */
	void switchTransformToBackup()
	{
		if(_globalBackup == null)
		{
			_globalBackup = new DBTransform();
			_globalTransformMatrixBackup = new Matrix();
		}
		_global = _globalBackup;
		_globalTransformMatrix = _globalTransformMatrixBackup;
	}

	/**
	 * The armature this DBObject instance belongs to.
	 */
	public FastArmature armature;

	/** @private */
	protected DBTransform _origin;

	/** @private */
	protected boolean _visible;

	/** @private */
	FastBone _parent;

	/** @private */
	void setParent(FastBone value)
	{
		_parent = value;
	}

	public FastDBObject()
	{
		_globalTransformMatrix = new Matrix();

		_global = new DBTransform();
		_origin = new DBTransform();

		_visible = true;

		armature = null;
		_parent = null;

		userData = null;

		this.inheritRotation = true;
		this.inheritScale = true;
		this.inheritTranslation = true;
	}

	/**
	 * Cleans up any resources used by this DBObject instance.
	 */
	public void dispose()
	{
		userData = null;

		_globalTransformMatrix = null;
		_global = null;
		_origin = null;

		armature = null;
		_parent = null;
	}

	static private DBObject.TempOutput tempOutputObj = new DBObject.TempOutput();
	protected DBObject.TempOutput calculateParentTransform()
	{
		if(this.getParent() != null && (this.inheritTranslation || this.inheritRotation || this.inheritScale))
		{
			DBTransform parentGlobalTransform = this._parent._global;
			Matrix parentGlobalTransformMatrix = this._parent._globalTransformMatrix;

			if(	!this.inheritTranslation && (parentGlobalTransform.x != 0 || parentGlobalTransform.y != 0) ||
				!this.inheritRotation && (parentGlobalTransform.skewX != 0 || parentGlobalTransform.skewY != 0) ||
				!this.inheritScale && (parentGlobalTransform.scaleX != 1 || parentGlobalTransform.scaleY != 1))
			{
				parentGlobalTransform = FastDBObject._tempParentGlobalTransform;
				parentGlobalTransform.copy(this._parent._global);
				if(!this.inheritTranslation)
				{
					parentGlobalTransform.x = 0;
					parentGlobalTransform.y = 0;
				}
				if(!this.inheritScale)
				{
					parentGlobalTransform.scaleX = 1;
					parentGlobalTransform.scaleY = 1;
				}
				if(!this.inheritRotation)
				{
					parentGlobalTransform.skewX = 0;
					parentGlobalTransform.skewY = 0;
				}

				parentGlobalTransformMatrix = DBObject._tempParentGlobalTransformMatrix;
				TransformUtil.transformToMatrix(parentGlobalTransform, parentGlobalTransformMatrix);
			}
			tempOutputObj.parentGlobalTransform = parentGlobalTransform;
			tempOutputObj.parentGlobalTransformMatrix = parentGlobalTransformMatrix;
			return tempOutputObj;
		}
		return null;
	}

	protected Object updateGlobal()
	{
		calculateRelativeParentTransform();
		DBObject.TempOutput output = calculateParentTransform();
		if(output != null)
		{
			//计算父骨头绝对坐标
			Matrix parentMatrix = output.parentGlobalTransformMatrix;
			DBTransform parentGlobalTransform = output.parentGlobalTransform;
			//计算绝对坐标
			double x = _global.x;
			double y = _global.y;

			_global.x = parentMatrix.a * x + parentMatrix.c * y + parentMatrix.tx;
			_global.y = parentMatrix.d * y + parentMatrix.b * x + parentMatrix.ty;

			if(this.inheritRotation)
			{
				_global.skewX += parentGlobalTransform.skewX;
				_global.skewY += parentGlobalTransform.skewY;
			}

			if(this.inheritScale)
			{
				_global.scaleX *= parentGlobalTransform.scaleX;
				_global.scaleY *= parentGlobalTransform.scaleY;
			}
		}
		TransformUtil.transformToMatrix(_global, _globalTransformMatrix);
		return output;
	}

	protected void calculateRelativeParentTransform()
	{
	}

	public String getName()
	{
		return _name;
	}
	public void setName(String value)
	{
		_name = value;
	}

	/**
	 * This DBObject instance global transform instance.
	 * @see dragonBones.objects.DBTransform
	 */
	public DBTransform getGlobal()
	{
		return _global;
	}


	public Matrix getGlobalTransformMatrix()
	{
		return _globalTransformMatrix;
	}

	/**
	 * This DBObject instance related to parent transform instance.
	 * @see dragonBones.objects.DBTransform
	 */
	public DBTransform getOrigin()
	{
		return _origin;
	}

	/**
	 * Indicates the Bone instance that directly contains this DBObject instance if any.
	 */
	public FastBone getParent()
	{
		return _parent;
	}

	/** @private */

	public boolean getVisible()
	{
		return _visible;
	}
	public void setVisible(boolean value)
	{
		_visible = value;
	}

	public void setFrameCache(FrameCache cache)
	{
		_frameCache = cache;
	}
}
