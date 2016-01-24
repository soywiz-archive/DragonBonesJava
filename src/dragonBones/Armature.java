package dragonBones;
import dragonBones.events.*;
import flash.errors.ArgumentError;
import flash.events.Event;
import flash.events.EventDispatcher;

import dragonBones.animation.Animation;
import dragonBones.animation.AnimationState;
import dragonBones.animation.TimelineState;
import dragonBones.core.IArmature;
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
	public ArrayList<Event> _eventList;


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
	 * @see dragonBones.objects.ArmatureData
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
	@Override
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
	 * @param display type of this object varies from flash.display.DisplayObject to startling.display.DisplayObject and subclasses.
	 */
	public Armature(Object display)
	{
		super(this);
		_display = display;

		_animation = new Animation(this);

		_slotsZOrderChanged = false;

		_slotList = new ArrayList<Slot>();
		_boneList = new ArrayList<Bone>();
		_eventList = new ArrayList<Event>();
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
		if(_animation == null || _lockDispose)
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

		_slotList.clear();
		_boneList.clear();
		_eventList.clear();

		_armatureData = null;
		_animation = null;
		_slotList = null;
		_boneList = null;
		_eventList = null;

		//_display = null;
	}

	public void invalidUpdate()
	{
		invalidUpdate(null);
	}

	/**
	 * Force update bones and slots. (When bone's animation play complete, it will not update)
	 */
	public void invalidUpdate(String boneName)
	{
		if(boneName != null)
		{
			Bone bone = getBone(boneName);
			if(bone != null)
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
	 * @param passedTime The amount of second to move the playhead ahead.
	 */
	public void advanceTime(double passedTime)
	{
		_lockDispose = true;

		_animation.advanceTime(passedTime);

		passedTime *= _animation.getTimeScale();    //_animation's time scale will impact childArmature

		boolean isFading = _animation._isFading;
		int i = _boneList.size();
		while(i -- > 0)
		{
			Bone bone = _boneList.get(i);
			bone.update(isFading);
		}

		i = _slotList.size();
		while(i -- > 0)
		{
			Slot slot = _slotList.get(i);
			slot.update();
			if(slot._isShowDisplay)
			{
				Armature childArmature = slot.getChildArmature();
				if(childArmature != null)
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
		getAnimation().stop();
		getAnimation().resetAnimationStateList();

		for (Bone boneItem : _boneList)
		{
			boneItem.removeAllStates();
		}
	}

	public ArrayList<Slot> getSlots()
	{
		return getSlots(true);
	}

	/**
	 * Get all Slot instance associated with this armature.
	 * @param returnCopy if return Vector copy
	 * @return A Vector.&lt;Slot&gt; instance.
	 * @see dragonBones.Slot
	 */
	public ArrayList<Slot> getSlots(boolean returnCopy)
	{
		return returnCopy? (ArrayList<Slot>) _slotList.clone() :_slotList;
	}

	/**
	 * Retrieves a Slot by name
	 * @param slotName The name of the Bone to retrieve.
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
	 * @param displayObj Instance type of this object varies from flash.display.DisplayObject to startling.display.DisplayObject and subclasses.
	 * @return A Slot instance or null if no Slot with that DisplayObject exist.
	 * @see dragonBones.Slot
	 */
	public Slot getSlotByDisplay(Object displayObj)
	{
		if(displayObj != null)
		{
			for (Slot slot : _slotList)
			{
				if(slot.getDisplay() == displayObj)
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
	 * @param slot The Slot instance to remove.
	 * @see dragonBones.Slot
	 */
	public void removeSlot(Slot slot)
	{
		if(slot == null || slot.getArmature() != this)
		{
			throw new ArgumentError();
		}

		slot.getParent().removeSlot(slot);
	}

	/**
	 * Remove a Slot instance from this Armature instance.
	 * @param slotName The name of the Slot instance to remove.
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

	public ArrayList<Bone> getBones()
	{
		return getBones(true);
	}

	/**
	 * Get all Bone instance associated with this armature.
	 * @param returnCopy if return Vector copy
	 * @return A Vector.&lt;Bone&gt; instance.
	 * @see dragonBones.Bone
	 */
	public ArrayList<Bone> getBones(boolean returnCopy)
	{
		return returnCopy? (ArrayList<Bone>) _boneList.clone() :_boneList;
	}

	/**
	 * Retrieves a Bone by name
	 * @param boneName The name of the Bone to retrieve.
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
	 * @param display Instance type of this object varies from flash.display.DisplayObject to startling.display.DisplayObject and subclasses.
	 * @return A Bone instance or null if no Bone with that DisplayObject exist..
	 * @see dragonBones.Bone
	 */
	public Bone getBoneByDisplay(Object display)
	{
		Slot slot = getSlotByDisplay(display);
		return (slot != null) ?slot.getParent():null;
	}

	public void addBone(Bone bone)
	{
		addBone(bone, null, false);
	}

		/**
		 * Add a Bone instance to this Armature instance.
		 * @see dragonBones.Bone
		 */
	public void addBone(Bone bone, String parentName, boolean updateLater)
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
			if(bone.getParent() != null)
			{
				bone.getParent().removeChildBone(bone, updateLater);
			}
			bone.setArmature(this);
			if(!updateLater)
			{
				updateAnimationAfterBoneListChanged();
			}
		}
	}

	public void removeBone(Bone bone)
	{
		removeBone(bone, false);
	}

	/**
	 * Remove a Bone instance from this Armature instance.
	 * @param bone The Bone instance to remove.
	 * @see	dragonBones.Bone
	 */
	public void removeBone(Bone bone, boolean updateLater)
	{
		if(bone == null || bone.getArmature() != this)
		{
			throw new ArgumentError();
		}

		if(bone.getParent() != null)
		{
			bone.getParent().removeChildBone(bone, updateLater);
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
	 * @param boneName The name of the Bone instance to remove.
	 * @see dragonBones.Bone
	 */
	public Bone removeBoneByName(String boneName)
	{
		Bone bone = getBone(boneName);
		if(bone != null)
		{
			removeBone(bone);
		}
		return bone;
	}

	/** @private */
	void addBoneToBoneList(Bone bone)
	{
		if(_boneList.indexOf(bone) < 0)
		{
			_boneList.set(_boneList.size(), bone);
		}
	}

	/** @private */
	void removeBoneFromBoneList(Bone bone)
	{
		_boneList.remove(bone);
	}

	/** @private */
	private void addSlotToSlotList(Slot slot)
	{
		if(_slotList.indexOf(slot) < 0)
		{
			_slotList.set(_slotList.size(), slot);
		}
	}

	/** @private */
	private void removeSlotFromSlotList(Slot slot)
	{
		_slotList.remove(slot);
	}

	/**
	 * Sort all slots based on zOrder
	 */
	public void updateSlotsZOrder()
	{
		_slotList.sort(sortSlot);
		int i = _slotList.size();
		while(i -- > 0)
		{
			Slot slot = _slotList.get(i);
			if(slot._isShowDisplay)
			{
				//_display 实际上是container, 这个方法就是把原来的显示对象放到container中的第一个
				slot.addDisplayToContainer(_display);
			}
		}

		_slotsZOrderChanged = false;
	}

	void updateAnimationAfterBoneListChanged()
	{
		updateAnimationAfterBoneListChanged(true);
	}

	void updateAnimationAfterBoneListChanged(boolean ifNeedSortBoneList)
	{
		if(ifNeedSortBoneList)
		{
			sortBoneList();
		}
		_animation.updateAnimationStates();
	}

	private void sortBoneList()
	{
		int i = _boneList.size();
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

		i = helpArray.size();

		while(i -- > 0)
		{
			_boneList[i] = helpArray[i][1];
		}

		helpArray.clear();
	}

	/** @private When AnimationState enter a key frame, call this func*/
	public void arriveAtFrame(Frame frame, TimelineState timelineState, AnimationState animationState, boolean isCross)
	{
		if(frame.event != null && this.hasEventListener(FrameEvent.ANIMATION_FRAME_EVENT))
		{
			FrameEvent frameEvent = new FrameEvent(FrameEvent.ANIMATION_FRAME_EVENT);
			frameEvent.animationState = animationState;
			frameEvent.frameLabel = frame.event;
			_eventList.add(frameEvent);
		}

		if(frame.sound != null && _soundManager.hasEventListener(SoundEvent.SOUND))
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
		return slot1.getZOrder() < slot2.getZOrder()?1: -1;
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
		SkinData skinData = getArmatureData().getSkinData(skinName);
		if(!skinData || !_skinLists[skinName])
		{
			return;
		}
		getArmatureData().setSkinData(skinName);
		ArrayList<Object> displayList = [];
		ArrayList<SlotData> slotDataList = getArmatureData().slotDataList;
		SlotData slotData;
		Slot slot;
		Bone bone;
		for(int i = 0; i < slotDataList.size(); i++)
		{

			slotData = slotDataList[i];
			displayList = _skinLists[skinName][slotData.name];
			bone = getBone(slotData.parent);
			if(bone == null || displayList == null)
			{
				continue;
			}

			slot = getSlot(slotData.name);
			slot.initWithSlotData(slotData);

			slot.setDisplayList(displayList);
			slot.changeDisplay(0);
		}
	}

	public Object getAnimation()
	{
		return _animation;
	}
}
