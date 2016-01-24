package dragonBones.animation;
import dragonBones.utils.ArrayListUtils;

import java.util.ArrayList;

/**
 * A WorldClock instance lets you conveniently update many number of Armature instances at once. You can add/remove Armature instance and set a global timescale that will apply to all registered Armature instance animations.
 * @see dragonBones.Armature
 * @see dragonBones.animation.Animation
 */
public final class WorldClock implements IAnimatable
{
	/**
	 * A global static WorldClock instance ready to use.
	 */
	public static WorldClock clock = new WorldClock();

	private ArrayList<IAnimatable> _animatableList;

	private double _time;
	public double getTime()
	{
		return _time;
	}

	private double _timeScale;
	/**
	 * The time scale to apply to the number of second passed to the advanceTime() method.
	 * A Number to use as a time scale.
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

	static private double getTimer() {
		return (double)System.currentTimeMillis();
	}

	public WorldClock()
	{
		this(-1, 1);
	}

	public WorldClock(double time)
	{
		this(time, 1);
	}

	/**
	 * Creates a new WorldClock instance. (use the static var WorldClock.clock instead).
	 */
	public WorldClock(double time, double timeScale)
	{
		_time = time >= 0?time:getTimer() * 0.001;
		_timeScale = Double.isNaN(timeScale)?1:timeScale;
		_animatableList = new ArrayList<IAnimatable>();
	}

	/**
	 * Returns true if the IAnimatable instance is contained by WorldClock instance.
	 * An IAnimatable instance (Armature or custom)
	 * @return true if the IAnimatable instance is contained by WorldClock instance.
	 */
	public boolean contains(IAnimatable animatable)
	{
		return _animatableList.contains(animatable);
	}

	/**
	 * Add a IAnimatable instance (Armature or custom) to this WorldClock instance.
	 * An IAnimatable instance (Armature, WorldClock or custom)
	 */
	public void add(IAnimatable animatable)
	{
		if (animatable != null && _animatableList.indexOf(animatable) == -1)
		{
			_animatableList.add(animatable);
		}
	}

	/**
	 * Remove a IAnimatable instance (Armature or custom) from this WorldClock instance.
	 * An IAnimatable instance (Armature or custom)
	 */
	public void remove(IAnimatable animatable)
	{
		int index = _animatableList.indexOf(animatable);
		if (index >= 0)
		{
			_animatableList.set(index, null);
		}
	}

	/**
	 * Remove all IAnimatable instance (Armature or custom) from this WorldClock instance.
	 */
	public void clear()
	{
		_animatableList.clear();
	}

	/**
	 * Update all registered IAnimatable instance animations using this method typically in an ENTERFRAME Event or with a Timer.
	 * The amount of second to move the playhead ahead.
	 */
	public void advanceTime(double passedTime)
	{
		if(passedTime < 0)
		{
			passedTime = getTimer() * 0.001 - _time;
		}
		_time += passedTime;

		passedTime *= _timeScale;

		int length = _animatableList.size();
		if(length == 0)
		{
			return;
		}
		int currentIndex = 0;

		int i = 0;
		for(i = 0;i < length; i++)
		{
			IAnimatable animatable = _animatableList.get(i);
			if(animatable != null)
			{
				if(currentIndex != i)
				{
					_animatableList.set(currentIndex, animatable);
					_animatableList.set(i, null);
				}
				animatable.advanceTime(passedTime);
				currentIndex ++;
			}
		}

		if (currentIndex != i)
		{
			length = _animatableList.size();
			while(i < length)
			{
				_animatableList.set(currentIndex++, _animatableList.get(i++));
			}

			ArrayListUtils.setLength(_animatableList, currentIndex, null);
		}
	}
}
