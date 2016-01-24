package dragonBones.fast.animation;

import dragonBones.objects.CurveData;
import dragonBones.utils.ArrayListUtils;
import flash.geom.ColorTransform;

import dragonBones.fast.FastArmature;
import dragonBones.fast.FastSlot;
import dragonBones.objects.Frame;
import dragonBones.objects.SlotFrame;
import dragonBones.objects.SlotTimeline;
import dragonBones.utils.ColorTransformUtil;
import dragonBones.utils.MathUtil;

import java.util.ArrayList;

//use namespace dragonBones_internal;

/** @private */
public final class FastSlotTimelineState
{
	private static final double HALF_PI = Math.PI * 0.5;
	private static final double DOUBLE_PI = Math.PI * 2;

	private static ArrayList<FastSlotTimelineState> _pool = new ArrayList<FastSlotTimelineState>();

	/** @private */
	static FastSlotTimelineState borrowObject()
	{
		if(_pool.size() == 0)
		{
			return new FastSlotTimelineState();
		}
		return ArrayListUtils.pop(_pool);
	}

	/** @private */
	static void returnObject(FastSlotTimelineState timeline)
	{
		if(_pool.indexOf(timeline) < 0)
		{
			_pool.set(_pool.size(), timeline);
		}

		timeline.clear();
	}

	/** @private */
	static void clearStatic()
	{
		int i = _pool.size();
		while(i -- > 0)
		{
			_pool.get(i).clear();
		}
		_pool.clear();
	}



	public String name;

	/** @private */
	double _weight;

	//TO DO 干什么用的
	/** @private */
	boolean _blendEnabled;

	/** @private */
	boolean _isComplete;

	/** @private */
	FastAnimationState _animationState;

	int _totalTime; //duration

	int _currentTime;
	int _currentFrameIndex;
	int _currentFramePosition;
	int _currentFrameDuration;

	double _tweenEasing;
	CurveData _tweenCurve;
	boolean _tweenColor;
	boolean _colorChanged;

	//-1: frameLength>1, 0:frameLength==0, 1:frameLength==1
	private int _updateMode;

	private FastArmature _armature;
	private FastAnimation _animation;
	private FastSlot _slot;

	private SlotTimeline _timelineData;
	private ColorTransform _durationColor;



	public FastSlotTimelineState()
	{
		_durationColor = new ColorTransform();
	}

	private void clear()
	{
		_slot = null;
		_armature = null;
		_animation = null;
		_animationState = null;
		_timelineData = null;
	}

//动画开始结束
	/** @private */
	void fadeIn(FastSlot slot, FastAnimationState animationState, SlotTimeline timelineData)
	{
		_slot = slot;
		_armature = _slot.armature;
		_animation = (FastAnimation)_armature.getAnimation();
		_animationState = animationState;
		_timelineData = timelineData;

		name = timelineData.name;

		_totalTime = _timelineData.duration;

		_isComplete = false;
		_blendEnabled = false;
		_tweenColor = false;
		_currentFrameIndex = -1;
		_currentTime = -1;
		_tweenEasing = Double.NaN;
		_weight = 1;

		switch(_timelineData.getFrameList().size())
		{
			case 0:
				_updateMode = 0;
				break;

			case 1:
				_updateMode = 1;
				break;

			default:
				_updateMode = -1;
				break;
		}
	}

//动画进行中

	/** @private */
	void updateFade(double progress)
	{
	}

	/** @private */
	void update(double progress)
	{
		if(_updateMode == -1)
		{
			updateMultipleFrame(progress);
		}
		else if(_updateMode == 1)
		{
			_updateMode = 0;
			updateSingleFrame();
		}
	}

	private void updateMultipleFrame(double progress)
	{
		int currentPlayTimes = 0;
		progress /= _timelineData.scale;
		progress += _timelineData.offset;

		int currentTime = (int)(_totalTime * progress);
		int playTimes = _animationState.getPlayTimes();
		if(playTimes == 0)
		{
			_isComplete = false;
			currentPlayTimes = (int)Math.ceil(Math.abs(currentTime) / _totalTime);
			if (currentPlayTimes == 0) currentPlayTimes = 1;
			currentTime -= (int)(currentTime / _totalTime) * _totalTime;

			if(currentTime < 0)
			{
				currentTime += _totalTime;
			}
		}
		else
		{
			int totalTimes = playTimes * _totalTime;
			if(currentTime >= totalTimes)
			{
				currentTime = totalTimes;
				_isComplete = true;
			}
			else if(currentTime <= -totalTimes)
			{
				currentTime = -totalTimes;
				_isComplete = true;
			}
			else
			{
				_isComplete = false;
			}

			if(currentTime < 0)
			{
				currentTime += totalTimes;
			}

			currentPlayTimes = (int)Math.ceil(currentTime / _totalTime);
			if (currentPlayTimes == 0) currentPlayTimes = 1;
			if(_isComplete)
			{
				currentTime = _totalTime;
			}
			else
			{
				currentTime -= (int)(currentTime / _totalTime) * _totalTime;
			}
		}

		if(_currentTime != currentTime)
		{
			_currentTime = currentTime;

			ArrayList<Frame> frameList = _timelineData.getFrameList();
			SlotFrame prevFrame = null;
			SlotFrame currentFrame = null;

			for (int i = 0, l = _timelineData.getFrameList().size(); i < l; ++i)
			{
				if(_currentFrameIndex < 0)
				{
					_currentFrameIndex = 0;
				}
				else if(_currentTime < _currentFramePosition || _currentTime >= _currentFramePosition + _currentFrameDuration)
				{
					_currentFrameIndex ++;
					if(_currentFrameIndex >= frameList.size())
					{
						if(_isComplete)
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
				currentFrame = (SlotFrame) frameList.get(_currentFrameIndex);

				if(prevFrame != null)
				{
					_slot.arriveAtFrame(prevFrame, _animationState);
				}

				_currentFrameDuration = currentFrame.duration;
				_currentFramePosition = currentFrame.position;
				prevFrame = currentFrame;
			}

			if(currentFrame != null)
			{
				_slot.arriveAtFrame(currentFrame, _animationState);

				_blendEnabled = currentFrame.displayIndex >= 0;
				if(_blendEnabled)
				{
					updateToNextFrame(currentPlayTimes);
				}
				else
				{
					_tweenEasing = Double.NaN;
					_tweenColor = false;
				}
			}

			if(_blendEnabled)
			{
				updateTween();
			}
		}
	}

	private void updateToNextFrame(int currentPlayTimes)
	{
		int nextFrameIndex = _currentFrameIndex + 1;
		if(nextFrameIndex >= _timelineData.getFrameList().size())
		{
			nextFrameIndex = 0;
		}
		SlotFrame currentFrame = (SlotFrame) _timelineData.getFrameList().get(_currentFrameIndex);
		SlotFrame nextFrame = (SlotFrame) _timelineData.getFrameList().get(nextFrameIndex);
		boolean tweenEnabled = false;
		if(nextFrameIndex == 0 &&
			(
				_animationState.getPlayTimes() != 0 &&
				_animationState.getCurrentPlayTimes() >= _animationState.getPlayTimes() &&
				((_currentFramePosition + _currentFrameDuration) / _totalTime + currentPlayTimes - _timelineData.offset) * _timelineData.scale > 0.999999
			)
		)
		{
			_tweenEasing = Double.NaN;
			tweenEnabled = false;
		}
		else if(currentFrame.displayIndex < 0 || nextFrame.displayIndex < 0)
		{
			_tweenEasing = Double.NaN;
			tweenEnabled = false;
		}
		else if(_animationState.autoTween)
		{
			_tweenEasing = _animationState.animationData.tweenEasing;
			if(Double.isNaN(_tweenEasing))
			{
				_tweenEasing = currentFrame.tweenEasing;
				_tweenCurve = currentFrame.curve;
				if(Double.isNaN(_tweenEasing))    //frame no tween
				{
					tweenEnabled = false;
				}
				else
				{
					if(_tweenEasing == 10)
					{
						_tweenEasing = 0;
					}
					//_tweenEasing [-1, 0) 0 (0, 1] (1, 2]
					tweenEnabled = true;
				}
			}
			else    //animationData overwrite tween
			{
				//_tweenEasing [-1, 0) 0 (0, 1] (1, 2]
				tweenEnabled = true;
			}
		}
		else
		{
			_tweenEasing = currentFrame.tweenEasing;
			_tweenCurve = currentFrame.curve;
			if(Double.isNaN(_tweenEasing) || _tweenEasing == 10)    //frame no tween
			{
				_tweenEasing = Double.NaN;
				tweenEnabled = false;
			}
			else
			{
				//_tweenEasing [-1, 0) 0 (0, 1] (1, 2]
				tweenEnabled = true;
			}
		}

		if(tweenEnabled)
		{
			if(currentFrame.color != null || nextFrame.color != null)
			{
				ColorTransformUtil.minus(
					nextFrame.color != null ? nextFrame.color : ColorTransformUtil.originalColor,
					currentFrame.color != null ? currentFrame.color : ColorTransformUtil.originalColor,
					_durationColor
				);
				_tweenColor = 	_durationColor.alphaOffset != 0 ||
								_durationColor.redOffset != 0 ||
								_durationColor.greenOffset != 0 ||
								_durationColor.blueOffset != 0 ||
								_durationColor.alphaMultiplier != 0 ||
								_durationColor.redMultiplier != 0 ||
								_durationColor.greenMultiplier != 0 ||
								_durationColor.blueMultiplier != 0;
			}
			else
			{
				_tweenColor = false;
			}
		}
		else
		{
			_tweenColor = false;
		}

		if(!_tweenColor)
		{
			ColorTransform targetColor;
			boolean colorChanged;

			if(currentFrame.getColorChanged())
			{
				targetColor = currentFrame.color;
				colorChanged = true;
			}
			else
			{
				targetColor = ColorTransformUtil.originalColor;
				colorChanged = false;
			}
			if ((_slot._isColorChanged || colorChanged))
			{
				if(	!ColorTransformUtil.isEqual(_slot._colorTransform, targetColor))
				{
					_slot.updateDisplayColor(
						targetColor.alphaOffset,
						targetColor.redOffset,
						targetColor.greenOffset,
						targetColor.blueOffset,
						targetColor.alphaMultiplier,
						targetColor.redMultiplier,
						targetColor.greenMultiplier,
						targetColor.blueMultiplier,
						colorChanged
					);
				}
			}
		}
	}

	private void updateTween()
	{
		SlotFrame currentFrame = (SlotFrame)_timelineData.getFrameList().get(_currentFrameIndex);

		if(_tweenColor)
		{
			double progress = (_currentTime - _currentFramePosition) / _currentFrameDuration;
			if (_tweenCurve != null)
			{
				progress = _tweenCurve.getValueByProgress(progress);
			}
			if(_tweenEasing != 0)
			{
				progress = MathUtil.getEaseValue(progress, _tweenEasing);
			}
			if(currentFrame.color != null)
			{
				_slot.updateDisplayColor(
					currentFrame.color.alphaOffset 		+ _durationColor.alphaOffset 		* progress,
					currentFrame.color.redOffset 		+ _durationColor.redOffset 			* progress,
					currentFrame.color.greenOffset 		+ _durationColor.greenOffset 		* progress,
					currentFrame.color.blueOffset 		+ _durationColor.blueOffset 		* progress,
					currentFrame.color.alphaMultiplier 	+ _durationColor.alphaMultiplier 	* progress,
					currentFrame.color.redMultiplier 	+ _durationColor.redMultiplier 		* progress,
					currentFrame.color.greenMultiplier 	+ _durationColor.greenMultiplier 	* progress,
					currentFrame.color.blueMultiplier	+ _durationColor.blueMultiplier 	* progress,
					true
				);
			}
			else
			{
				_slot.updateDisplayColor(
					_durationColor.alphaOffset 		* progress,
					_durationColor.redOffset 		* progress,
					_durationColor.greenOffset 		* progress,
					_durationColor.blueOffset 		* progress,
					_durationColor.alphaMultiplier 	* progress + 1,
					_durationColor.redMultiplier 	* progress + 1,
					_durationColor.greenMultiplier 	* progress + 1,
					_durationColor.blueMultiplier 	* progress + 1,
					true
				);
			}
		}
	}

	private void updateSingleFrame()
	{
		SlotFrame currentFrame = (SlotFrame) _timelineData.getFrameList().get(0);
		_slot.arriveAtFrame(currentFrame, _animationState);
		_isComplete = true;
		_tweenEasing = Double.NaN;
		_tweenColor = false;

		_blendEnabled = currentFrame.displayIndex >= 0;
		if(_blendEnabled)
		{
			ColorTransform targetColor;
			boolean colorChanged;
			if(currentFrame.getColorChanged())
			{
				targetColor = currentFrame.color;
				colorChanged = true;
			}
			else
			{
				targetColor = ColorTransformUtil.originalColor;
				colorChanged = false;
			}
			if ((_slot._isColorChanged || colorChanged))
			{
				if(	!ColorTransformUtil.isEqual(_slot._colorTransform, targetColor))
				{
					_slot.updateDisplayColor(
						targetColor.alphaOffset,
						targetColor.redOffset,
						targetColor.greenOffset,
						targetColor.blueOffset,
						targetColor.alphaMultiplier,
						targetColor.redMultiplier,
						targetColor.greenMultiplier,
						targetColor.blueMultiplier,
						colorChanged
					);
				}
			}
		}
	}

}
