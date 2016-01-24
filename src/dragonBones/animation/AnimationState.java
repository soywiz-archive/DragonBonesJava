package dragonBones.animation;
import dragonBones.Armature;
import dragonBones.Bone;
import dragonBones.Slot;
import dragonBones.core.dragonBones_internal;
import dragonBones.events.AnimationEvent;
import dragonBones.objects.AnimationData;
import dragonBones.objects.Frame;
import dragonBones.objects.SlotTimeline;
import dragonBones.objects.TransformTimeline;
import dragonBones.utils.ArrayListUtils;

import java.util.ArrayList;
import java.util.Objects;

//use namespace dragonBones_internal;
/**
 * The AnimationState gives full control over animation blending.
 * In most cases the Animation interface is sufficient and easier to use. Use the AnimationState if you need full control over the animation blending any playback process.
 */
final public class AnimationState
{
	private static ArrayList<AnimationState> _pool = new ArrayList<AnimationState>();

	/** @private */
	private static AnimationState borrowObject()
	{
		if(_pool.size() == 0)
		{
			return new AnimationState();
		}
		return ArrayListUtils.pop(_pool);
	}

	/** @private */
	private static void returnObject(AnimationState animationState)
	{
		animationState.clear();

		if(_pool.indexOf(animationState) < 0)
		{
			_pool.set(_pool.size(), animationState);
		}
	}

	/** @private */
	private static void clearStatic()
	{
		int i = _pool.size();
		while(i -- > 0)
		{
			_pool.get(i).clear();
		}
		_pool.clear();

		TimelineState.clearStatic();
	}

	/**
	 * Sometimes, we want slots controlled by a spedific animation state when animation is doing mix or addition.
	 * It determine if animation's color change, displayIndex change, visible change can apply to its display
	 */
	public boolean displayControl;

	/**
	 * If animation mixing use additive blending.
	 */
	public boolean additiveBlending;

	/**
	 * If animation auto fade out after play complete.
	 */
	public boolean autoFadeOut;
	/**
	 * Duration of fade out. By default, it equals to fade in time.
	 */
	public double fadeOutTime;

	/**
	 * The weight of animation.
	 */
	public double weight;

	/**
	 * If auto genterate tween between keyframes.
	 */
	public boolean autoTween;
	/**
	 * If generate tween between the lastFrame to the first frame for loop animation.
	 */
	public boolean lastFrameAutoTween;

	/** @private */
	private int _layer;
	/** @private */
	private String _group;

	private Armature _armature;
	private ArrayList<TimelineState> _timelineStateList;
	private ArrayList<SlotTimelineState> _slotTimelineStateList;
	private ArrayList<String> _boneMasks;

	private boolean _isPlaying;
	private double _time;
	private int _currentFrameIndex;
	private int _currentFramePosition;
	private int _currentFrameDuration;

	//Fadein 的时候是否先暂停
	private boolean _pausePlayheadInFade;
	private boolean _isFadeOut;
	//最终的真实权重值
	private double _fadeTotalWeight;
	//受fade影响的动作权重系数，在fadein阶段他的值会由0变为1，在fadeout阶段会由1变为0
	private double _fadeWeight;
	private double _fadeCurrentTime;
	private double _fadeBeginTime;

	private String _name;
	private AnimationData _clip;
	private boolean _isComplete;
	private int _currentPlayTimes;
	private int _totalTime;
	private int _currentTime;
	private int _lastTime;
	//-1 beforeFade, 0 fading, 1 fadeComplete
	private int _fadeState;
	private double _fadeTotalTime;

	//时间缩放参数， 各帧duration数据不变的情况下，让传入时间*timeScale 实现durationScale
	private double _timeScale;
	private int _playTimes;

	public AnimationState()
	{
		_timelineStateList = new ArrayList<TimelineState>;
		_slotTimelineStateList = new ArrayList<SlotTimelineState>;
		_boneMasks = new ArrayList<String>;
	}

	private void clear()
	{
		resetTimelineStateList();

		_boneMasks.clear();

		_armature = null;
		_clip = null;
	}

	private void resetTimelineStateList()
	{
		int i = _timelineStateList.size();
		while(i -- > 0)
		{
			TimelineState.returnObject(_timelineStateList.get(i));
		}
		_timelineStateList.clear();

		i = _slotTimelineStateList.size();
		while(i -- > 0)
		{
			SlotTimelineState.returnObject(_slotTimelineStateList.get(i));
		}
		_slotTimelineStateList.clear();
	}

//骨架装配
	public boolean containsBoneMask(String boneName)
	{
		return _boneMasks.size() == 0 || _boneMasks.indexOf(boneName) >= 0;
	}

	/**
	 * Adds a bone which should be animated. This allows you to reduce the number of animations you have to create.
	 * @param boneName Bone's name.
	 * @param ifInvolveChildBones if involve child bone's animation.
	 */
	public AnimationState addBoneMask(String boneName, boolean ifInvolveChildBones = true)
	{
		addBoneToBoneMask(boneName);

		if(ifInvolveChildBones)
		{
			Bone currentBone = _armature.getBone(boneName);
			if(currentBone != null)
			{
				ArrayList<Bone> boneList = _armature.getBones(false);
				int i = boneList.size();
				while(i-- > 0)
				{
					Bone tempBone = boneList.get(i);
					if(currentBone.contains(tempBone))
					{
						addBoneToBoneMask(tempBone.name);
					}
				}
			}
		}

		updateTimelineStates();
		return this;
	}

	public AnimationState removeBoneMask(String boneName)
	{
		removeBoneMask(boneName, true);
	}

	/**
	 * Removes a bone which was supposed be animated.
	 * @param boneName Bone's timeline name.
	 * @param ifInvolveChildBones If involved child bone's timeline.
	 */
	public AnimationState removeBoneMask(String boneName, boolean ifInvolveChildBones)
	{
		removeBoneFromBoneMask(boneName);

		if(ifInvolveChildBones)
		{
			Bone currentBone = _armature.getBone(boneName);
			if(currentBone != null)
			{
				ArrayList<Bone> boneList = _armature.getBones(false);
				int i = boneList.size();
				while(i-- > 0)
				{
					Bone tempBone = boneList.get(i);
					if(currentBone.contains(tempBone))
					{
						removeBoneFromBoneMask(tempBone.name);
					}
				}
			}
		}
		updateTimelineStates();

		return this;
	}

	public AnimationState removeAllMixingTransform()
	{
		_boneMasks.clear();
		updateTimelineStates();
		return this;
	}

	private void addBoneToBoneMask(String boneName)
	{
		if(_clip.getTimeline(boneName) != null && _boneMasks.indexOf(boneName)<0)
		{
			_boneMasks.add(boneName);
		}
	}

	private void removeBoneFromBoneMask(String boneName)
	{
		_boneMasks.remove(boneName);
	}

	/**
	 * @private
	 * Update timeline state based on mixing transforms and clip.
	 */
	private void updateTimelineStates()
	{
		TimelineState timelineState;
		SlotTimelineState slotTimelineState;
		int i = _timelineStateList.size();
		while(i -- > 0)
		{
			timelineState = _timelineStateList.get(i);
			if(_armature.getBone(timelineState.name) == null)
			{
				removeTimelineState(timelineState);
			}
		}

		i = _slotTimelineStateList.size();
		while (i -- > 0)
		{
			slotTimelineState = _slotTimelineStateList.get(i);
			if (_armature.getSlot(slotTimelineState.name) == null)
			{
				removeSlotTimelineState(slotTimelineState);
			}
		}

		if(_boneMasks.size() > 0)
		{
			i = _timelineStateList.size();
			while(i -- > 0)
			{
				timelineState = _timelineStateList.get(i);
				if(_boneMasks.indexOf(timelineState.name) < 0)
				{
					removeTimelineState(timelineState);
				}
			}

			for (String timelineName : _boneMasks)
			{
				addTimelineState(timelineName);
			}
		}
		else
		{
			for (TransformTimeline timeline : _clip.getTimelineList())
			{
				addTimelineState(timeline.name);
			}
		}

		for (SlotTimeline slotTimeline : _clip.getSlotTimelineList())
		{
			addSlotTimelineState(slotTimeline.name);
		}
	}

	private void addTimelineState(String timelineName)
	{
		Bone bone = _armature.getBone(timelineName);
		if(bone != null)
		{
			for (TimelineState eachState : _timelineStateList)
			{
				if(Objects.equals(eachState.name, timelineName))
				{
					return;
				}
			}
			TimelineState timelineState = TimelineState.borrowObject();
			timelineState.fadeIn(bone, this, _clip.getTimeline(timelineName));
			_timelineStateList.add(timelineState);
		}
	}

	private void removeTimelineState(TimelineState timelineState)
	{
		_timelineStateList.remove(timelineState);
		TimelineState.returnObject(timelineState);
	}

	private void addSlotTimelineState(String timelineName)
	{
		Slot slot = _armature.getSlot(timelineName);
		if(slot != null && slot.getDisplayList().size() > 0)
		{
			for (SlotTimelineState eachState : _slotTimelineStateList)
			{
				if(Objects.equals(eachState.name, timelineName))
				{
					return;
				}
			}
			SlotTimelineState timelineState = SlotTimelineState.borrowObject();
			timelineState.fadeIn(slot, this, _clip.getSlotTimeline(timelineName));
			_slotTimelineStateList.add(timelineState);
		}
	}

	private void removeSlotTimelineState(SlotTimelineState timelineState)
	{
		_slotTimelineStateList.remove(timelineState);
		SlotTimelineState.returnObject(timelineState);
	}

//动画
	/**
	 * Play the current animation. 如果动画已经播放完毕, 将不会继续播放.
	 */
	public AnimationState play()
	{
		_isPlaying = true;
		return this;
	}

	/**
	 * Stop playing current animation.
	 */
	public AnimationState stop()
	{
		_isPlaying = false;
		return this;
	}

	/** @private */
	private AnimationState fadeIn(Armature armature, AnimationData clip, double fadeTotalTime, double timeScale, int playTimes, boolean pausePlayhead)
	{
		_armature = armature;
		_clip = clip;
		_pausePlayheadInFade = pausePlayhead;

		_name = _clip.name;
		_totalTime = _clip.duration;

		autoTween = _clip.autoTween;

		setTimeScale(timeScale);
		setPlayTimes(playTimes);

		//reset
		_isComplete = false;
		_currentFrameIndex = -1;
		_currentPlayTimes = -1;
		if(Math.round(_totalTime * _clip.frameRate * 0.001) < 2 || timeScale == Double.POSITIVE_INFINITY)
		{
			_currentTime = _totalTime;
		}
		else
		{
			_currentTime = -1;
		}
		_time = 0;
		_boneMasks.clear();

		//fade start
		_isFadeOut = false;
		_fadeWeight = 0;
		_fadeTotalWeight = 1;
		_fadeState = -1;
		_fadeCurrentTime = 0;
		_fadeBeginTime = _fadeCurrentTime;
		_fadeTotalTime = fadeTotalTime * _timeScale;

		//default
		_isPlaying = true;
		displayControl = true;
		lastFrameAutoTween = true;
		additiveBlending = false;
		weight = 1;
		fadeOutTime = fadeTotalTime;

		updateTimelineStates();
		return this;
	}

	/**
	 * Fade out the animation state
	 * @param fadeTotalTime
	 * @param pausePlayhead pause the animation before fade out complete
	 */
	public AnimationState fadeOut(double fadeTotalTime, boolean pausePlayhead)
	{
		if(_armature == null)
		{
			return null;
		}

		if(Double.isNaN(fadeTotalTime) || fadeTotalTime < 0)
		{
			fadeTotalTime = 0;
		}
		_pausePlayheadInFade = pausePlayhead;

		if(_isFadeOut)
		{
			if(fadeTotalTime > _fadeTotalTime / _timeScale - (_fadeCurrentTime - _fadeBeginTime))
			{
				//如果已经在淡出中，新的淡出需要更长的淡出时间，则忽略
				//If the animation is already in fade out, the new fade out will be ignored.
				return this;
			}
		}
		else
		{
			//第一次淡出
			//The first time to fade out.
			for (TimelineState timelineState : _timelineStateList)
			{
				timelineState.fadeOut();
			}
		}

		//fade start
		_isFadeOut = true;
		_fadeTotalWeight = _fadeWeight;
		_fadeState = -1;
		_fadeBeginTime = _fadeCurrentTime;
		_fadeTotalTime = _fadeTotalWeight >= 0?fadeTotalTime * _timeScale:0;

		//default
		displayControl = false;

		return this;
	}

	/** @private */
	private boolean advanceTime(double passedTime)
	{
		passedTime *= _timeScale;

		advanceFadeTime(passedTime);

		if(_fadeWeight != 0)
		{
			advanceTimelinesTime(passedTime);
		}

		return _isFadeOut && _fadeState == 1;
	}

	private void advanceFadeTime(double passedTime)
	{
		boolean fadeStartFlg = false;
		boolean fadeCompleteFlg = false;

		if(_fadeBeginTime >= 0)
		{
			int fadeState = _fadeState;
			_fadeCurrentTime += passedTime < 0?-passedTime:passedTime;
			if(_fadeCurrentTime >= _fadeBeginTime + _fadeTotalTime)
			{
				//fade完全结束之后触发
				//TODO 研究明白为什么要下次再触发
				if(
					_fadeWeight == 1 ||
					_fadeWeight == 0
				)
				{
					fadeState = 1;
					if (_pausePlayheadInFade)
					{
						_pausePlayheadInFade = false;
						_currentTime = -1;
					}
				}

				_fadeWeight = _isFadeOut?0:1;
			}
			else if(_fadeCurrentTime >= _fadeBeginTime)
			{
				//fading
				fadeState = 0;
				//暂时只支持线性淡入淡出
				//Currently only support Linear fadein and fadeout
				_fadeWeight = (_fadeCurrentTime - _fadeBeginTime) / _fadeTotalTime * _fadeTotalWeight;
				if(_isFadeOut)
				{
					_fadeWeight = _fadeTotalWeight - _fadeWeight;
				}
			}
			else
			{
				//before fade
				fadeState = -1;
				_fadeWeight = _isFadeOut?1:0;
			}

			if(_fadeState != fadeState)
			{
				//_fadeState == -1 && (fadeState == 0 || fadeState == 1)
				if(_fadeState == -1)
				{
					fadeStartFlg = true;
				}

				//(_fadeState == -1 || _fadeState == 0) && fadeState == 1
				if(fadeState == 1)
				{
					fadeCompleteFlg = true;
				}
				_fadeState = fadeState;
			}
		}

		AnimationEvent event;

		if(fadeStartFlg)
		{
			if(_isFadeOut)
			{
				if(_armature.hasEventListener(AnimationEvent.FADE_OUT))
				{
					event = new AnimationEvent(AnimationEvent.FADE_OUT);
					event.animationState = this;
					_armature._eventList.push(event);
				}
			}
			else
			{
				//动画开始，先隐藏不需要的骨头
				hideBones();

				if(_armature.hasEventListener(AnimationEvent.FADE_IN))
				{
					event = new AnimationEvent(AnimationEvent.FADE_IN);
					event.animationState = this;
					_armature._eventList.push(event);
				}
			}
		}

		if(fadeCompleteFlg)
		{
			if(_isFadeOut)
			{
				if(_armature.hasEventListener(AnimationEvent.FADE_OUT_COMPLETE))
				{
					event = new AnimationEvent(AnimationEvent.FADE_OUT_COMPLETE);
					event.animationState = this;
					_armature._eventList.push(event);
				}
			}
			else
			{
				if(_armature.hasEventListener(AnimationEvent.FADE_IN_COMPLETE))
				{
					event = new AnimationEvent(AnimationEvent.FADE_IN_COMPLETE);
					event.animationState = this;
					_armature._eventList.push(event);
				}
			}
		}
	}

	private void advanceTimelinesTime(double passedTime)
	{
		if(_isPlaying && !_pausePlayheadInFade)
		{
			_time += passedTime;
		}

		boolean startFlg = false;
		boolean completeFlg = false;
		boolean loopCompleteFlg = false;
		boolean isThisComplete = false;
		int currentPlayTimes = 0;
		int currentTime = _time * 1000;
		if(_playTimes == 0)
		{
			isThisComplete = false;
			currentPlayTimes = Math.ceil(Math.abs(currentTime) / _totalTime) || 1;
			//currentTime -= Math.floor(currentTime / _totalTime) * _totalTime;

			currentTime -= int(currentTime / _totalTime) * _totalTime;

			if(currentTime < 0)
			{
				currentTime += _totalTime;
			}
		}
		else
		{
			int totalTimes = _playTimes * _totalTime;
			if(currentTime >= totalTimes)
			{
				currentTime = totalTimes;
				isThisComplete = true;
			}
			else if(currentTime <= -totalTimes)
			{
				currentTime = -totalTimes;
				isThisComplete = true;
			}
			else
			{
				isThisComplete = false;
			}

			if(currentTime < 0)
			{
				currentTime += totalTimes;
			}

			currentPlayTimes = (int)Math.ceil(currentTime / _totalTime);
			if (currentPlayTimes == 0) currentPlayTimes = 1;
			//currentTime -= Math.floor(currentTime / _totalTime) * _totalTime;
			currentTime -= (int)(currentTime / _totalTime) * _totalTime;

			if(isThisComplete)
			{
				currentTime = _totalTime;
			}
		}

		//update timeline
		_isComplete = isThisComplete;
		double progress = _time * 1000 / _totalTime;
		for (TimelineState timeline : _timelineStateList)
		{
			timeline.update(progress);
			_isComplete = timeline._isComplete && _isComplete;
		}
		//update slotTimelie
		for (SlotTimelineState slotTimeline : _slotTimelineStateList)
		{
			slotTimeline.update(progress);
			_isComplete = slotTimeline._isComplete && _isComplete;
		}
		//update main timeline
		if(_currentTime != currentTime)
		{
			if(_currentPlayTimes != currentPlayTimes)    //check loop complete
			{
				if(_currentPlayTimes > 0 && currentPlayTimes > 1)
				{
					loopCompleteFlg = true;
				}
				_currentPlayTimes = currentPlayTimes;
			}

			if(_currentTime < 0)    //check start
			{
				startFlg = true;
			}

			if(_isComplete)    //check complete
			{
				completeFlg = true;
			}
			_lastTime = _currentTime;
			_currentTime = currentTime;
			/*
			if(isThisComplete)
			{
			currentTime = _totalTime * 0.999999;
			}
			//[0, _totalTime)
			*/
			updateMainTimeline(isThisComplete);
		}

		AnimationEvent event;
		if(startFlg)
		{
			if(_armature.hasEventListener(AnimationEvent.START))
			{
				event = new AnimationEvent(AnimationEvent.START);
				event.animationState = this;
				_armature._eventList.push(event);
			}
		}

		if(completeFlg)
		{
			if(_armature.hasEventListener(AnimationEvent.COMPLETE))
			{
				event = new AnimationEvent(AnimationEvent.COMPLETE);
				event.animationState = this;
				_armature._eventList.push(event);
			}
			if(autoFadeOut)
			{
				fadeOut(fadeOutTime, true);
			}
		}
		else if(loopCompleteFlg)
		{
			if(_armature.hasEventListener(AnimationEvent.LOOP_COMPLETE))
			{
				event = new AnimationEvent(AnimationEvent.LOOP_COMPLETE);
				event.animationState = this;
				_armature._eventList.push(event);
			}
		}
	}

	private void updateMainTimeline(boolean isThisComplete)
	{
		ArrayList<Frame> frameList = _clip.getFrameList();
		if(frameList.size() > 0)
		{
			Frame prevFrame = null;
			Frame currentFrame = null;
			for (int i = 0, l = _clip.getFrameList().size(); i < l; ++i)
			{
				if(_currentFrameIndex < 0)
				{
					_currentFrameIndex = 0;
				}
				else if(_currentTime < _currentFramePosition || _currentTime >= _currentFramePosition + _currentFrameDuration || _currentTime < _lastTime)
				{
					_lastTime = _currentTime;
					_currentFrameIndex ++;
					if(_currentFrameIndex >= frameList.size())
					{
						if(isThisComplete)
						{
							_currentFrameIndex --;
							break;
						}
						else
						{
							_currentFrameIndex = 0;
						}
					}
				}
				else
				{
					break;
				}
				currentFrame = frameList.get(_currentFrameIndex);

				if(prevFrame != null)
				{
					_armature.arriveAtFrame(prevFrame, null, this, true);
				}

				_currentFrameDuration = currentFrame.duration;
				_currentFramePosition = currentFrame.position;
				prevFrame = currentFrame;
			}

			if(currentFrame != null)
			{
				_armature.arriveAtFrame(currentFrame, null, this, false);
			}
		}
	}

	private void hideBones()
	{
		for (String timelineName : _clip.hideTimelineNameMap)
		{
			Bone bone = _armature.getBone(timelineName);
			if(bone != null)
			{
				bone.hideSlots();
			}
		}
		for (String slotTimelineName : _clip.hideSlotTimelineNameMap)
		{
			Slot slot= _armature.getSlot(slotTimelineName);
			if (slot != null)
			{
				slot.resetToOrigin();
			}
		}
	}

//属性访问
	public AnimationState setAdditiveBlending(boolean value)
	{
		additiveBlending = value;
		return this;
	}


	public AnimationState setAutoFadeOut(boolean value, double fadeOutTime = -1)
	{
		autoFadeOut = value;
		if(fadeOutTime >= 0)
		{
			this.fadeOutTime = fadeOutTime * _timeScale;
		}
		return this;
	}

	public AnimationState setWeight(double value)
	{
		if(Double.isNaN(value) || value < 0)
		{
			value = 1;
		}
		weight = value;
		return this;
	}

	public AnimationState setFrameTween(boolean autoTween, boolean lastFrameAutoTween)
	{
		this.autoTween = autoTween;
		this.lastFrameAutoTween = lastFrameAutoTween;
		return this;
	}

	public AnimationState setCurrentTime(double value)
	{
		if(value < 0 || Double.isNaN(value))
		{
			value = 0;
		}
		_time = value;
		_currentTime = (int)(_time * 1000);
		return this;
	}

	public AnimationState setTimeScale(double value)
	{
		if(Double.isNaN(value) || value == Double.POSITIVE_INFINITY)
		{
			value = 1;
		}
		_timeScale = value;
		return this;
	}

	public AnimationState setPlayTimes(int value)
	{
		//如果动画只有一帧  播放一次就可以
		if(Math.round(_totalTime * 0.001 * _clip.frameRate) < 2)
		{
			_playTimes = value < 0?-1:1;
		}
		else
		{
			_playTimes = value < 0?-value:value;
		}
		autoFadeOut = value < 0?true:false;
		return this;
	}

	/**
	 * The name of the animation state.
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * The layer of the animation. When calculating the final blend weights, animations in higher layers will get their weights.
	 */
	public int getLayer()
	{
		return _layer;
	}

	/**
	 * The group of the animation.
	 */
	public String getGroup()
	{
		return _group;
	}

	/**
	 * The clip that is being played by this animation state.
	 * @see dragonBones.objects.AnimationData
	 */
	public AnimationData getClip()
	{
		return _clip;
	}

	/**
	 * Is animation complete.
	 */
	public boolean getIsComplete()
	{
		return _isComplete;
	}
	/**
	 * Is animation playing.
	 */
	public boolean getIsPlaying()
	{
		return (_isPlaying && !_isComplete);
	}

	/**
	 * Current animation played times
	 */
	public int getCurrentPlayTimes()
	{
		return _currentPlayTimes < 0 ? 0 : _currentPlayTimes;
	}

	/**
	 * The length of the animation clip in seconds.
	 */
	public double getTotalTime()
	{
		return _totalTime * 0.001;
	}

	/**
	 * The current time of the animation.
	 */
	public double getCurrentTime()
	{
		return _currentTime < 0 ? 0 : _currentTime * 0.001;
	}

	public double getFadeWeight()
	{
		return _fadeWeight;
	}

	public int getFadeState()
	{
		return _fadeState;
	}

	public double getFadeTotalTime()
	{
		return _fadeTotalTime;
	}

	/**
	 * The amount by which passed time should be scaled. Used to slow down or speed up the animation. Defaults to 1.
	 */
	public double getTimeScale()
	{
		return _timeScale;
	}

	/**
	 * playTimes Play times(0:loop forever, 1~+∞:play times, -1~-∞:will fade animation after play complete).
	 */
	public int getPlayTimes()
	{
		return _playTimes;
	}
}
