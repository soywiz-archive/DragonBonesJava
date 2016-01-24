package dragonBones.fast;

import dragonBones.events.EventInfo;
import dragonBones.utils.ArrayListUtils;
import flash.events.Event;
import flash.events.EventDispatcher;

import dragonBones.cache.AnimationCacheManager;
import dragonBones.cache.SlotFrameCache;
import dragonBones.core.IArmature;
import dragonBones.core.ICacheableArmature;
import dragonBones.core.dragonBones_internal;
import dragonBones.events.AnimationEvent;
import dragonBones.events.FrameEvent;
import dragonBones.fast.animation.FastAnimation;
import dragonBones.fast.animation.FastAnimationState;
import dragonBones.objects.ArmatureData;
import dragonBones.objects.DragonBonesData;
import dragonBones.objects.Frame;

import java.util.ArrayList;

//use namespace dragonBones_internal;

/**
 * Dispatched when an animation state play complete (if playtimes equals to 0 means loop forever. Then this Event will not be triggered)
 */
@EventInfo(name="complete", type="dragonBones.events.AnimationEvent")

/**
 * Dispatched when an animation state complete a loop.
 */
@EventInfo(name="loopComplete", type="dragonBones.events.AnimationEvent")

/**
 * Dispatched when an animation state enter a frame with animation frame event.
 */
@EventInfo(name="animationFrameEvent", type="dragonBones.events.FrameEvent")

/**
 * Dispatched when an bone enter a frame with animation frame event.
 */
@EventInfo(name="boneFrameEvent", type="dragonBones.events.FrameEvent")

/**
 * 不支持动态添加Bone和Slot，换装请通过更换Slot的dispaly或子骨架childArmature来实现
 */
public class FastArmature extends EventDispatcher implements ICacheableArmature
{
    /**
     * The name should be same with ArmatureData's name
     */
    public String name;
    /**
     * An object that can contain any user extra data.
     */
    public Object userData;


    private boolean _enableCache;

    /**
     * 保证CacheManager是独占的前提下可以开启，开启后有助于性能提高
     */
    public boolean isCacheManagerExclusive = false;

    /** @private */
    protected FastAnimation _animation;

    /** @private */
    protected Object _display;

    /** @private Store bones based on bones' hierarchy (From root to leaf)*/
    public ArrayList<FastBone> boneList;
    private Object _boneDic;

    /** @private Store slots based on slots' zOrder*/
    public ArrayList<FastSlot> slotList;
    private Object _slotDic;

    public ArrayList<FastSlot> slotHasChildArmatureList;

    protected boolean _enableEventDispatch = true;

    private DragonBonesData __dragonBonesData;
    private ArmatureData _armatureData;
    boolean _slotsZOrderChanged;

    private ArrayList<Object> _eventList;
    private boolean _delayDispose;
    private boolean _lockDispose;
    private boolean useCache = true;
    public FastArmature(Object display)
    {
        super(this);
        _display = display;
        _animation = new FastAnimation(this);
        _slotsZOrderChanged = false;
        _armatureData = null;

        boneList = new ArrayList<>();
        _boneDic = {};
        slotList = new ArrayList<FastSlot>();
        _slotDic = {};
        slotHasChildArmatureList = new ArrayList<>();

        _eventList = [];

        _delayDispose = false;
        _lockDispose = false;

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
        int i = slotList.size();
        while(i -- > 0)
        {
            slotList.get(i).dispose();
        }
        i = boneList.size();
        while(i -- > 0)
        {
            boneList.get(i).dispose();
        }

        slotList.clear();
        boneList.clear();

        _armatureData = null;
        _animation = null;
        slotList = null;
        boneList = null;
        _eventList = null;

    }

    /**
     * Update the animation using this method typically in an ENTERFRAME Event or with a Timer.
     * @param passedTime The amount of second to move the playhead ahead.
     */

    public void advanceTime(double passedTime)
    {
        _lockDispose = true;
        _animation.advanceTime(passedTime);

        FastBone bone;
        FastSlot slot;
        int i;
        if(_animation.animationState.isUseCache())
        {
            if(!useCache)
            {
                useCache = true;
            }
            i = slotList.size();
            while(i -- > 0)
            {
                slot = slotList.get(i);
                slot.updateByCache();
            }
        }
        else
        {
            if(useCache)
            {
                useCache = false;
                i = slotList.size();
                while(i -- > 0)
                {
                    slot = slotList.get(i);
                    slot.switchTransformToBackup();
                }
            }

            i = boneList.size();
            while(i -- > 0)
            {
                bone = boneList.get(i);
                bone.update();
            }

            i = slotList.size();
            while(i -- > 0)
            {
                slot = slotList.get(i);
                slot.update();
            }
        }

        i = slotHasChildArmatureList.size();
        while(i-- > 0)
        {
            slot = slotHasChildArmatureList.get(i);
            IArmature childArmature = (IArmature)slot.childArmature;
            if(childArmature != null)
            {
                childArmature.advanceTime(passedTime);
            }
        }

        if(_slotsZOrderChanged)
        {
            updateSlotsZOrder();
        }

        while(_eventList.size() > 0)
        {
            this.dispatchEvent(ArrayListUtils.shift(_eventList));
        }

        _lockDispose = false;
        if(_delayDispose)
        {
            dispose();
        }
    }

    public AnimationCacheManager enableAnimationCache(int frameRate)
    {
        return enableAnimationCache(frameRate, null, true);
    }

        public AnimationCacheManager enableAnimationCache(int frameRate, ArrayList<?> animationList, boolean loop)
    {
        AnimationCacheManager animationCacheManager = AnimationCacheManager.initWithArmatureData(armatureData,frameRate);
        if(animationList != null)
        {
            for (String animationName : animationList)
            {
                animationCacheManager.initAnimationCache(animationName);
            }
        }
        else
        {
            animationCacheManager.initAllAnimationCache();
        }
        animationCacheManager.setCacheGeneratorArmature(this);
        animationCacheManager.generateAllAnimationCache(loop);

        animationCacheManager.bindCacheUserArmature(this);
        enableCache = true;
        return animationCacheManager;
    }

    public FastBone getBone(String boneName)
    {
        return _boneDic[boneName];
    }
    public FastSlot getSlot(String slotName)
    {
        return _slotDic[slotName];
    }

    /**
     * Gets the Bone associated with this DisplayObject.
     * @param display Instance type of this object varies from flash.display.DisplayObject to startling.display.DisplayObject and subclasses.
     * @return A Bone instance or null if no Bone with that DisplayObject exist..
     * @see dragonBones.Bone
     */
    public FastBone getBoneByDisplay(Object display)
    {
        FastSlot slot = getSlotByDisplay(display);
        return slot != null ?slot.parent:null;
    }

    /**
     * Gets the Slot associated with this DisplayObject.
     * @param displayObj Instance type of this object varies from flash.display.DisplayObject to startling.display.DisplayObject and subclasses.
     * @return A Slot instance or null if no Slot with that DisplayObject exist.
     * @see dragonBones.Slot
     */
    public FastSlot getSlotByDisplay(Object displayObj)
    {
        if(displayObj != null)
        {
            for (FastSlot slot : slotList)
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
     * Get all Slot instance associated with this armature.
     * @param if return Vector copy
     * @return A Vector.&lt;Slot&gt; instance.
     * @see dragonBones.Slot
     */
    public ArrayList<FastSlot> getSlots(boolean returnCopy = true)
    {
        return returnCopy?slotList.concat():slotList;
    }

    private void _updateBonesByCache()
    {
        var i:int = boneList.length;
        var bone:FastBone;
        while(i --)
        {
            bone = boneList[i];
            bone.update();
        }
    }


    /**
     * Add a Bone instance to this Armature instance.
     * @param A Bone instance.
     * @param (optional) The parent's name of this Bone instance.
     * @see dragonBones.Bone
     */
    private void addBone(FastBone bone, String parentName = null)
    {
        FastBone parentBone;
        if(parentName)
        {
            parentBone = getBone(parentName);
            parentBone.boneList.push(bone);
        }
        bone.armature = this;
        bone.setParent(parentBone);
        boneList.unshift(bone);
        _boneDic[bone.name] = bone;
    }

    /**
     * Add a slot to a bone as child.
     * @param slot A Slot instance
     * @param boneName bone name
     * @see dragonBones.core.DBObject
     */
    private void addSlot(FastSlot slot, String parentBoneName)
    {
        FastBone bone = getBone(parentBoneName);
        if(bone)
        {
            slot.armature = this;
            slot.setParent(bone);
            bone.slotList.push(slot);
            slot.addDisplayToContainer(display);
            slotList.push(slot);
            _slotDic[slot.name] = slot;
            if(slot.hasChildArmature)
            {
                slotHasChildArmatureList.push(slot);
            }

        }
        else
        {
            throw new ArgumentError();
        }
    }

    /**
     * Sort all slots based on zOrder
     */
    private void updateSlotsZOrder()
    {
        slotList.fixed = false;
        slotList.sort(sortSlot);
        slotList.fixed = true;
        int i = slotList.length;
        while(i -- > 0)
        {
            FastSlot slot = slotList[i];
            if ((slot._frameCache && ((SlotFrameCache)slot._frameCache).displayIndex >= 0)
                || (!slot._frameCache && slot.displayIndex >= 0))
            {
                slot.addDisplayToContainer(_display);
            }
        }

        _slotsZOrderChanged = false;
    }

    private void sortBoneList()
    {
        int i = boneList.length;
        if(i == 0)
        {
            return;
        }
        ArrayList<?> helpArray = [];
        while(i --)
        {
            int level = 0;
            FastBone bone = boneList[i];
            FastBone boneParent = bone;
            while(boneParent)
            {
                level ++;
                boneParent = boneParent.parent;
            }
            helpArray[i] = [level, bone];
        }

        helpArray.sortOn("0", Array.NUMERIC|Array.DESCENDING);

        i = helpArray.length;

        boneList.fixed = false;
        while(i --)
        {
            boneList[i] = helpArray[i][1];
        }
        boneList.fixed = true;

        helpArray.length = 0;
    }





    /** @private When AnimationState enter a key frame, call this func*/
    public void arriveAtFrame(Frame frame, FastAnimationState animationState)
    {
        if(frame.event && this.hasEventListener(FrameEvent.ANIMATION_FRAME_EVENT))
        {
            var frameEvent:FrameEvent = new FrameEvent(FrameEvent.ANIMATION_FRAME_EVENT);
            frameEvent.animationState = animationState;
            frameEvent.frameLabel = frame.event;
            addEvent(frameEvent);
        }

        if(frame.action)
        {
            animation.gotoAndPlay(frame.action);
        }
    }

    /**
     * Force update bones and slots. (When bone's animation play complete, it will not update)
     */
    public void invalidUpdate(String boneName = null)
    {
        if(boneName)
        {
            var bone:FastBone = getBone(boneName);
            if(bone)
            {
                bone.invalidUpdate();
            }
        }
        else
        {
            var i:int = boneList.length;
            while(i --)
            {
                boneList[i].invalidUpdate();
            }
        }
    }

    public void resetAnimation()
    {
        animation.animationState.resetTimelineStateList();
        for (FastBone boneItem : boneList)
        {
            boneItem._timelineState = null;
        }
        animation.stop();
    }

    private int sortSlot(FastSlot slot1, FastSlot slot2)
    {
        return slot1.zOrder < slot2.zOrder?1: -1;
    }

    public Object getAnimation()
    {
        return _animation;
    }

    /**
     * ArmatureData.
     * @see dragonBones.objects.ArmatureData.
     */
    public ArmatureData getArmatureData()
    {
        return _armatureData;
    }

    /**
     * An Animation instance
     * @see dragonBones.animation.Animation
     */
    public FastAnimation getAnimation()
    {
        return _animation;
    }

    /**
     * Armature's display object. It's instance type depends on render engine. For example "flash.display.DisplayObject" or "startling.display.DisplayObject"
     */
    public Object getDisplay()
    {
        return _display;
    }

    public boolean getEnableCache()
    {
        return _enableCache;
    }
    public void setEnableCache(boolean value)
    {
        _enableCache = value;
    }

    public boolean getEnableEventDispatch()
    {
        return _enableEventDispatch;
    }
    public void setEnableEventDispatch(boolean value)
    {
        _enableEventDispatch = value;
    }

    public Object getSlotDic()
    {
        return _slotDic;
    }

    public void addEvent(Event event)
    {
        if (_enableEventDispatch)
        {
            _eventList.add(event);
        }
    }
}
