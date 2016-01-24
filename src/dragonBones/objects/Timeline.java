package dragonBones.objects;

import flash.errors.ArgumentError;

import java.util.ArrayList;

public class Timeline
{
	public int duration;
	public double scale;

	private ArrayList<Frame> _frameList;

	public Timeline()
	{
		_frameList = new ArrayList<Frame>();
		duration = 0;
		scale = 1;
	}

	public void dispose()
	{
		int i = _frameList.size();
		while(i -- > 0)
		{
			_frameList.get(i).dispose();
		}
		_frameList.clear();
		_frameList = null;
	}

	public void addFrame(Frame frame)
	{
		if(frame == null)
		{
			throw new ArgumentError();
		}

		if(_frameList.indexOf(frame) < 0)
		{
			_frameList.set(_frameList.size(), frame);
		}
		else
		{
			throw new ArgumentError();
		}
	}

	public ArrayList<Frame> getFrameList()
	{
		return _frameList;
	}
}
