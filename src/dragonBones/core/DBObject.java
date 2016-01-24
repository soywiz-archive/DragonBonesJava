package dragonBones.core;

import dragonBones.Armature;
import dragonBones.fast.FastDBObject;
import flash.geom.Matrix;

import dragonBones.Bone;
import dragonBones.objects.DBTransform;
import dragonBones.utils.TransformUtil;

//use namespace dragonBones_internal;

public class DBObject
{
	public String name;

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
	private DBTransform _global;
	/** @private */
	private Matrix _globalTransformMatrix;

	public static Matrix _tempParentGlobalTransformMatrix = new Matrix();
	private static DBTransform _tempParentGlobalTransform = new DBTransform();


	/**
	 * This DBObject instance global transform instance.
	 * @see dragonBones.objects.DBTransform
	 */
	public DBTransform getGlobal()
	{
		return _global;
	}

	/** @private */
	protected DBTransform _origin;
	/**
	 * This DBObject instance related to parent transform instance.
	 * @see dragonBones.objects.DBTransform
	 */
	public DBTransform getOrigin()
	{
		return _origin;
	}

	/** @private */
	protected DBTransform _offset;
	/**
	 * This DBObject instance offset transform instance (For manually control).
	 * @see dragonBones.objects.DBTransform
	 */
	public DBTransform getOffset()
	{
		return _offset;
	}

	/** @private */
	protected boolean _visible;
	public boolean getVisible()
	{
		return _visible;
	}
	public void setVisible(boolean value)
	{
		_visible = value;
	}

	/** @private */
	protected Armature _armature;
	/**
	 * The armature this DBObject instance belongs to.
	 */
	public Armature getArmature()
	{
		return _armature;
	}
	/** @private */
	public void setArmature(Armature value)
	{
		_armature = value;
	}

	/** @private */
	private Bone _parent;
	/**
	 * Indicates the Bone instance that directly contains this DBObject instance if any.
	 */
	public Bone getParent()
	{
		return _parent;
	}
	/** @private */
	public void setParent(Bone value)
	{
		_parent = value;
	}

	public DBObject()
	{
		_globalTransformMatrix = new Matrix();

		_global = new DBTransform();
		_origin = new DBTransform();
		_offset = new DBTransform();
		_offset.scaleX = _offset.scaleY = 1;

		_visible = true;

		_armature = null;
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
		_offset = null;

		_armature = null;
		_parent = null;
	}

	protected void calculateRelativeParentTransform()
	{
	}

	static public class TempOutput {
		public DBTransform parentGlobalTransform;
		public Matrix parentGlobalTransformMatrix;
		public TempOutput() {
		}

		public TempOutput(DBTransform parentGlobalTransform, Matrix parentGlobalTransformMatrix) {
			this.parentGlobalTransform = parentGlobalTransform;
			this.parentGlobalTransformMatrix = parentGlobalTransformMatrix;
		}
	}


	protected DBObject.TempOutput calculateParentTransform()
	{
		if(this.getParent() != null && (this.inheritTranslation || this.inheritRotation || this.inheritScale))
		{
			DBTransform parentGlobalTransform = this._parent._globalTransformForChild;
			Matrix parentGlobalTransformMatrix = this._parent._globalTransformMatrixForChild;

			if(!this.inheritTranslation || !this.inheritRotation || !this.inheritScale)
			{
				parentGlobalTransform = DBObject._tempParentGlobalTransform;
				parentGlobalTransform.copy(this._parent._globalTransformForChild);
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

			return new TempOutput(parentGlobalTransform, parentGlobalTransformMatrix);
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
}
