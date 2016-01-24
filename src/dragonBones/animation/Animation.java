package dragonBones.animation;

import dragonBones.Armature;
import dragonBones.Slot;
import dragonBones.objects.AnimationData;
import dragonBones.utils.ArrayListUtils;

import java.util.ArrayList;
import java.util.Objects;

/**
 * An Animation instance is used to control the animation state of an Armature.
 * @see dragonBones.Armature
 * @see Animation
 * @see dragonBones.animation.AnimationState
 */
public class Animation
{
	public static final String NONE = "none";
	public static final String SAME_LAYER = "sameLayer";
	public static final String SAME_GROUP = "sameGroup";
	public static final String SAME_LAYER_AND_GROUP = "sameLayerAndGroup";
	public static final String ALL = "all";

	/**
	 * Whether animation tweening is enabled or not.
	 */
	public boolean tweenEnabled;
	private Armature _armature;
	private ArrayList<AnimationState> _animationStateList;
	private ArrayList<AnimationData> _animationDataList;
	private ArrayList<String> _animationList;
	private boolean _isPlaying;
	private double _timeScale;

	/** @private */
	private AnimationState _lastAnimationState;

	/** @private */
	public boolean _isFading;

	/** @private */
	private int _animationStateCount;


	/**
	 * Creates a new Animation instance and attaches it to the passed Armature.
	 * @param armature An Armature to attach this Animation instance to.
	 */
	public Animation(Armature armature)
	{
		_armature = armature;
		_animationList = new ArrayList<String>();
		_animationStateList = new ArrayList<AnimationState>();

		_timeScale = 1;
		_isPlaying = false;

		tweenEnabled = true;
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

		resetAnimationStateList();

		_animationList.clear();

		_armature = null;
		_animationDataList = null;
		_animationList = null;
		_animationStateList = null;
	}

	public void resetAnimationStateList()
	{
		int i = _animationStateList.size();
		AnimationState animationState;
		while(i -- > 0)
		{
			animationState = _animationStateList.get(i);
			animationState.resetTimelineStateList();
			AnimationState.returnObject(animationState);
		}
		_animationStateList.clear();
	}

	public AnimationState gotoAndPlay(String animationName) {
		return gotoAndPlay(animationName, -1, -1, Double.NaN, 0, null, SAME_LAYER_AND_GROUP, true, true);
	}

	public AnimationState gotoAndPlay(String animationName, double fadeInTime) {
		return gotoAndPlay(animationName, fadeInTime, -1, Double.NaN, 0, null, SAME_LAYER_AND_GROUP, true, true);
	}


	/**
	 * Fades the animation with name animation in over a period of time seconds and fades other animations out.
	 * @param animationName The name of the AnimationData to play.
	 * @param fadeInTime A fade time to apply (>= 0), -1 means use xml data's fadeInTime.
	 * @param duration The duration of that Animation. -1 means use xml data's duration.
	 * @param playTimes Play times(0:loop forever, >=1:play times, -1~-∞:will fade animation after play complete), 默认使用AnimationData.loop.
	 * @param layer The layer of the animation.
	 * @param group The group of the animation.
	 * @param fadeOutMode Fade out mode (none, sameLayer, sameGroup, sameLayerAndGroup, all).
	 * @param pauseFadeOut Pause other animation playing.
	 * @param pauseFadeIn Pause this animation playing before fade in complete.
	 * @return AnimationState.
	 * @see dragonBones.objects.AnimationData
	 * @see dragonBones.animation.AnimationState
	 */
	public AnimationState gotoAndPlay(
		String animationName,
		double fadeInTime,
		double duration,
		double playTimes,
		int layer,
		String group,
		String fadeOutMode,
		boolean pauseFadeOut,
		boolean pauseFadeIn
	)
	{
		if (_animationDataList == null)
		{
			return null;
		}
		int i = _animationDataList.size();
		AnimationData animationData = null;
		while(i -- > 0)
		{
			if(Objects.equals(_animationDataList.get(i).name, animationName))
			{
				animationData = _animationDataList.get(i);
				break;
			}
		}
		if (animationData == null)
		{
			return null;
		}
		boolean needUpdata = !_isPlaying;
		_isPlaying = true;
		_isFading = true;

		//
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

//根据fadeOutMode,选择正确的animationState执行fadeOut
		AnimationState animationState;
		switch(fadeOutMode)
		{
			case NONE:
				break;

			case SAME_LAYER:
				i = _animationStateList.size();
				while(i -- > 0)
				{
					animationState = _animationStateList.get(i);
					if(animationState.getLayer() == layer)
					{
						animationState.fadeOut(fadeInTime, pauseFadeOut);
					}
				}
				break;

			case SAME_GROUP:
				i = _animationStateList.size();
				while(i -- > 0)
				{
					animationState = _animationStateList.get(i);
					if(Objects.equals(animationState.getGroup(), group))
					{
						animationState.fadeOut(fadeInTime, pauseFadeOut);
					}
				}
				break;

			case ALL:
				i = _animationStateList.size();
				while(i -- > 0)
				{
					animationState = _animationStateList.get(i);
					animationState.fadeOut(fadeInTime, pauseFadeOut);
				}
				break;

			case SAME_LAYER_AND_GROUP:
			default:
				i = _animationStateList.size();
				while(i -- > 0)
				{
					animationState = _animationStateList.get(i);
					if(animationState.getLayer() == layer && Objects.equals(animationState.getGroup(), group))
					{
						animationState.fadeOut(fadeInTime, pauseFadeOut);
					}
				}
				break;
		}

		_lastAnimationState = AnimationState.borrowObject();
		_lastAnimationState._layer = layer;
		_lastAnimationState._group = group;
		_lastAnimationState.autoTween = tweenEnabled;
		_lastAnimationState.fadeIn(_armature, animationData, fadeInTime, 1 / durationScale, (int)playTimes, pauseFadeIn);

		addState(_lastAnimationState);

	//控制子骨架播放同名动画
		ArrayList<Slot> slotList = _armature.getSlots(false);
		i = slotList.size();
		while(i -- > 0)
		{
			Slot slot = slotList.get(i);
			if(slot.getChildArmature() != null)
			{
				slot.getChildArmature().getAnimation().gotoAndPlay(animationName, fadeInTime);
			}
		}
		if(needUpdata)
		{
			_armature.advanceTime(0);
		}
		return _lastAnimationState;
	}

	public AnimationState gotoAndStop(
		String animationName,
		double time
	) {
		return gotoAndStop(animationName, time, -1, 0, -1, 0, null, ALL);
	}

		/**
		 * Control the animation to stop with a specified time. If related animationState haven't been created, then create a new animationState.
		 * @param animationName The name of the animationState.
		 * @param time
		 * @param normalizedTime
		 * @param fadeInTime A fade time to apply (>= 0), -1 means use xml data's fadeInTime.
		 * @param duration The duration of that Animation. -1 means use xml data's duration.
		 * @param layer The layer of the animation.
		 * @param group The group of the animation.
		 * @param fadeOutMode Fade out mode (none, sameLayer, sameGroup, sameLayerAndGroup, all).
		 * @return AnimationState.
		 * @see dragonBones.objects.AnimationData
		 * @see dragonBones.animation.AnimationState
		 */
	public AnimationState gotoAndStop(
		String animationName,
		double time,
		double normalizedTime,
		double fadeInTime,
		double duration,
		int layer,
		String group,
		String fadeOutMode
	)
	{
		AnimationState animationState = getState(animationName, layer);
		if(animationState == null)
		{
			animationState = gotoAndPlay(animationName, fadeInTime, duration, Double.NaN, layer, group, fadeOutMode, true, true);
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
		if (_animationDataList == null || _animationDataList.size() == 0)
		{
			return;
		}
		if(_lastAnimationState == null)
		{
			gotoAndPlay(_animationDataList.get(0).name);
		}
		else if (!_isPlaying)
		{
			_isPlaying = true;
		}
		else
		{
			gotoAndPlay(_lastAnimationState.getName());
		}
	}

	public void stop()
	{
		_isPlaying = false;
	}

	public AnimationState getState(String name)
	{
		return getState(name, 0);
	}

	/**
	 * Returns the AnimationState named name.
	 * @return A AnimationState instance.
	 * @see dragonBones.animation.AnimationState
	 */
	public AnimationState getState(String name, int layer)
	{
		int i = _animationStateList.size();
		while(i -- > 0)
		{
			AnimationState animationState = _animationStateList.get(i);
			if(Objects.equals(animationState.getName(), name) && animationState.getLayer() == layer)
			{
				return animationState;
			}
		}
		return null;
	}

	/**
	 * check if contains a AnimationData by name.
	 * @return Boolean.
	 */
	public boolean hasAnimation(String animationName)
	{
		int i = _animationDataList.size();
		while(i -- > 0)
		{
			if(Objects.equals(_animationDataList.get(i).name, animationName))
			{
				return true;
			}
		}

		return false;
	}

	/** @private */
	public void advanceTime(double passedTime)
	{
		if(!_isPlaying)
		{
			return;
		}

		boolean isFading = false;

		passedTime *= _timeScale;
		int i = _animationStateList.size();
		while(i -- > 0)
		{
			AnimationState animationState = _animationStateList.get(i);
			if(animationState.advanceTime(passedTime))
			{
				removeState(animationState);
			}
			else if(animationState.getFadeState() != 1)
			{
				isFading = true;
			}
		}

		_isFading = isFading;
	}

	/** @private */
	//当动画播放过程中Bonelist改变时触发
	public void updateAnimationStates()
	{
		int i = _animationStateList.size();
		while(i -- > 0)
		{
			_animationStateList.get(i).updateTimelineStates();
		}
	}

	private void addState(AnimationState animationState)
	{
		if(_animationStateList.indexOf(animationState) < 0)
		{
			ArrayListUtils.unshift(_animationStateList, animationState);

			_animationStateCount = _animationStateList.size();
		}
	}

	private void removeState(AnimationState animationState)
	{
		int index = _animationStateList.indexOf(animationState);
		if(index >= 0)
		{
			_animationStateList.remove(index); // removeAt
			AnimationState.returnObject(animationState);

			if(_lastAnimationState == animationState)
			{
				if(_animationStateList.size() > 0)
				{
					_lastAnimationState = _animationStateList.get(0);
				}
				else
				{
					_lastAnimationState = null;
				}
			}

			_animationStateCount = _animationStateList.size();
		}
	}



	/**
	* Unrecommended API. Recommend use animationList.
	*/
	public ArrayList<String> getMovementList()
	{
		return _animationList;
	}

	/**
	* Unrecommended API. Recommend use lastAnimationName.
	*/
	public String getMovementID()
	{
		return getLastAnimationName();
	}



	/**
	 * The last AnimationState this Animation played.
	 * @see dragonBones.objects.AnimationData
	 */
	public AnimationState getLastAnimationState()
	{
		return _lastAnimationState;
	}
	/**
	 * The name of the last AnimationData played.
	 * @see dragonBones.objects.AnimationData
	 */
	public String getLastAnimationName()
	{
		return _lastAnimationState != null?_lastAnimationState.getName():null;
	}


	/**
	 * An vector containing all AnimationData names the Animation can play.
	 * @see dragonBones.objects.AnimationData
	 */
	public ArrayList<String> getAnimationList()
	{
		return _animationList;
	}


	/**
	 * Is the animation playing.
	 * @see dragonBones.animation.AnimationState
	 */
	public boolean getIsPlaying()
	{
		return _isPlaying && !isComplete();
	}

	/**
	 * Is animation complete.
	 * @see dragonBones.animation.AnimationState
	 */
	public boolean isComplete()
	{
		if(_lastAnimationState != null)
		{
			if(!_lastAnimationState.isComplete())
			{
				return false;
			}
			int i = _animationStateList.size();
			while(i -- > 0)
			{
				if(!_animationStateList.get(i).isComplete())
				{
					return false;
				}
			}
			return true;
		}
		return true;
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
		_animationList.clear();
		for (AnimationData animationData : _animationDataList)
		{
			_animationList.set(_animationList.size(), animationData.name);
		}
	}

}
