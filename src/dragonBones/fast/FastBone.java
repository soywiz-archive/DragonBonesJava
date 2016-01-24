package dragonBones.fast;

import dragonBones.animation.TimelineState;
import dragonBones.events.FrameEvent;
import dragonBones.fast.animation.FastAnimationState;
import dragonBones.fast.animation.FastBoneTimelineState;
import dragonBones.objects.BoneData;
import dragonBones.objects.DBTransform;
import dragonBones.objects.Frame;
import flash.geom.Point;

import java.util.ArrayList;

/**
 * 不保存子骨骼列表和子插槽列表
 * 不能动态添加子骨骼和子插槽
 */
public class FastBone extends FastDBObject
{
	public static FastBone initWithBoneData(BoneData boneData)
	{
		FastBone outputBone = new FastBone();

		outputBone.setName(boneData.name);
		outputBone.inheritRotation = boneData.inheritRotation;
		outputBone.inheritScale = boneData.inheritScale;
		outputBone.getOrigin().copy(boneData.transform);

		return outputBone;
	}

	public ArrayList<FastSlot> slotList = new ArrayList<>();
	public ArrayList<FastBone> boneList = new ArrayList<>();
	/** @private */
	FastBoneTimelineState _timelineState;

	/** @private */
	int _needUpdate;
	/** @private */
	Point _tweenPivot;

	public FastBone()
	{
		super();
		_needUpdate = 2;
		_tweenPivot = new Point();
	}

	/**
	 * Get all Bone instance associated with this bone.
	 * @return A Vector.&lt;Slot&gt; instance.
	 * @see dragonBones.Slot
	 */
	public ArrayList<FastBone> getBones(boolean returnCopy = true)
	{
		return returnCopy? (ArrayList<FastBone>) boneList.clone() :boneList;
	}

	/**
	 * Get all Slot instance associated with this bone.
	 * @return A Vector.&lt;Slot&gt; instance.
	 * @see dragonBones.Slot
	 */
	public ArrayList<FastSlot> getSlots(boolean returnCopy = true)
	{
		return returnCopy? (ArrayList<FastSlot>) slotList.clone() :slotList;
	}

	/**
	 * @inheritDoc
	 */
	@Override public void dispose()
	{
		super.dispose();
		_timelineState = null;
		_tweenPivot = null;
	}

//动画
	/**
	 * Force update the bone in next frame even if the bone is not moving.
	 */
	public void invalidUpdate()
	{
		_needUpdate = 2;
	}

	@Override
	protected void calculateRelativeParentTransform()
	{
		_global.copy(this._origin);
		if(_timelineState != null)
		{
			_global.add(_timelineState._transform);
		}
	}

	/** @private */
	@Override void updateByCache()
	{
		super.updateByCache();
		_global = _frameCache.globalTransform;
		_globalTransformMatrix = _frameCache.globalTransformMatrix;
	}

	/** @private */
	void update(boolean needUpdate = false)
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
		updateGlobal();
	}

	/** @private */
	void hideSlots()
	{
		for (FastSlot childSlot : slotList)
		{
			childSlot.hideSlots();
		}
	}

	/** @private When bone timeline enter a key frame, call this func*/
	void arriveAtFrame(Frame frame, FastAnimationState animationState)
	{
		FastSlot childSlot;
		if(frame.event != null && this.armature.hasEventListener(FrameEvent.BONE_FRAME_EVENT))
		{
			FrameEvent frameEvent = new FrameEvent(FrameEvent.BONE_FRAME_EVENT);
			frameEvent.bone = this;
			frameEvent.animationState = animationState;
			frameEvent.frameLabel = frame.event;
			this.armature.addEvent(frameEvent);
		}
	}

	private void blendingTimeline()
	{
		if(_timelineState != null)
		{
			_tweenPivot.x = _timelineState._pivot.x;
			_tweenPivot.y = _timelineState._pivot.y;
		}
	}

	/**
	 * Unrecommended API. Recommend use slot.childArmature.
	 */
	public Object getChildArmature()
	{
		FastSlot s = slot;
		if(s != null)
		{
			return s.childArmature;
		}
		return null;
	}

	/**
	 * Unrecommended API. Recommend use slot.display.
	 */
	public Object getDisplay()
	{
		FastSlot s = slot;
		if(s != null)
		{
			return s.getDisplay();
		}
		return null;
	}
	public void setDisplay(Object value)
	{
		FastSlot s = slot;
		if(s != null)
		{
			s.setDisplay(value);
		}
	}

	/** @private */
	@Override public void setVisible(boolean value)
	{
		if(this._visible != value)
		{
			this._visible = value;
			for (FastSlot childSlot : armature.slotList)
			{
				if(childSlot.getParent() == this)
				{
					childSlot.updateDisplayVisible(this._visible);
				}
			}
		}
	}

	public FastSlot getSlot()
	{
		return slotList.size() > 0? slotList.get(0) :null;
	}
}
