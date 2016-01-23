package dragonBones.objects;

public class Timeline
{
	public int duration;
	public double scale;

	private ArrayList<Frame> _frameList;

	public Timeline()
	{
		_frameList = new ArrayList<Frame>(0, true);
		duration = 0;
		scale = 1;
	}

	public void dispose()
	{
		int i = _frameList.size();
		while(i -- > 0)
		{
			_frameList[i].dispose();
		}
		_frameList.fixed = false;
		_frameList.length = 0;
		_frameList = null;
	}

	public void addFrame(Frame frame)
	{
		if(!frame)
		{
			throw new ArgumentError();
		}

		if(_frameList.indexOf(frame) < 0)
		{
			_frameList.fixed = false;
			_frameList[_frameList.length] = frame;
			_frameList.fixed = true;
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
