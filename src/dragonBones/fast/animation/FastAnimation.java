package dragonBones.fast.animation;

import dragonBones.animation.BaseAnimation;
import dragonBones.cache.AnimationCacheManager;
import dragonBones.core.IAnimationState;
import dragonBones.core.IArmature;
import dragonBones.fast.FastArmature;
import dragonBones.fast.FastSlot;
import dragonBones.objects.AnimationData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//use namespace dragonBones_internal;

/**
 * 不支持动画融合，在开启缓存的情况下，不支持无极的平滑补间
 */
public class FastAnimation extends BaseAnimation
{
	public ArrayList<String> animationList;
	public FastAnimationState animationState;

	private FastArmature _armature;
	private ArrayList<AnimationData> _animationDataList;
	private Map<String, AnimationData> _animationDataObj;
	private boolean _isPlaying;
	private double _timeScale;

	public FastAnimation(FastArmature armature)
	{
		_armature = armature;
		animationState = new FastAnimationState();
		ianimationState = animationState;
		animationState._armature = armature;
		animationList = new ArrayList<String>();
		_animationDataObj = new HashMap<>();

		_isPlaying = false;
		_timeScale = 1;
	}

	/**
	 * Qualifies all resources used by this Animation instance for garbage collection.
	 */
	public void dispose()
	{
		if(_armature == null)
		{
			return;
		}

		_armature = null;
		_animationDataList = null;
		animationList = null;
		animationState = null;
	}

	public IAnimationState gotoAndPlay(String animationName, double fadeInTime, double duration, double playTimes)
	{
		if (_animationDataList == null)
		{
			return null;
		}
		AnimationData animationData = _animationDataObj.get(animationName);
		if (animationData == null)
		{
			return null;
		}
		_isPlaying = true;
		fadeInTime = fadeInTime < 0?(animationData.fadeTime < 0?0.3:animationData.fadeTime):fadeInTime;
		double durationScale;
		if(duration < 0)
		{
			durationScale = animationData.scale < 0?1:animationData.scale;
		}
		else
		{
			durationScale = duration * 1000 / animationData.duration;
		}
		playTimes = Double.isNaN(playTimes)?animationData.playTimes:playTimes;

		//播放新动画

		animationState.fadeIn(animationData, playTimes, 1 / durationScale, fadeInTime);

		if(_armature.getEnableCache() && animationCacheManager != null)
		{
			animationState.animationCache = animationCacheManager.getAnimationCache(animationName);
		}

		int i = _armature.slotHasChildArmatureList.size();
		while(i-- > 0)
		{
			FastSlot slot = _armature.slotHasChildArmatureList.get(i);
			IArmature childArmature = (IArmature)slot.getChildArmature();
			if(childArmature != null)
			{
				childArmature.getAnimation().gotoAndPlay(animationName);
			}
		}
		return animationState;
	}

	public FastAnimationState gotoAndStop(
		String animationName,
		double time
	) {
		return gotoAndStop(animationName, time, -1, 0, -1);
	}

	/**
	 * Control the animation to stop with a specified time. If related animationState haven't been created, then create a new animationState.
	 * @param animationName The name of the animationState.
	 * @param time
	 * @param normalizedTime
	 * @param fadeInTime A fade time to apply (>= 0), -1 means use xml data's fadeInTime.
	 * @param duration The duration of that Animation. -1 means use xml data's duration.
	 * @return AnimationState.
	 * @see dragonBones.objects.AnimationData
	 * @see dragonBones.animation.AnimationState
	 */
	public FastAnimationState gotoAndStop(
		String animationName,
		double time,
		double normalizedTime,
		double fadeInTime,
		double duration
	)
	{
		if(Objects.equals(animationState.name, animationName))
		{
			gotoAndPlay(animationName, fadeInTime, duration);
		}

		if(normalizedTime >= 0)
		{
			animationState.setCurrentTime(animationState.getTotalTime() * normalizedTime);
		}
		else
		{
			animationState.setCurrentTime(time);
		}

		animationState.stop();
		return animationState;
	}

	/**
	 * Play the animation from the current position.
	 */
	public void play()
	{
		if(_animationDataList == null)
		{
			return;
		}
		if(animationState.name == null)
		{
			gotoAndPlay(_animationDataList.get(0).name);
		}
		else if (!_isPlaying)
		{
			_isPlaying = true;
		}
		else
		{
			gotoAndPlay(animationState.name);
		}
	}

	public void stop()
	{
		_isPlaying = false;
	}

	/** @private */
	public void advanceTime(double passedTime)
	{
		if(!_isPlaying)
		{
			return;
		}

		animationState.advanceTime(passedTime * _timeScale);
	}

	/**
	 * check if contains a AnimationData by name.
	 * @return Boolean.
	 */
	public boolean hasAnimation(String animationName)
	{
		return _animationDataObj.containsKey(animationName);
	}

	/**
	 * The amount by which passed time should be scaled. Used to slow down or speed up animations. Defaults to 1.
	 */
	public double getTimeScale()
	{
		return _timeScale;
	}
	public void setTimeScale(double value)
	{
		if(Double.isNaN(value) || value < 0)
		{
			value = 1;
		}
		_timeScale = value;
	}

	/**
	 * The AnimationData list associated with this Animation instance.
	 * @see dragonBones.objects.AnimationData
	 */
	public ArrayList<AnimationData> getAnimationDataList()
	{
		return _animationDataList;
	}
	public void setAnimationDataList(ArrayList<AnimationData> value)
	{
		_animationDataList = value;
		animationList.clear();
		for (AnimationData animationData : _animationDataList)
		{
			animationList.add(animationData.name);
			_animationDataObj.put(animationData.name, animationData);
		}
	}

	/**
	 * Unrecommended API. Recommend use animationList.
	 */
	public ArrayList<String> getMovementList()
	{
		return animationList;
	}

	/**
	 * Unrecommended API. Recommend use lastAnimationName.
	 */
	public String getMovementID()
	{
		return getLastAnimationName();
	}

	/**
	 * Is the animation playing.
	 * @see dragonBones.animation.AnimationState
	 */
	public boolean isPlaying()
	{
		return _isPlaying && !isComplete();
	}

	/**
	 * Is animation complete.
	 * @see dragonBones.animation.AnimationState
	 */
	public boolean isComplete()
	{
		return animationState.isComplete();
	}

	/**
	 * The last AnimationState this Animation played.
	 * @see dragonBones.objects.AnimationData
	 */
	public FastAnimationState getLastAnimationState()
	{
		return animationState;
	}
	/**
	 * The name of the last AnimationData played.
	 * @see dragonBones.objects.AnimationData
	 */
	public String getLastAnimationName()
	{
		return animationState!= null?animationState.name:null;
	}
}
