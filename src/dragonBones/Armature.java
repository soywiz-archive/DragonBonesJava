package dragonBones;
import dragonBones.events.*;
import flash.events.Event;
import flash.events.EventDispatcher;

import dragonBones.animation.Animation;
import dragonBones.animation.AnimationState;
import dragonBones.animation.TimelineState;
import dragonBones.core.IArmature;
import dragonBones.core.dragonBones_internal;
import dragonBones.objects.ArmatureData;
import dragonBones.objects.DragonBonesData;
import dragonBones.objects.Frame;
import dragonBones.objects.SkinData;
import dragonBones.objects.SlotData;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Vector;

//use namespace dragonBones_internal;

/**
 * Dispatched when slot's zOrder changed
 */
@EventInfo(name="zOrderUpdated", type="dragonBones.events.ArmatureEvent")

/**
 * Dispatched when an animation state begins fade in (Even if fade in time is 0)
 */
@EventInfo(name="fadeIn", type="dragonBones.events.AnimationEvent")

/**
 * Dispatched when an animation state begins fade out (Even if fade out time is 0)
 */
@EventInfo(name="fadeOut", type="dragonBones.events.AnimationEvent")

/**
 * Dispatched when an animation state start to play(AnimationState may play when fade in start or end. It is controllable).
 */
@EventInfo(name="start", type="dragonBones.events.AnimationEvent")

/**
 * Dispatched when an animation state play complete (if playtimes equals to 0 means loop forever. Then this Event will not be triggered)
 */
@EventInfo(name="complete", type="dragonBones.events.AnimationEvent")

/**
 * Dispatched when an animation state complete a loop.
 */
@EventInfo(name="loopComplete", type="dragonBones.events.AnimationEvent")

/**
 * Dispatched when an animation state fade in complete.
 */
@EventInfo(name="fadeInComplete", type="dragonBones.events.AnimationEvent")

/**
 * Dispatched when an animation state fade out complete.
 */
@EventInfo(name="fadeOutComplete", type="dragonBones.events.AnimationEvent")

/**
 * Dispatched when an animation state enter a frame with animation frame event.
 */
@EventInfo(name="animationFrameEvent", type="dragonBones.events.FrameEvent")

/**
 * Dispatched when an bone enter a frame with animation frame event.
 */
@EventInfo(name="boneFrameEvent", type="dragonBones.events.FrameEvent")

public class Armature extends EventDispatcher implements IArmature
{
	private DragonBonesData __dragonBonesData;


	/**
	 * The instance dispatch sound event.
	 */
	private static final SoundEventManager _soundManager = SoundEventManager.getInstance();

	/**
	 * The name should be same with ArmatureData's name
	 */
	public String name;

	/**
	 * An object that can contain any user extra data.
	 */
	public Object userData;

	/** @private Set it to true when slot's zorder changed*/
	boolean _slotsZOrderChanged;

	/** @private Store event needed to dispatch in current frame. When advanceTime execute complete, dispath them.*/
	private ArrayList<Event> _eventList;


	/** @private Store slots based on slots' zOrder*/
	protected ArrayList<Slot> _slotList;

	/** @private Store bones based on bones' hierarchy (From root to leaf)*/
	protected ArrayList<Bone> _boneList;

	private boolean _delayDispose;
	private boolean _lockDispose;

	/** @private */
	private ArmatureData _armatureData;
	/**
	 * ArmatureData.
	 * @see dragonBones.objects.ArmatureData.
	 */
	public ArmatureData getArmatureData()
	{
		return _armatureData;
	}

	/** @private */
	protected Object _display;
	/**
	 * Armature's display object. It's instance type depends on render engine. For example "flash.display.DisplayObject" or "startling.display.DisplayObject"
	 */
	public Object getDisplay()
	{
		return _display;
	}

	/** @private */
	protected Animation _animation;
	/**
	 * An Animation instance
	 * @see dragonBones.animation.Animation
	 */
	public Animation getAnimation()
	{
		return _animation;
	}

	/**
	 * save more skinLists
	 */
	private Object _skinLists;
	/**
	 * Creates a Armature blank instance.
	 * @param Instance type of this object varies from flash.display.DisplayObject to startling.display.DisplayObject and subclasses.
	 * @see #display
	 */
	public Armature(Object display)
	{
		super(this);
		_display = display;

		_animation = new Animation(this);

		_slotsZOrderChanged = false;

		_slotList = new Vector.<Slot>;
		_slotList.fixed = true;
		_boneList = new Vector.<Bone>;
		_boneList.fixed = true;
		_eventList = new Vector.<Event>;
		_skinLists = { };
		_delayDispose = false;
		_lockDispose = false;

		_armatureData = null;
	}

	/**
	 * Cleans up any resources used by this instance.
	 */
	public void dispose()
	{
		_delayDispose = true;
		if(!_animation || _lockDispose)
		{
			return;
		}

		userData = null;

		_animation.dispose();
		int i = _slotList.size();
		while(i -- > 0)
		{
			_slotList.get(i).dispose();
		}
		i = _boneList.size();
		while(i -- > 0)
		{
			_boneList.get(i).dispose();
		}

		_slotList.fixed = false;
		_slotList.length = 0;
		_boneList.fixed = false;
		_boneList.length = 0;
		_eventList.length = 0;

		_armatureData = null;
		_animation = null;
		_slotList = null;
		_boneList = null;
		_eventList = null;

		//_display = null;
	}

	/**
	 * Force update bones and slots. (When bone's animation play complete, it will not update)
	 */
	public void invalidUpdate(String boneName = null)
	{
		if(boneName != null)
		{
			Bone bone = getBone(boneName);
			if(bone)
			{
				bone.invalidUpdate();
			}
		}
		else
		{
			int i = _boneList.size();
			while(i -- > 0)
			{
				_boneList.get(i).invalidUpdate();
			}
		}
	}

	/**
	 * Update the animation using this method typically in an ENTERFRAME Event or with a Timer.
	 * @param The amount of second to move the playhead ahead.
	 */
	public void advanceTime(double passedTime)
	{
		_lockDispose = true;

		_animation.advanceTime(passedTime);

		passedTime *= _animation.timeScale;    //_animation's time scale will impact childArmature

		boolean isFading = _animation._isFading;
		int i = _boneList.size();
		while(i -- > 0)
		{
			Bone bone = _boneList[i];
			bone.update(isFading);
		}

		i = _slotList.size();
		while(i -- > 0)
		{
			Slot slot = _slotList[i];
			slot.update();
			if(slot._isShowDisplay)
			{
				Armature childArmature = slot.childArmature;
				if(childArmature)
				{
					childArmature.advanceTime(passedTime);
				}
			}
		}

		if(_slotsZOrderChanged)
		{
			updateSlotsZOrder();

			if(this.hasEventListener(ArmatureEvent.Z_ORDER_UPDATED))
			{
				this.dispatchEvent(new ArmatureEvent(ArmatureEvent.Z_ORDER_UPDATED));
			}
		}

		if(_eventList.size() > 0)
		{
			for (Event event : _eventList)
			{
				this.dispatchEvent(event);
			}
			_eventList.clear();
		}

		_lockDispose = false;
		if(_delayDispose)
		{
			dispose();
		}
	}

	public void resetAnimation()
	{
		animation.stop();
		animation.resetAnimationStateList();

		for (Bone boneItem : _boneList)
		{
			boneItem.removeAllStates();
		}
	}

	/**
	 * Get all Slot instance associated with this armature.
	 * @param if return Vector copy
	 * @return A Vector.&lt;Slot&gt; instance.
	 * @see dragonBones.Slot
	 */
	public ArrayList<Slot> getSlots(boolean returnCopy = true)
	{
		return returnCopy?_slotList.concat():_slotList;
	}

	/**
	 * Retrieves a Slot by name
	 * @param The name of the Bone to retrieve.
	 * @return A Slot instance or null if no Slot with that name exist.
	 * @see dragonBones.Slot
	 */
	public Slot getSlot(String slotName)
	{
		for (Slot slot : _slotList)
		{
			if(Objects.equals(slot.name, slotName))
			{
				return slot;
			}
		}
		return null;
	}

	/**
	 * Gets the Slot associated with this DisplayObject.
	 * @param Instance type of this object varies from flash.display.DisplayObject to startling.display.DisplayObject and subclasses.
	 * @return A Slot instance or null if no Slot with that DisplayObject exist.
	 * @see dragonBones.Slot
	 */
	public Slot getSlotByDisplay(Object displayObj)
	{
		if(displayObj != null)
		{
			for (Slot slot : _slotList)
			{
				if(slot.display == displayObj)
				{
					return slot;
				}
			}
		}
		return null;
	}

	/**
	 * Add a slot to a bone as child.
	 * @param slot A Slot instance
	 * @param boneName bone name
	 * @see dragonBones.core.DBObject
	 */
	public void addSlot(Slot slot, String boneName)
	{
		Bone bone = getBone(boneName);
		if (bone != null)
		{
			bone.addSlot(slot);
		}
		else
		{
			throw new ArgumentError();
		}
	}

	/**
	 * Remove a Slot instance from this Armature instance.
	 * @param The Slot instance to remove.
	 * @see dragonBones.Slot
	 */
	public void removeSlot(Slot slot)
	{
		if(slot == null || slot.armature != this)
		{
			throw new ArgumentError();
		}

		slot.parent.removeSlot(slot);
	}

	/**
	 * Remove a Slot instance from this Armature instance.
	 * @param The name of the Slot instance to remove.
	 * @see dragonBones.Slot
	 */
	public Slot removeSlotByName(String slotName)
	{
		Slot slot = getSlot(slotName);
		if(slot != null)
		{
			removeSlot(slot);
		}
		return slot;
	}

	/**
	 * Get all Bone instance associated with this armature.
	 * @param if return Vector copy
	 * @return A Vector.&lt;Bone&gt; instance.
	 * @see dragonBones.Bone
	 */
	public ArrayList<Bone> getBones(boolean returnCopy = true)
	{
		return returnCopy?_boneList.concat():_boneList;
	}

	/**
	 * Retrieves a Bone by name
	 * @param The name of the Bone to retrieve.
	 * @return A Bone instance or null if no Bone with that name exist.
	 * @see dragonBones.Bone
	 */
	public Bone getBone(String boneName)
	{
		for (Bone bone : _boneList)
		{
			if(Objects.equals(bone.name, boneName))
			{
				return bone;
			}
		}
		return null;
	}

	/**
	 * Gets the Bone associated with this DisplayObject.
	 * @param Instance type of this object varies from flash.display.DisplayObject to startling.display.DisplayObject and subclasses.
	 * @return A Bone instance or null if no Bone with that DisplayObject exist..
	 * @see dragonBones.Bone
	 */
	public Bone getBoneByDisplay(Object display)
	{
		Slot slot = getSlotByDisplay(display);
		return (slot != null) ?slot.getParent():null;
	}

	/**
	 * Add a Bone instance to this Armature instance.
	 * @param A Bone instance.
	 * @param (optional) The parent's name of this Bone instance.
	 * @see dragonBones.Bone
	 */
	public void addBone(Bone bone, String parentName = null, boolean updateLater = false)
	{
		Bone parentBone = null;
		if(parentName != null)
		{
			parentBone = getBone(parentName);
			if (parentBone == null)
			{
				throw new ArgumentError();
			}
		}

		if(parentBone != null)
		{
			parentBone.addChildBone(bone, updateLater);
		}
		else
		{
			if(bone.parent)
			{
				bone.parent.removeChildBone(bone, updateLater);
			}
			bone.setArmature(this);
			if(!updateLater)
			{
				updateAnimationAfterBoneListChanged();
			}
		}
	}

	/**
	 * Remove a Bone instance from this Armature instance.
	 * @param The Bone instance to remove.
	 * @see	dragonBones.Bone
	 */
	public void removeBone(Bone bone, boolean updateLater = false)
	{
		if(bone == null || bone.armature != this)
		{
			throw new ArgumentError();
		}

		if(bone.getParent() != null)
		{
			bone.parent.removeChildBone(bone, updateLater);
		}
		else
		{
			bone.setArmature(null);
			if(!updateLater)
			{
				updateAnimationAfterBoneListChanged(false);
			}
		}
	}

	/**
	 * Remove a Bone instance from this Armature instance.
	 * @param The name of the Bone instance to remove.
	 * @see dragonBones.Bone
	 */
	public Bone removeBoneByName(String boneName)
	{
		Bone bone = getBone(boneName);
		if(bone)
		{
			removeBone(bone);
		}
		return bone;
	}

	/** @private */
	private void addBoneToBoneList(Bone bone)
	{
		if(_boneList.indexOf(bone) < 0)
		{
			_boneList.fixed = false;
			_boneList[_boneList.length] = bone;
			_boneList.fixed = true;
		}
	}

	/** @private */
	private void removeBoneFromBoneList(Bone bone)
	{
		int index = _boneList.indexOf(bone);
		if(index >= 0)
		{
			_boneList.fixed = false;
			_boneList.splice(index, 1);
			_boneList.fixed = true;
		}
	}

	/** @private */
	private void addSlotToSlotList(Slot slot)
	{
		if(_slotList.indexOf(slot) < 0)
		{
			_slotList.fixed = false;
			_slotList[_slotList.length] = slot;
			_slotList.fixed = true;
		}
	}

	/** @private */
	private void removeSlotFromSlotList(Slot slot)
	{
		int index = _slotList.indexOf(slot);
		if(index >= 0)
		{
			_slotList.fixed = false;
			_slotList.splice(index, 1);
			_slotList.fixed = true;
		}
	}

	/**
	 * Sort all slots based on zOrder
	 */
	public void updateSlotsZOrder()
	{
		_slotList.fixed = false;
		_slotList.sort(sortSlot);
		_slotList.fixed = true;
		int i = _slotList.length;
		while(i -- > 0)
		{
			Slot slot = _slotList[i];
			if(slot._isShowDisplay)
			{
				//_display 实际上是container, 这个方法就是把原来的显示对象放到container中的第一个
				slot.addDisplayToContainer(_display);
			}
		}

		_slotsZOrderChanged = false;
	}

	private void updateAnimationAfterBoneListChanged(boolean ifNeedSortBoneList = true)
	{
		if(ifNeedSortBoneList)
		{
			sortBoneList();
		}
		_animation.updateAnimationStates();
	}

	private void sortBoneList()
	{
		int i = _boneList.length;
		if(i == 0)
		{
			return;
		}
		Array helpArray = [];
		while(i -- > 0)
		{
			int level = 0;
			Bone bone = _boneList[i];
			Bone boneParent = bone;
			while(boneParent != null)
			{
				level ++;
				boneParent = boneParent.parent;
			}
			helpArray[i] = [level, bone];
		}

		helpArray.sortOn("0", Array.NUMERIC|Array.DESCENDING);

		i = helpArray.length;

		_boneList.fixed = false;
		while(i -- > 0)
		{
			_boneList[i] = helpArray[i][1];
		}
		_boneList.fixed = true;

		helpArray.length = 0;
	}

	/** @private When AnimationState enter a key frame, call this func*/
	private void arriveAtFrame(Frame frame, TimelineState timelineState, AnimationState animationState, boolean isCross)
	{
		if(frame.event && this.hasEventListener(FrameEvent.ANIMATION_FRAME_EVENT))
		{
			FrameEvent frameEvent = new FrameEvent(FrameEvent.ANIMATION_FRAME_EVENT);
			frameEvent.animationState = animationState;
			frameEvent.frameLabel = frame.event;
			_eventList.push(frameEvent);
		}

		if(frame.sound && _soundManager.hasEventListener(SoundEvent.SOUND))
		{
			SoundEvent soundEvent = new SoundEvent(SoundEvent.SOUND);
			soundEvent.armature = this;
			soundEvent.animationState = animationState;
			soundEvent.sound = frame.sound;
			_soundManager.dispatchEvent(soundEvent);
		}

		//[TODO]currently there is only gotoAndPlay belongs to frame action. In future, there will be more.
		//后续会扩展更多的action，目前只有gotoAndPlay的含义
		if(frame.action != null)
		{
			if(animationState.displayControl)
			{
				animation.gotoAndPlay(frame.action);
			}
		}
	}

	private int sortSlot(Slot slot1, Slot slot2)
	{
		return slot1.zOrder < slot2.zOrder?1: -1;
	}

	public void addSkinList(String skinName, Object list)
	{
		if (skinName == null)
		{
			skinName = "default";
		}
		if (_skinLists[skinName] != null)
		{
			_skinLists[skinName] = list;
		}
	}

	public void changeSkin(String skinName)
	{
		SkinData skinData = armatureData.getSkinData(skinName);
		if(!skinData || !_skinLists[skinName])
		{
			return;
		}
		armatureData.setSkinData(skinName);
		Array displayList = [];
		Vector.<SlotData> slotDataList = armatureData.slotDataList;
		SlotData slotData;
		Slot slot;
		Bone bone;
		for(int i = 0; i < slotDataList.length; i++)
		{

			slotData = slotDataList[i];
			displayList = _skinLists[skinName][slotData.name];
			bone = getBone(slotData.parent);
			if(!bone || !displayList)
			{
				continue;
			}

			slot = getSlot(slotData.name);
			slot.initWithSlotData(slotData);

			slot.displayList = displayList;
			slot.changeDisplay(0);
		}
	}

	public Object getAnimation()
	{
		return _animation;
	}
}
