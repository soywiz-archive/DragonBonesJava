package dragonBones.animation;

import dragonBones.objects.CurveData;
import dragonBones.utils.ArrayListUtils;
import flash.geom.ColorTransform;

import dragonBones.Armature;
import dragonBones.Slot;
import dragonBones.objects.Frame;
import dragonBones.objects.SlotFrame;
import dragonBones.objects.SlotTimeline;
import dragonBones.utils.MathUtil;

import java.util.ArrayList;

/** @private */
public final class SlotTimelineState
{
	private static final double HALF_PI = Math.PI * 0.5;
	private static final double DOUBLE_PI = Math.PI * 2;

	private static ArrayList<SlotTimelineState> _pool = new ArrayList<SlotTimelineState>();

	/** @private */
	static SlotTimelineState borrowObject()
	{
		if(_pool.size() == 0)
		{
			return new SlotTimelineState();
		}
		return ArrayListUtils.pop(_pool);
	}

	/** @private */
	static void returnObject(SlotTimelineState timeline)
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
	AnimationState _animationState;

	private int _totalTime; //duration

	private int _currentTime;
	private int _currentFrameIndex;
	private int _currentFramePosition;
	private int _currentFrameDuration;

	private double _tweenEasing;
	private CurveData _tweenCurve;
	private boolean _tweenColor;

	private double _rawAnimationScale;

	//-1: frameLength>1, 0:frameLength==0, 1:frameLength==1
	private int _updateMode;

	private Armature _armature;
	private Animation _animation;
	private Slot _slot;

	private SlotTimeline _timelineData;
	private ColorTransform _durationColor;


	public SlotTimelineState()
	{
		_durationColor = new ColorTransform();
	}

	private void clear()
	{
//			if(_slot)
//			{
//				_slot.removeState(this);
//				_slot = null;
//			}
		_slot = null;
		_armature = null;
		_animation = null;
		_animationState = null;
		_timelineData = null;
	}

//动画开始结束
	/** @private */
	void fadeIn(Slot slot, AnimationState animationState, SlotTimeline timelineData)
	{
		_slot = slot;
		_armature = _slot.getArmature();
		_animation = _armature.getAnimation();
		_animationState = animationState;
		_timelineData = timelineData;

		name = timelineData.name;

		_totalTime = _timelineData.duration;
		_rawAnimationScale = _animationState.getClip().scale;

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

//			_slot.addState(this);
	}

//动画进行中

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
					_slot.arriveAtFrame(prevFrame, this, _animationState, true);
				}

				_currentFrameDuration = currentFrame.duration;
				_currentFramePosition = currentFrame.position;
				prevFrame = currentFrame;
			}

			if(currentFrame != null)
			{
				_slot.arriveAtFrame(currentFrame, this, _animationState, false);

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
		if(
			nextFrameIndex == 0 &&
			(
				!_animationState.lastFrameAutoTween ||
				(
					_animationState.getPlayTimes() != 0 &&
					_animationState.getCurrentPlayTimes() >= _animationState.getPlayTimes() &&
					((_currentFramePosition + _currentFrameDuration) / _totalTime + currentPlayTimes - _timelineData.offset) * _timelineData.scale > 0.999999
				)
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
			_tweenEasing = _animationState.getClip().tweenEasing;
			if(Double.isNaN(_tweenEasing))
			{
				_tweenEasing = currentFrame.tweenEasing;
				_tweenCurve = currentFrame.curve;
				if(Double.isNaN(_tweenEasing) && _tweenCurve == null)    //frame no tween
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
			if((Double.isNaN(_tweenEasing) || _tweenEasing == 10) && _tweenCurve == null)   //frame no tween
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
			if(currentFrame.color != null && nextFrame.color != null)
			{
				_durationColor.alphaOffset = nextFrame.color.alphaOffset - currentFrame.color.alphaOffset;
				_durationColor.redOffset = nextFrame.color.redOffset - currentFrame.color.redOffset;
				_durationColor.greenOffset = nextFrame.color.greenOffset - currentFrame.color.greenOffset;
				_durationColor.blueOffset = nextFrame.color.blueOffset - currentFrame.color.blueOffset;

				_durationColor.alphaMultiplier = nextFrame.color.alphaMultiplier - currentFrame.color.alphaMultiplier;
				_durationColor.redMultiplier = nextFrame.color.redMultiplier - currentFrame.color.redMultiplier;
				_durationColor.greenMultiplier = nextFrame.color.greenMultiplier - currentFrame.color.greenMultiplier;
				_durationColor.blueMultiplier = nextFrame.color.blueMultiplier - currentFrame.color.blueMultiplier;

				if(
					_durationColor.alphaOffset != 0 ||
					_durationColor.redOffset != 0 ||
					_durationColor.greenOffset != 0 ||
					_durationColor.blueOffset != 0 ||
					_durationColor.alphaMultiplier != 0 ||
					_durationColor.redMultiplier != 0 ||
					_durationColor.greenMultiplier != 0 ||
					_durationColor.blueMultiplier != 0
				)
				{
					_tweenColor = true;
				}
				else
				{
					_tweenColor = false;
				}
			}
			else if(currentFrame.color != null)
			{
				_tweenColor = true;
				_durationColor.alphaOffset = -currentFrame.color.alphaOffset;
				_durationColor.redOffset = -currentFrame.color.redOffset;
				_durationColor.greenOffset = -currentFrame.color.greenOffset;
				_durationColor.blueOffset = -currentFrame.color.blueOffset;

				_durationColor.alphaMultiplier = 1 - currentFrame.color.alphaMultiplier;
				_durationColor.redMultiplier = 1 - currentFrame.color.redMultiplier;
				_durationColor.greenMultiplier = 1 - currentFrame.color.greenMultiplier;
				_durationColor.blueMultiplier = 1 - currentFrame.color.blueMultiplier;
			}
			else if(nextFrame.color != null)
			{
				_tweenColor = true;
				_durationColor.alphaOffset = nextFrame.color.alphaOffset;
				_durationColor.redOffset = nextFrame.color.redOffset;
				_durationColor.greenOffset = nextFrame.color.greenOffset;
				_durationColor.blueOffset = nextFrame.color.blueOffset;

				_durationColor.alphaMultiplier = nextFrame.color.alphaMultiplier - 1;
				_durationColor.redMultiplier = nextFrame.color.redMultiplier - 1;
				_durationColor.greenMultiplier = nextFrame.color.greenMultiplier - 1;
				_durationColor.blueMultiplier = nextFrame.color.blueMultiplier - 1;
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

		if(!_tweenColor && _animationState.displayControl)
		{
			if(currentFrame.color != null)
			{
				_slot.updateDisplayColor(
					currentFrame.color.alphaOffset,
					currentFrame.color.redOffset,
					currentFrame.color.greenOffset,
					currentFrame.color.blueOffset,
					currentFrame.color.alphaMultiplier,
					currentFrame.color.redMultiplier,
					currentFrame.color.greenMultiplier,
					currentFrame.color.blueMultiplier,
					true
				);
			}
			else if(_slot._isColorChanged)
			{
				_slot.updateDisplayColor(0, 0, 0, 0, 1, 1, 1, 1, false);
			}

		}
	}

	private void updateTween()
	{
		SlotFrame currentFrame = (SlotFrame) _timelineData.getFrameList().get(_currentFrameIndex);

		if(_tweenColor && _animationState.displayControl)
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
					currentFrame.color.alphaOffset + _durationColor.alphaOffset * progress,
					currentFrame.color.redOffset + _durationColor.redOffset * progress,
					currentFrame.color.greenOffset + _durationColor.greenOffset * progress,
					currentFrame.color.blueOffset + _durationColor.blueOffset * progress,
					currentFrame.color.alphaMultiplier + _durationColor.alphaMultiplier * progress,
					currentFrame.color.redMultiplier + _durationColor.redMultiplier * progress,
					currentFrame.color.greenMultiplier + _durationColor.greenMultiplier * progress,
					currentFrame.color.blueMultiplier + _durationColor.blueMultiplier * progress,
					true
				);
			}
			else
			{
				_slot.updateDisplayColor(
					_durationColor.alphaOffset * progress,
					_durationColor.redOffset * progress,
					_durationColor.greenOffset * progress,
					_durationColor.blueOffset * progress,
					1 + _durationColor.alphaMultiplier * progress,
					1 + _durationColor.redMultiplier * progress,
					1 + _durationColor.greenMultiplier * progress,
					1 + _durationColor.blueMultiplier * progress,
					true
				);
			}
		}
	}

	private void updateSingleFrame()
	{
		SlotFrame currentFrame = (SlotFrame) _timelineData.getFrameList().get(0);
		_slot.arriveAtFrame(currentFrame, this, _animationState, false);
		_isComplete = true;
		_tweenEasing = Double.NaN;
		_tweenColor = false;

		_blendEnabled = currentFrame.displayIndex >= 0;
		if(_blendEnabled)
		{
			/**
			 * <使用绝对数据>
			 * 单帧的timeline，第一个关键帧的transform为0
			 * timeline.originTransform = firstFrame.transform;
			 * eachFrame.transform = eachFrame.transform - timeline.originTransform;
			 * firstFrame.transform == 0;
			 *
			 * <使用相对数据>
			 * 使用相对数据时，timeline.originTransform = 0，第一个关键帧的transform有可能不为 0
			 */
			if(_animationState.displayControl)
			{
				if(currentFrame.color != null)
				{
					_slot.updateDisplayColor(
						currentFrame.color.alphaOffset,
						currentFrame.color.redOffset,
						currentFrame.color.greenOffset,
						currentFrame.color.blueOffset,
						currentFrame.color.alphaMultiplier,
						currentFrame.color.redMultiplier,
						currentFrame.color.greenMultiplier,
						currentFrame.color.blueMultiplier,
						true
					);
				}
				else if(_slot._isColorChanged)
				{
					_slot.updateDisplayColor(0, 0, 0, 0, 1, 1, 1, 1, false);
				}
			}
		}
	}


}
