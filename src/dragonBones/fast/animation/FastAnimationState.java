package dragonBones.fast.animation;

import dragonBones.cache.AnimationCache;
import dragonBones.core.IAnimationState;
import dragonBones.core.IName;
import dragonBones.events.AnimationEvent;
import dragonBones.fast.FastArmature;
import dragonBones.fast.FastBone;
import dragonBones.fast.FastSlot;
import dragonBones.objects.AnimationData;
import dragonBones.objects.Frame;
import dragonBones.objects.SlotTimeline;
import dragonBones.objects.TransformTimeline;

import java.util.ArrayList;

//use namespace dragonBones_internal;

public class FastAnimationState implements IAnimationState, IName
{

	public AnimationCache animationCache;
	/**
	 * If auto genterate tween between keyframes.
	 */
	public boolean autoTween;
	private double _progress;

	FastArmature _armature;

	private ArrayList<FastBoneTimelineState> _boneTimelineStateList = new ArrayList<FastBoneTimelineState>();
	private ArrayList<FastSlotTimelineState> _slotTimelineStateList = new ArrayList<FastSlotTimelineState>();
	public AnimationData animationData;

	public String name;
	public String getName() { return name; }
	private double _time;//秒
	private int _currentFrameIndex;
	private int _currentFramePosition;
	private int _currentFrameDuration;

	private int _currentPlayTimes;
	private int _totalTime;//毫秒
	private int _currentTime;
	private int _lastTime;

	private boolean _isComplete;
	private boolean _isPlaying;
	private double _timeScale;
	private int _playTimes;

	private boolean _fading = false;
	private boolean _listenCompleteEvent;
	private boolean _listenLoopCompleteEvent;

	double _fadeTotalTime;


	public FastAnimationState()
	{
	}

	public void dispose()
	{
		resetTimelineStateList();
		_armature = null;
	}

	/**
	 * Play the current animation. 如果动画已经播放完毕, 将不会继续播放.
	 */
	public FastAnimationState play()
	{
		_isPlaying = true;
		return this;
	}

	/**
	 * Stop playing current animation.
	 */
	public FastAnimationState stop()
	{
		_isPlaying = false;
		return this;
	}

	public FastAnimationState setCurrentTime(double value)
	{
		if(value < 0 || Double.isNaN(value))
		{
			value = 0;
		}
		_time = value;
		_currentTime = (int)(_time * 1000);
		return this;
	}

	public void resetTimelineStateList()
	{
		int i = _boneTimelineStateList.size();
		while(i -- > 0)
		{
			FastBoneTimelineState.returnObject(_boneTimelineStateList.get(i));
		}
		_boneTimelineStateList.clear();

		i = _slotTimelineStateList.size();
		while(i -- > 0)
		{
			FastSlotTimelineState.returnObject(_slotTimelineStateList.get(i));
		}
		_slotTimelineStateList.clear();
		name = null;
	}

	/** @private */
	void fadeIn(AnimationData aniData, double playTimes, double timeScale, double fadeTotalTime)
	{
		animationData = aniData;

		name = animationData.name;
		_totalTime = animationData.duration;
		autoTween = aniData.autoTween;
		setTimeScale(timeScale);
		setPlayTimes((int)playTimes);

		//reset
		_isComplete = false;
		_currentFrameIndex = -1;
		_currentPlayTimes = -1;
		if(Math.round(_totalTime * animationData.frameRate * 0.001) < 2)
		{
			_currentTime = _totalTime;
		}
		else
		{
			_currentTime = -1;
		}

		_fadeTotalTime = fadeTotalTime * _timeScale;
		_fading = _fadeTotalTime>0;
		//default
		_isPlaying = true;

		_listenCompleteEvent = _armature.hasEventListener(AnimationEvent.COMPLETE);

		if(this._armature.getEnableCache() && animationCache != null && _fading && _boneTimelineStateList != null)
		{
			updateTransformTimeline(getProgress());
		}

		_time = 0;
		_progress = 0;

		updateTimelineStateList();
		hideBones();
		return;
	}

	/**
	 * @private
	 * Update timeline state based on mixing transforms and clip.
	 */
	void updateTimelineStateList()
	{
		resetTimelineStateList();
		String timelineName;
		for (TransformTimeline boneTimeline : animationData.getTimelineList())
		{
			timelineName = boneTimeline.name;
			FastBone bone = _armature.getBone(timelineName);
			if(bone != null)
			{
				FastBoneTimelineState boneTimelineState = FastBoneTimelineState.borrowObject();
				boneTimelineState.fadeIn(bone, this, boneTimeline);
				_boneTimelineStateList.add(boneTimelineState);
			}
		}

		for (SlotTimeline slotTimeline : animationData.getSlotTimelineList())
		{
			timelineName = slotTimeline.name;
			FastSlot slot = _armature.getSlot(timelineName);
			if(slot != null && slot.getDisplayList().size() > 0)
			{
				FastSlotTimelineState slotTimelineState = FastSlotTimelineState.borrowObject();
				slotTimelineState.fadeIn(slot, this, slotTimeline);
				_slotTimelineStateList.add(slotTimelineState);
			}
		}
	}

	/** @private */
	void advanceTime(double passedTime)
	{
		passedTime *= _timeScale;
		if(_fading)
		{
			//计算progress
			_time += passedTime;
			_progress = _time / _fadeTotalTime;
			if(getProgress() >= 1)
			{
				_progress = 0;
				_time = 0;
				_fading = false;
			}
		}

		if(_fading)
		{
			//update boneTimelie
			for (FastBoneTimelineState timeline : _boneTimelineStateList)
			{
				timeline.updateFade(getProgress());
			}
			//update slotTimelie
			for (FastSlotTimelineState slotTimeline : _slotTimelineStateList)
			{
				slotTimeline.updateFade(getProgress());
			}
		}
		else
		{
			advanceTimelinesTime(passedTime);
		}
	}

	private void advanceTimelinesTime(double passedTime)
	{
		if(_isPlaying)
		{
			_time += passedTime;
		}

		//计算是否已经播放完成isThisComplete

		boolean startFlg = false;
		boolean loopCompleteFlg = false;
		boolean completeFlg = false;
		boolean isThisComplete = false;
		int currentPlayTimes = 0;
		int currentTime = (int)(_time * 1000);
		if( _playTimes == 0 || //无限循环
			currentTime < _playTimes * _totalTime) //没有播放完毕
		{
			isThisComplete = false;

			_progress = currentTime / _totalTime;
			currentPlayTimes = (int)Math.ceil(getProgress());
			if (currentPlayTimes == 0) currentPlayTimes = 1;
			_progress -= Math.floor(getProgress());
			currentTime %= _totalTime;
		}
		else
		{
			currentPlayTimes = _playTimes;
			currentTime = _totalTime;
			isThisComplete = true;
			_progress = 1;
		}

		_isComplete = isThisComplete;

		if(this.isUseCache())
		{
			animationCache.update(getProgress());
		}
		else
		{
			updateTransformTimeline(getProgress());
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
			if (_isComplete)
			{
				completeFlg = true;
			}
			_lastTime = _currentTime;
			_currentTime = currentTime;
			updateMainTimeline(isThisComplete);
		}

		//抛事件
		AnimationEvent event;
		if(startFlg)
		{
			if(_armature.hasEventListener(AnimationEvent.START))
			{
				event = new AnimationEvent(AnimationEvent.START);
				event.animationState = this;
				_armature.addEvent(event);
			}
		}
		if(completeFlg)
		{
			if (_armature.hasEventListener(AnimationEvent.COMPLETE))
			{
				event = new AnimationEvent(AnimationEvent.COMPLETE);
				event.animationState = this;
				_armature.addEvent(event);
			}
		}
		else if(loopCompleteFlg)
		{
			if (_armature.hasEventListener(AnimationEvent.LOOP_COMPLETE))
			{
				event = new AnimationEvent(AnimationEvent.LOOP_COMPLETE);
				event.animationState = this;
				_armature.addEvent(event);
			}

		}
	}

	private void updateTransformTimeline(double progress)
	{
		int i = _boneTimelineStateList.size();
		FastBoneTimelineState boneTimeline;
		FastSlotTimelineState slotTimeline;

		if(_isComplete) // 性能优化
		{
			//update boneTimelie
			while(i-- > 0)
			{
				boneTimeline = _boneTimelineStateList.get(i);
				boneTimeline.update(progress);
				_isComplete = boneTimeline._isComplete && _isComplete;
			}

			i = _slotTimelineStateList.size();

			//update slotTimelie
			while(i-- > 0)
			{
				slotTimeline = _slotTimelineStateList.get(i);
				slotTimeline.update(progress);
				_isComplete = slotTimeline._isComplete && _isComplete;
			}
		}
		else
		{
			//update boneTimelie
			while(i-- > 0)
			{
				boneTimeline = _boneTimelineStateList.get(i);
				boneTimeline.update(progress);
			}

			i = _slotTimelineStateList.size();

			//update slotTimelie
			while(i-- > 0)
			{
				slotTimeline = _slotTimelineStateList.get(i);
				slotTimeline.update(progress);
			}
		}
	}

	private void updateMainTimeline(boolean isThisComplete)
	{
		ArrayList<Frame> frameList = animationData.getFrameList();
		if(frameList.size() > 0)
		{
			Frame prevFrame = null;
			Frame currentFrame = null;
			for (int i = 0, l = animationData.getFrameList().size(); i < l; ++i)
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
					_armature.arriveAtFrame(prevFrame, this);
				}

				_currentFrameDuration = currentFrame.duration;
				_currentFramePosition = currentFrame.position;
				prevFrame = currentFrame;
			}

			if(currentFrame != null)
			{
				_armature.arriveAtFrame(currentFrame, this);
			}
		}
	}

	private void hideBones()
	{
		for (String timelineName : animationData.hideTimelineNameMap)
		{
			FastBone bone = _armature.getBone(timelineName);
			if(bone != null)
			{
				bone.hideSlots();
			}
		}
		for (String slotTimelineName : animationData.hideSlotTimelineNameMap)
		{
			FastSlot slot = _armature.getSlot(slotTimelineName);
			if (slot != null)
			{
				slot.resetToOrigin();
			}
		}
	}

	public FastAnimationState setTimeScale(double value)
	{
		if(Double.isNaN(value) || value == Double.POSITIVE_INFINITY)
		{
			value = 1;
		}
		_timeScale = value;
		return this;
	}

	public FastAnimationState setPlayTimes(int value)
	{
		//如果动画只有一帧  播放一次就可以
		if(Math.round(_totalTime * 0.001 * animationData.frameRate) < 2)
		{
			_playTimes = 1;
		}
		else
		{
			_playTimes = value;
		}
		return this;
	}

	/**
	 * playTimes Play times(0:loop forever, 1~+∞:play times, -1~-∞:will fade animation after play complete).
	 */
	public int getPlayTimes()
	{
		return _playTimes;
	}

	/**
	 * Current animation played times
	 */
	public int getCurrentPlayTimes()
	{
		return _currentPlayTimes < 0 ? 0 : _currentPlayTimes;
	}

	/**
	 * Is animation complete.
	 */
	public boolean isComplete()
	{
		return _isComplete;
	}

	/**
	 * Is animation playing.
	 */
	public boolean isPlaying()
	{
		return (_isPlaying && !_isComplete);
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


	public boolean isUseCache()
	{
		return _armature.getEnableCache() && animationCache != null && !_fading;
	}

	public double getProgress()
	{
		return _progress;
	}
}
