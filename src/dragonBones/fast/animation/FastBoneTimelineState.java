package dragonBones.fast.animation;

import dragonBones.objects.CurveData;
import dragonBones.utils.ArrayListUtils;
import flash.geom.Point;

import dragonBones.fast.FastBone;
import dragonBones.objects.DBTransform;
import dragonBones.objects.Frame;
import dragonBones.objects.TransformFrame;
import dragonBones.objects.TransformTimeline;
import dragonBones.utils.MathUtil;
import dragonBones.utils.TransformUtil;

import java.util.ArrayList;

//use namespace dragonBones_internal;

public class FastBoneTimelineState
{
	private static ArrayList<FastBoneTimelineState> _pool = new ArrayList<>();

	/** @private */
	static FastBoneTimelineState borrowObject()
	{
		if(_pool.size() == 0)
		{
			return new FastBoneTimelineState();
		}
		return ArrayListUtils.pop(_pool);
	}

	/** @private */
	static void returnObject(FastBoneTimelineState timeline)
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
	private int _totalTime; //duration
	private int _currentTime;
	private int _lastTime;
	private int _currentFrameIndex;
	private int _currentFramePosition;
	private int _currentFrameDuration;

	private FastBone _bone;
	private TransformTimeline _timelineData;
	private DBTransform _durationTransform;

	private boolean _tweenTransform;
	private double _tweenEasing;
	private CurveData _tweenCurve;

	private int _updateMode;
	private DBTransform _transformToFadein;
	private Point _durationPivot;
	private Point _originPivot;
	/** @private */
	FastAnimationState _animationState;
	/** @private */
	boolean _isComplete;
	/** @private */
	public DBTransform _transform;
	/** @private */
	public Point _pivot;


	public FastBoneTimelineState()
	{
		_transform = new DBTransform();
		_durationTransform = new DBTransform();
		_transformToFadein = new DBTransform();
		_pivot = new Point();
		_durationPivot = new Point();
	}

	private void clear()
	{
		if(_bone != null)
		{
			_bone._timelineState = null;
			_bone = null;
		}
		_animationState = null;
		_timelineData = null;
		_originPivot = null;
	}

	/** @private */
	void fadeIn(FastBone bone, FastAnimationState animationState, TransformTimeline timelineData)
	{
		_bone = bone;
		_animationState = animationState;
		_timelineData = timelineData;

		name = timelineData.name;

		_totalTime = _timelineData.duration;

		_isComplete = false;

		_tweenTransform = false;
		_currentFrameIndex = -1;
		_currentTime = -1;
		_tweenEasing = Double.NaN;

		_durationPivot.x = 0;
		_durationPivot.y = 0;
		_pivot.x = 0;
		_pivot.y = 0;
		_originPivot = _timelineData.originPivot;
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


		if(animationState._fadeTotalTime>0)
		{
			Point pivotToFadein;
			if(_bone._timelineState != null)
			{
				_transformToFadein.copy(_bone._timelineState._transform);

			}
			else
			{
				_transformToFadein = new DBTransform();
//					_pivotToFadein = new Point();
			}
			TransformFrame firstFrame = (TransformFrame)(_timelineData.getFrameList().get(0));
			_durationTransform.copy(firstFrame.transform);
			_durationTransform.minus(this._transformToFadein);
		}

		_bone._timelineState = this;
	}

	/** @private */
	void updateFade(double progress)
	{
		_transform.x = _transformToFadein.x + _durationTransform.x * progress;
		_transform.y = _transformToFadein.y + _durationTransform.y * progress;
		_transform.scaleX = _transformToFadein.scaleX * (1 + (_durationTransform.scaleX -1 ) * progress);
		_transform.scaleY = _transformToFadein.scaleX * (1 + (_durationTransform.scaleY -1 ) * progress);
		_transform.setRotation(_transformToFadein.getRotation() + _durationTransform.getRotation() * progress);

		_bone.invalidUpdate();
	}

	/** @private */
	void update(double progress)
	{
		if(_updateMode == 1)
		{
			_updateMode = 0;
			updateSingleFrame();

		}
		else if(_updateMode == -1)
		{
			updateMultipleFrame(progress);
		}
	}

	private void updateSingleFrame()
	{
		TransformFrame currentFrame = (TransformFrame)_timelineData.getFrameList().get(0);
		_bone.arriveAtFrame(currentFrame, _animationState);
		_isComplete = true;
		_tweenEasing = Double.NaN;
		_tweenTransform = false;
		_pivot.x = _originPivot.x + currentFrame.pivot.x;
		_pivot.y = _originPivot.y + currentFrame.pivot.y;

		_transform.copy(currentFrame.transform);

		_bone.invalidUpdate();
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
				currentFrame = (TransformFrame)frameList.get(_currentFrameIndex);

				if(prevFrame != null)
				{
					_bone.arriveAtFrame(prevFrame, _animationState);
				}

				_currentFrameDuration = currentFrame.duration;
				_currentFramePosition = currentFrame.position;
				prevFrame = currentFrame;
			}

			if(currentFrame != null)
			{
				_bone.arriveAtFrame(currentFrame, _animationState);
				updateToNextFrame(currentPlayTimes);
			}

			if(_tweenTransform)
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
		TransformFrame currentFrame = (TransformFrame)_timelineData.getFrameList().get(_currentFrameIndex);
		TransformFrame nextFrame = (TransformFrame)_timelineData.getFrameList().get(nextFrameIndex);
		boolean tweenEnabled = false;
		if(nextFrameIndex == 0 &&( _animationState.getPlayTimes() != 0 &&
									_animationState.getCurrentPlayTimes() >= _animationState.getPlayTimes() &&
									((_currentFramePosition + _currentFrameDuration) / _totalTime + currentPlayTimes - _timelineData.offset)* _timelineData.scale > 0.999999
			))
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
			//transform
			_durationTransform.x = nextFrame.transform.x - currentFrame.transform.x;
			_durationTransform.y = nextFrame.transform.y - currentFrame.transform.y;
			_durationTransform.skewX = nextFrame.transform.skewX - currentFrame.transform.skewX;
			_durationTransform.skewY = nextFrame.transform.skewY - currentFrame.transform.skewY;

			_durationTransform.scaleX = nextFrame.transform.scaleX - currentFrame.transform.scaleX + nextFrame.scaleOffset.x;
			_durationTransform.scaleY = nextFrame.transform.scaleY - currentFrame.transform.scaleY + nextFrame.scaleOffset.y;
			_durationTransform.normalizeRotation();

			_durationPivot.x = nextFrame.pivot.x - currentFrame.pivot.x;
			_durationPivot.y = nextFrame.pivot.y - currentFrame.pivot.y;

			if(nextFrameIndex == 0)
			{
				_durationTransform.skewX = TransformUtil.formatRadian(_durationTransform.skewX);
				_durationTransform.skewY = TransformUtil.formatRadian(_durationTransform.skewY);
			}

			if(
				_durationTransform.x != 0 ||
				_durationTransform.y != 0 ||
				_durationTransform.skewX != 0 ||
				_durationTransform.skewY != 0 ||
				_durationTransform.scaleX != 1 ||
				_durationTransform.scaleY != 1 ||
				_durationPivot.x != 0 ||
				_durationPivot.y != 0
			)
			{
				_tweenTransform = true;
			}
			else
			{
				_tweenTransform = false;
			}

		}
		else
		{
			_tweenTransform = false;
		}

		if(!_tweenTransform)
		{
			_transform.copy(currentFrame.transform);
			_pivot.x = _originPivot.x + currentFrame.pivot.x;
			_pivot.y = _originPivot.y + currentFrame.pivot.y;
			_bone.invalidUpdate();
		}
	}

	private void updateTween()
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

		TransformFrame currentFrame = (TransformFrame)_timelineData.getFrameList().get(_currentFrameIndex);

		DBTransform currentTransform = currentFrame.transform;
		Point currentPivot = currentFrame.pivot;
		//normal blending
		_transform.x = currentTransform.x + _durationTransform.x * progress;
		_transform.y = currentTransform.y + _durationTransform.y * progress;
		_transform.skewX = currentTransform.skewX + _durationTransform.skewX * progress;
		_transform.skewY = currentTransform.skewY + _durationTransform.skewY * progress;
		_transform.scaleX = currentTransform.scaleX + _durationTransform.scaleX * progress;
		_transform.scaleY = currentTransform.scaleY + _durationTransform.scaleY * progress;
		_pivot.x = currentPivot.x + _durationPivot.x * progress;
		_pivot.y = currentPivot.y + _durationPivot.y * progress;
		_bone.invalidUpdate();
	}
}
