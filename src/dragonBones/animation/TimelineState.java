package dragonBones.animation;
import dragonBones.Armature;
import dragonBones.objects.CurveData;
import flash.geom.Point;

import dragonBones.Bone;
import dragonBones.core.dragonBones_internal;
import dragonBones.objects.DBTransform;
import dragonBones.objects.Frame;
import dragonBones.objects.TransformFrame;
import dragonBones.objects.TransformTimeline;
import dragonBones.utils.TransformUtil;
import dragonBones.utils.MathUtil;

import java.util.ArrayList;

//use namespace dragonBones_internal;

/** @private */
public final class TimelineState
{
	private static final double HALF_PI = Math.PI * 0.5;
	private static final double DOUBLE_PI = Math.PI * 2;

	private static ArrayList<TimelineState> _pool = new ArrayList<TimelineState>();

	/** @private */
	static TimelineState borrowObject()
	{
		if(_pool.size() == 0)
		{
			return new TimelineState();
		}
		return _pool.pop();
	}

	/** @private */
	static void returnObject(TimelineState timeline)
	{
		if(_pool.indexOf(timeline) < 0)
		{
			_pool[_pool.size()] = timeline;
		}

		timeline.clear();
	}

	/** @private */
	static void clear()
	{
		int i = _pool.size();
		while(i -- > 0)
		{
			_pool[i].clear();
		}
		_pool.clear();
	}

	public String name;

	/** @private */
	public double _weight;

	/** @private */
	public DBTransform _transform;

	/** @private */
	public Point _pivot;

	/** @private */
	public boolean _isComplete;

	/** @private */
	public AnimationState _animationState;

	private int _totalTime; //duration

	private int _currentTime;
	private int _lastTime;
	private int _currentFrameIndex;
	private int _currentFramePosition;
	private int _currentFrameDuration;

	private double _tweenEasing;
	private CurveData _tweenCurve;
	private boolean _tweenTransform;
	private boolean _tweenScale;

	private double _rawAnimationScale;

	//-1: frameLength>1, 0:frameLength==0, 1:frameLength==1
	private int _updateMode;

	private Armature _armature;
	private Animation _animation;
	private Bone _bone;

	private TransformTimeline _timelineData;
	private DBTransform _originTransform;
	private Point _originPivot;

	private DBTransform _durationTransform;
	private Point _durationPivot;


	public TimelineState()
	{
		_transform = new DBTransform();
		_pivot = new Point();

		_durationTransform = new DBTransform();
		_durationPivot = new Point();
	}

	private void clear()
	{
		if(_bone != null)
		{
			_bone.removeState(this);
			_bone = null;
		}
		_armature = null;
		_animation = null;
		_animationState = null;
		_timelineData = null;
		_originTransform = null;
		_originPivot = null;
	}

//动画开始结束
	/** @private */
	void fadeIn(Bone bone, AnimationState animationState, TransformTimeline timelineData)
	{
		_bone = bone;
		_armature = _bone.getArmature();
		_animation = _armature.getAnimation();
		_animationState = animationState;
		_timelineData = timelineData;
		_originTransform = _timelineData.originTransform;
		_originPivot = _timelineData.originPivot;

		name = timelineData.name;

		_totalTime = _timelineData.duration;
		_rawAnimationScale = _animationState.getClip().scale;

		_isComplete = false;
		_tweenTransform = false;
		_tweenScale = false;
		_currentFrameIndex = -1;
		_currentTime = -1;
		_tweenEasing = Double.NaN;
		_weight = 1;

		_transform.x = 0;
		_transform.y = 0;
		_transform.scaleX = 1;
		_transform.scaleY = 1;
		_transform.skewX = 0;
		_transform.skewY = 0;
		_pivot.x = 0;
		_pivot.y = 0;

		_durationTransform.x = 0;
		_durationTransform.y = 0;
		_durationTransform.scaleX = 1;
		_durationTransform.scaleY = 1;
		_durationTransform.skewX = 0;
		_durationTransform.skewY = 0;
		_durationPivot.x = 0;
		_durationPivot.y = 0;

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

		_bone.addState(this);
	}

	/** @private */
	void fadeOut()
	{
		_transform.skewX = TransformUtil.formatRadian(_transform.skewX);
		_transform.skewY = TransformUtil.formatRadian(_transform.skewY);
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
			currentTime -= Math.floor(currentTime / _totalTime) * _totalTime;

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
				currentTime -= Math.floor(currentTime / _totalTime) * _totalTime;
			}
		}

		if(_currentTime != currentTime)
		{
			_lastTime = _currentTime;
			_currentTime = currentTime;

			ArrayList<Frame> frameList = _timelineData.getFrameList();
			TransformFrame prevFrame = null;
			TransformFrame currentFrame = null;

			for (int i = 0, l = _timelineData.getFrameList().size(); i < l; ++i)
			{
				if(_currentFrameIndex < 0)
				{
					_currentFrameIndex = 0;
				}
				else if(_currentTime < _currentFramePosition || _currentTime >= _currentFramePosition + _currentFrameDuration || _currentTime < _lastTime)
				{
					_currentFrameIndex ++;
					_lastTime = _currentTime;
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
				currentFrame = (TransformFrame) frameList.get(_currentFrameIndex);

				if(prevFrame != null)
				{
					_bone.arriveAtFrame(prevFrame, this, _animationState, true);
				}

				_currentFrameDuration = currentFrame.duration;
				_currentFramePosition = currentFrame.position;
				prevFrame = currentFrame;
			}

			if(currentFrame != null)
			{
				_bone.arriveAtFrame(currentFrame, this, _animationState, false);

				updateToNextFrame(currentPlayTimes);
			}
			updateTween();
		}
	}

	private void updateToNextFrame(int currentPlayTimes)
	{
		int nextFrameIndex = _currentFrameIndex + 1;
		if(nextFrameIndex >= _timelineData.getFrameList().size())
		{
			nextFrameIndex = 0;
		}
		TransformFrame currentFrame = (TransformFrame) _timelineData.getFrameList().get(_currentFrameIndex);
		TransformFrame nextFrame = (TransformFrame) _timelineData.getFrameList().get(nextFrameIndex);
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
//			else if(currentFrame.displayIndex < 0 || nextFrame.displayIndex < 0)
//			{
//				_tweenEasing = NaN;
//				tweenEnabled = false;
//			}
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
			//transform
			_durationTransform.x = nextFrame.transform.x - currentFrame.transform.x;
			_durationTransform.y = nextFrame.transform.y - currentFrame.transform.y;
			_durationTransform.skewX = nextFrame.transform.skewX - currentFrame.transform.skewX;
			_durationTransform.skewY = nextFrame.transform.skewY - currentFrame.transform.skewY;

			_durationTransform.scaleX = nextFrame.transform.scaleX - currentFrame.transform.scaleX + nextFrame.scaleOffset.x;
			_durationTransform.scaleY = nextFrame.transform.scaleY - currentFrame.transform.scaleY + nextFrame.scaleOffset.y;
			_durationTransform.normalizeRotation();
			if(nextFrameIndex == 0)
			{
				_durationTransform.skewX = TransformUtil.formatRadian(_durationTransform.skewX);
				_durationTransform.skewY = TransformUtil.formatRadian(_durationTransform.skewY);
			}

			_durationPivot.x = nextFrame.pivot.x - currentFrame.pivot.x;
			_durationPivot.y = nextFrame.pivot.y - currentFrame.pivot.y;

			if(
				_durationTransform.x != 0 ||
				_durationTransform.y != 0 ||
				_durationTransform.skewX != 0 ||
				_durationTransform.skewY != 0 ||
				_durationTransform.scaleX != 0 ||
				_durationTransform.scaleY != 0 ||
				_durationPivot.x != 0 ||
				_durationPivot.y != 0
			)
			{
				_tweenTransform = true;
				_tweenScale = currentFrame.tweenScale;
			}
			else
			{
				_tweenTransform = false;
				_tweenScale = false;
			}

		}
		else
		{
			_tweenTransform = false;
			_tweenScale = false;
		}

		if(!_tweenTransform)
		{
			if(_animationState.additiveBlending)
			{
				_transform.x = currentFrame.transform.x;
				_transform.y = currentFrame.transform.y;
				_transform.skewX = currentFrame.transform.skewX;
				_transform.skewY = currentFrame.transform.skewY;
				_transform.scaleX = currentFrame.transform.scaleX;
				_transform.scaleY = currentFrame.transform.scaleY;

				_pivot.x = currentFrame.pivot.x;
				_pivot.y = currentFrame.pivot.y;
			}
			else
			{
				_transform.x = _originTransform.x + currentFrame.transform.x;
				_transform.y = _originTransform.y + currentFrame.transform.y;
				_transform.skewX = _originTransform.skewX + currentFrame.transform.skewX;
				_transform.skewY = _originTransform.skewY + currentFrame.transform.skewY;
				_transform.scaleX = _originTransform.scaleX * currentFrame.transform.scaleX;
				_transform.scaleY = _originTransform.scaleY * currentFrame.transform.scaleY;

				_pivot.x = _originPivot.x + currentFrame.pivot.x;
				_pivot.y = _originPivot.y + currentFrame.pivot.y;
			}

			_bone.invalidUpdate();
		}
		else if(!_tweenScale)
		{
			if(_animationState.additiveBlending)
			{
				_transform.scaleX = currentFrame.transform.scaleX;
				_transform.scaleY = currentFrame.transform.scaleY;
			}
			else
			{
				_transform.scaleX = _originTransform.scaleX * currentFrame.transform.scaleX;
				_transform.scaleY = _originTransform.scaleY * currentFrame.transform.scaleY;
			}
		}
	}

	private void updateTween()
	{
		TransformFrame currentFrame = (TransformFrame) _timelineData.getFrameList().get(_currentFrameIndex);
		if(_tweenTransform)
		{
			double progress = (_currentTime - _currentFramePosition) / _currentFrameDuration;
			if (_tweenCurve != null)
			{
				progress = _tweenCurve.getValueByProgress(progress);
			}
			else if(_tweenEasing != 0)
			{
				progress = MathUtil.getEaseValue(progress, _tweenEasing);
			}

			DBTransform currentTransform = currentFrame.transform;
			Point currentPivot = currentFrame.pivot;
			if(_animationState.additiveBlending)
			{
				//additive blending
				_transform.x = currentTransform.x + _durationTransform.x * progress;
				_transform.y = currentTransform.y + _durationTransform.y * progress;
				_transform.skewX = currentTransform.skewX + _durationTransform.skewX * progress;
				_transform.skewY = currentTransform.skewY + _durationTransform.skewY * progress;
				if(_tweenScale)
				{
					_transform.scaleX = currentTransform.scaleX + _durationTransform.scaleX * progress;
					_transform.scaleY = currentTransform.scaleY + _durationTransform.scaleY * progress;
				}

				_pivot.x = currentPivot.x + _durationPivot.x * progress;
				_pivot.y = currentPivot.y + _durationPivot.y * progress;
			}
			else
			{
				//normal blending
				_transform.x = _originTransform.x + currentTransform.x + _durationTransform.x * progress;
				_transform.y = _originTransform.y + currentTransform.y + _durationTransform.y * progress;
				_transform.skewX = _originTransform.skewX + currentTransform.skewX + _durationTransform.skewX * progress;
				_transform.skewY = _originTransform.skewY + currentTransform.skewY + _durationTransform.skewY * progress;
				if(_tweenScale)
				{
					_transform.scaleX = _originTransform.scaleX * currentTransform.scaleX + _durationTransform.scaleX * progress;
					_transform.scaleY = _originTransform.scaleY * currentTransform.scaleY + _durationTransform.scaleY * progress;
				}

				_pivot.x = _originPivot.x + currentPivot.x + _durationPivot.x * progress;
				_pivot.y = _originPivot.y + currentPivot.y + _durationPivot.y * progress;
			}

			_bone.invalidUpdate();
		}
	}

	private void updateSingleFrame()
	{
		TransformFrame currentFrame = (TransformFrame) _timelineData.getFrameList().get(0);
		_bone.arriveAtFrame(currentFrame, this, _animationState, false);
		_isComplete = true;
		_tweenEasing = Double.NaN;
		_tweenTransform = false;
		_tweenScale = false;
		//_tweenColor = false;
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
		if(_animationState.additiveBlending)
		{
			_transform.x = currentFrame.transform.x;
			_transform.y = currentFrame.transform.y;
			_transform.skewX = currentFrame.transform.skewX;
			_transform.skewY = currentFrame.transform.skewY;
			_transform.scaleX = currentFrame.transform.scaleX;
			_transform.scaleY = currentFrame.transform.scaleY;

			_pivot.x = currentFrame.pivot.x;
			_pivot.y = currentFrame.pivot.y;
		}
		else
		{
			_transform.x = _originTransform.x + currentFrame.transform.x;
			_transform.y = _originTransform.y + currentFrame.transform.y;
			_transform.skewX = _originTransform.skewX + currentFrame.transform.skewX;
			_transform.skewY = _originTransform.skewY + currentFrame.transform.skewY;
			_transform.scaleX = _originTransform.scaleX * currentFrame.transform.scaleX;
			_transform.scaleY = _originTransform.scaleY * currentFrame.transform.scaleY;

			_pivot.x = _originPivot.x + currentFrame.pivot.x;
			_pivot.y = _originPivot.y + currentFrame.pivot.y;
		}

		_bone.invalidUpdate();
	}


}
