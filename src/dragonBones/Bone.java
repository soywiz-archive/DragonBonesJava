package dragonBones;

import flash.errors.ArgumentError;
import flash.geom.Matrix;
import flash.geom.Point;

import dragonBones.animation.AnimationState;
import dragonBones.animation.TimelineState;
import dragonBones.core.DBObject;
import dragonBones.events.FrameEvent;
import dragonBones.events.SoundEvent;
import dragonBones.events.SoundEventManager;
import dragonBones.objects.BoneData;
import dragonBones.objects.DBTransform;
import dragonBones.objects.Frame;
import dragonBones.utils.TransformUtil;

import java.util.ArrayList;
import java.util.Objects;

//use namespace dragonBones_internal;

public class Bone extends DBObject
{
	/**
	 * The instance dispatch sound event.
	 */
	private static final SoundEventManager _soundManager = SoundEventManager.getInstance();

	public static Bone initWithBoneData(BoneData boneData)
	{
		Bone outputBone = new Bone();

		outputBone.name = boneData.name;
		outputBone.inheritRotation = boneData.inheritRotation;
		outputBone.inheritScale = boneData.inheritScale;
		outputBone.getOrigin().copy(boneData.transform);

		return outputBone;
	}

	public boolean applyOffsetTranslationToChild = true;

	public boolean applyOffsetRotationToChild = true;

	public boolean applyOffsetScaleToChild = false;

	/**
	 * AnimationState that slots belong to the bone will be controlled by.
	 * Sometimes, we want slots controlled by a spedific animation state when animation is doing mix or addition.
	 */
	public String displayController;

	/** @private */
	protected ArrayList<Bone> _boneList;

	/** @private */
	protected ArrayList<Slot> _slotList;

	/** @private */
	protected ArrayList<TimelineState> _timelineStateList;

	/** @private */
	protected DBTransform _tween;

	/** @private */
	protected Point _tweenPivot;

	/** @private */
	protected int _needUpdate;

	/** @private */
	//dragonBones_internal var _isColorChanged:Boolean;

	/** @private */
	public DBTransform _globalTransformForChild;
	/** @private */
	public Matrix _globalTransformMatrixForChild;

	private DBTransform _tempGlobalTransformForChild;
	private Matrix _tempGlobalTransformMatrixForChild;

	public Bone()
	{
		super();

		_tween = new DBTransform();
		_tweenPivot = new Point();
		_tween.scaleX = _tween.scaleY = 1;

		_boneList = new ArrayList<Bone>();
		_slotList = new ArrayList<Slot>();
		_timelineStateList = new ArrayList<TimelineState>();

		_needUpdate = 2;
		//_isColorChanged = false;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void dispose()
	{
		if(_boneList == null)
		{
			return;
		}

		super.dispose();
		int i = _boneList.size();
		while(i -- > 0)
		{
			_boneList.get(i).dispose();
		}

		i = _slotList.size();
		while(i -- > 0)
		{
			_slotList.get(i).dispose();
		}

		_boneList.clear();
		_slotList.clear();
		_timelineStateList.clear();

		_tween = null;
		_tweenPivot = null;
		_boneList = null;
		_slotList = null;
		_timelineStateList = null;
	}

//骨架装配
	/**
	 * If contains some bone or slot
	 * @param Slot or Bone instance
	 * @return Boolean
	 * @see dragonBones.core.DBObject
	 */
	public boolean contains(DBObject child)
	{
		if(child == null)
		{
			throw new ArgumentError();
		}
		if(child == this)
		{
			return false;
		}
		DBObject ancestor = child;
		while(!(ancestor == this || ancestor == null))
		{
			ancestor = ancestor.getParent();
		}
		return ancestor == this;
	}

	/**
	 * Add a bone as child
	 * @param a Bone instance
	 * @see dragonBones.core.DBObject
	 */
	public void addChildBone(Bone childBone, boolean updateLater = false)
	{
		if(childBone == null)
		{
			throw new ArgumentError();
		}

		if(childBone == this || childBone.contains(this))
		{
			throw new ArgumentError("An Bone cannot be added as a child to itself or one of its children (or children's children, etc.)");
		}

		if(childBone.getParent() == this)
		{
			return;
		}

		if(childBone.getParent() != null)
		{
			childBone.getParent().removeChildBone(childBone);
		}

		_boneList.set(_boneList.size(), childBone);
		childBone.setParent(this);
		childBone.setArmature(_armature);

		if(_armature != null && !updateLater)
		{
			_armature.updateAnimationAfterBoneListChanged();
		}
	}

	/**
	 * remove a child bone
	 * @param a Bone instance
	 * @see dragonBones.core.DBObject
	 */
	public void removeChildBone(Bone childBone, boolean updateLater = false)
	{
		if(childBone == null)
		{
			throw new ArgumentError();
		}

		if(!_boneList.contains(childBone))
		{
			throw new ArgumentError();
		}

		_boneList.remove(childBone);


		childBone.setParent(null);
		childBone.setArmature(null);

		if(_armature != null && !updateLater)
		{
			_armature.updateAnimationAfterBoneListChanged(false);
		}
	}

	/**
	 * Add a slot as child
	 * @param a Slot instance
	 * @see dragonBones.core.DBObject
	 */
	public void addSlot(Slot childSlot)
	{
		if(childSlot == null)
		{
			throw new ArgumentError();
		}

		if(childSlot.getParent())
		{
			childSlot.getParent().removeSlot(childSlot);
		}

		_slotList.set(_slotList.size(), childSlot);
		childSlot.setParent(this);
		childSlot.setArmature(this._armature);
	}

	/**
	 * remove a child slot
	 * @param a Slot instance
	 * @see dragonBones.core.DBObject
	 */
	public void removeSlot(Slot childSlot)
	{
		if(childSlot == null)
		{
			throw new ArgumentError();
		}

		if(!_slotList.contains(childSlot))
		{
			throw new ArgumentError();
		}

		_slotList.remove(childSlot);
		childSlot.setParent(null);
		childSlot.setArmature(null);
	}

	/** @private */
	@Override
	public void setArmature(Armature value)
	{
		if(_armature == value)
		{
			return;
		}
		if(_armature != null)
		{
			_armature.removeBoneFromBoneList(this);
			_armature.updateAnimationAfterBoneListChanged(false);
		}
		_armature = value;
		if(_armature != null)
		{
			_armature.addBoneToBoneList(this);
		}

		int i = _boneList.size();
		while(i -- > 0)
		{
			_boneList.get(i).setArmature(this._armature);
		}

		i = _slotList.size();
		while(i -- >0)
		{
			_slotList.get(i).setArmature(this._armature);
		}
	}

	/**
	 * Get all Bone instance associated with this bone.
	 * @return A Vector.&lt;Slot&gt; instance.
	 * @see dragonBones.Slot
	 */
	public ArrayList<Bone> getBones(boolean returnCopy = true)
	{
		return returnCopy?_boneList.concat():_boneList;
	}

	/**
	 * Get all Slot instance associated with this bone.
	 * @return A Vector.&lt;Slot&gt; instance.
	 * @see dragonBones.Slot
	 */
	public ArrayList<Slot> getSlots(boolean returnCopy = true)
	{
		return returnCopy?_slotList.concat():_slotList;
	}

//动画
	/**
	 * Force update the bone in next frame even if the bone is not moving.
	 */
	public void invalidUpdate()
	{
		_needUpdate = 2;
	}

	@Override protected void calculateRelativeParentTransform()
	{
		_global.scaleX = this._origin.scaleX * _tween.scaleX * this._offset.scaleX;
		_global.scaleY = this._origin.scaleY * _tween.scaleY * this._offset.scaleY;
		_global.skewX = this._origin.skewX + _tween.skewX + this._offset.skewX;
		_global.skewY = this._origin.skewY + _tween.skewY + this._offset.skewY;
		_global.x = this._origin.x + _tween.x + this._offset.x;
		_global.y = this._origin.y + _tween.y + this._offset.y;
	}

	private void update()
	{
		update(false);
	}

		/** @private */
		void update(boolean needUpdate)
	{
		_needUpdate --;
		if(needUpdate || _needUpdate > 0 || (this._parent && this._parent._needUpdate > 0))
		{
			_needUpdate = 1;
		}
		else
		{
			return;
		}

		blendingTimeline();

	//计算global
		Object result = updateGlobal();
		DBTransform parentGlobalTransform = result != null ? result.parentGlobalTransform : null;
		Matrix parentGlobalTransformMatrix = result != null ? result.parentGlobalTransformMatrix : null;

	//计算globalForChild
		boolean ifExistOffsetTranslation = _offset.x != 0 || _offset.y != 0;
		boolean ifExistOffsetScale = _offset.scaleX != 1 || _offset.scaleY != 1;
		boolean ifExistOffsetRotation = _offset.skewX != 0 || _offset.skewY != 0;

		if(	(!ifExistOffsetTranslation || applyOffsetTranslationToChild) &&
			(!ifExistOffsetScale || applyOffsetScaleToChild) &&
			(!ifExistOffsetRotation || applyOffsetRotationToChild))
		{
			_globalTransformForChild = _global;
			_globalTransformMatrixForChild = _globalTransformMatrix;
		}
		else
		{
			if(_tempGlobalTransformForChild == null)
			{
				_tempGlobalTransformForChild = new DBTransform();
			}
			_globalTransformForChild = _tempGlobalTransformForChild;

			if(_tempGlobalTransformMatrixForChild == null)
			{
				_tempGlobalTransformMatrixForChild = new Matrix();
			}
			_globalTransformMatrixForChild = _tempGlobalTransformMatrixForChild;

			_globalTransformForChild.x = this._origin.x + _tween.x;
			_globalTransformForChild.y = this._origin.y + _tween.y;
			_globalTransformForChild.scaleX = this._origin.scaleX * _tween.scaleX;
			_globalTransformForChild.scaleY = this._origin.scaleY * _tween.scaleY;
			_globalTransformForChild.skewX = this._origin.skewX + _tween.skewX;
			_globalTransformForChild.skewY = this._origin.skewY + _tween.skewY;

			if(applyOffsetTranslationToChild)
			{
				_globalTransformForChild.x += this._offset.x;
				_globalTransformForChild.y += this._offset.y;
			}
			if(applyOffsetScaleToChild)
			{
				_globalTransformForChild.scaleX *= this._offset.scaleX;
				_globalTransformForChild.scaleY *= this._offset.scaleY;
			}
			if(applyOffsetRotationToChild)
			{
				_globalTransformForChild.skewX += this._offset.skewX;
				_globalTransformForChild.skewY += this._offset.skewY;
			}

			TransformUtil.transformToMatrix(_globalTransformForChild, _globalTransformMatrixForChild);
			if(parentGlobalTransformMatrix != null)
			{
				_globalTransformMatrixForChild.concat(parentGlobalTransformMatrix);
				TransformUtil.matrixToTransform(_globalTransformMatrixForChild, _globalTransformForChild, _globalTransformForChild.scaleX * parentGlobalTransform.scaleX >= 0, _globalTransformForChild.scaleY * parentGlobalTransform.scaleY >= 0 );
			}
		}
	}

	/** @private */
	public void hideSlots()
	{
		for (Slot childSlot : _slotList)
		{
			childSlot.changeDisplay(-1);
		}
	}

	/** @private When bone timeline enter a key frame, call this func*/
	public void arriveAtFrame(Frame frame, TimelineState timelineState, AnimationState animationState, boolean isCross)
	{
		boolean displayControl =
			animationState.displayControl &&
			(displayController != null || Objects.equals(displayController, animationState.getName())) &&
			animationState.containsBoneMask(name);

		if(displayControl)
		{
			Slot childSlot;
			if(frame.event && this._armature.hasEventListener(FrameEvent.BONE_FRAME_EVENT))
			{
				FrameEvent frameEvent = new FrameEvent(FrameEvent.BONE_FRAME_EVENT);
				frameEvent.bone = this;
				frameEvent.animationState = animationState;
				frameEvent.frameLabel = frame.event;
				this._armature._eventList.push(frameEvent);
			}
			if(frame.sound && _soundManager.hasEventListener(SoundEvent.SOUND))
			{
				SoundEvent soundEvent = new SoundEvent(SoundEvent.SOUND);
				soundEvent.armature = this._armature;
				soundEvent.animationState = animationState;
				soundEvent.sound = frame.sound;
				_soundManager.dispatchEvent(soundEvent);
			}

			//[TODO]currently there is only gotoAndPlay belongs to frame action. In future, there will be more.
			//后续会扩展更多的action，目前只有gotoAndPlay的含义
			if(frame.action != null)
			{
				for (Slot childSlot2 : _slotList)
				{
					Armature childArmature = childSlot2.childArmature;
					if(childArmature != null)
					{
						childArmature.animation.gotoAndPlay(frame.action);
					}
				}
			}
		}
	}

	/** @private */
	public void addState(TimelineState timelineState)
	{
		if(_timelineStateList.indexOf(timelineState) < 0)
		{
			_timelineStateList.add(timelineState);
			_timelineStateList.sort(sortState);
		}
	}

	/** @private */
	public void removeState(TimelineState timelineState)
	{
		_timelineStateList.remove(timelineState);
	}

	/** @private */
	void removeAllStates()
	{
		_timelineStateList.clear();
	}

	private void blendingTimeline()
	{
		TimelineState timelineState;
		DBTransform transform;
		Point pivot;
		double weight;

		int i = _timelineStateList.size();
		if(i == 1)
		{
			timelineState = _timelineStateList.get(0);
			weight = timelineState._animationState.weight * timelineState._animationState.getFadeWeight();
			timelineState._weight = weight;
			transform = timelineState._transform;
			pivot = timelineState._pivot;

			_tween.x = transform.x * weight;
			_tween.y = transform.y * weight;
			_tween.skewX = transform.skewX * weight;
			_tween.skewY = transform.skewY * weight;
			_tween.scaleX = 1 + (transform.scaleX - 1) * weight;
			_tween.scaleY = 1 + (transform.scaleY - 1) * weight;

			_tweenPivot.x = pivot.x * weight;
			_tweenPivot.y = pivot.y * weight;
		}
		else if(i > 1)
		{
			double x = 0;
			double y = 0;
			double skewX = 0;
			double skewY = 0;
			double scaleX = 1;
			double scaleY = 1;
			double pivotX = 0;
			double pivotY = 0;

			double weigthLeft = 1;
			double layerTotalWeight = 0;
			int prevLayer = _timelineStateList.get(i - 1)._animationState.getLayer();
			int currentLayer;

			//Traversal the layer from up to down
			//layer由高到低依次遍历

			while(i -- > 0)
			{
				timelineState = _timelineStateList.get(i);

				currentLayer = timelineState._animationState.getLayer();
				if(prevLayer != currentLayer)
				{
					if(layerTotalWeight >= weigthLeft)
					{
						timelineState._weight = 0;
						break;
					}
					else
					{
						weigthLeft -= layerTotalWeight;
					}
				}
				prevLayer = currentLayer;

				weight = timelineState._animationState.weight * timelineState._animationState.getFadeWeight() * weigthLeft;
				timelineState._weight = weight;
				if(weight != 0)
				{
					transform = timelineState._transform;
					pivot = timelineState._pivot;

					x += transform.x * weight;
					y += transform.y * weight;
					skewX += transform.skewX * weight;
					skewY += transform.skewY * weight;
					scaleX += (transform.scaleX - 1) * weight;
					scaleY += (transform.scaleY - 1) * weight;
					pivotX += pivot.x * weight;
					pivotY += pivot.y * weight;

					layerTotalWeight += weight;
				}
			}

			_tween.x = x;
			_tween.y = y;
			_tween.skewX = skewX;
			_tween.skewY = skewY;
			_tween.scaleX = scaleX;
			_tween.scaleY = scaleY;
			_tweenPivot.x = pivotX;
			_tweenPivot.y = pivotY;
		}
	}

	private int sortState(TimelineState state1, TimelineState state2)
	{
		return state1._animationState.getLayer() < state2._animationState.getLayer()?-1:1;
	}

	/**
	 * Unrecommended API. Recommend use slot.childArmature.
	 */
	public Armature getChildArmature()
	{
		if(getSlot() != null)
		{
			return getSlot().getChildArmature();
		}
		return null;
	}

	/**
	 * Unrecommended API. Recommend use slot.display.
	 */
	public Object getDisplay()
	{
		if(getSlot() != null)
		{
			return getSlot().getDisplay();
		}
		return null;
	}
	public void setDisplay(Object value)
	{
		if(getSlot() != null)
		{
			getSlot().setDisplay(value);
		}
	}

	/**
	 * Unrecommended API. Recommend use offset.
	 */
	public DBTransform getNode()
	{
		return _offset;
	}




	/** @private */
	@Override public void setVisible(boolean value)
	{
		if(this._visible != value)
		{
			this._visible = value;
			for (Slot childSlot : _slotList)
			{
				childSlot.updateDisplayVisible(this._visible);
			}
		}
	}


	public Slot getSlot()
	{
		return _slotList.size() > 0? _slotList.get(0) :null;
	}
}
