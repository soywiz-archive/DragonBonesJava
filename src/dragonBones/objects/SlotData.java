package dragonBones.objects;

/** @private */
public final class SlotData
{
	public String name;
	public String parent;
	public double zOrder;
    public String blendMode;
	public int displayIndex;

	private ArrayList<DisplayData> _displayDataList;

	public SlotData()
	{
		_displayDataList = new ArrayList<DisplayData>(0, true);
		zOrder = 0;
	}

	public void dispose()
	{
		_displayDataList.fixed = false;
		_displayDataList.length = 0;
	}

	public void addDisplayData(DisplayData displayData)
	{
		if(!displayData)
		{
			throw new ArgumentError();
		}
		if (_displayDataList.indexOf(displayData) < 0)
		{
			_displayDataList.fixed = false;
			_displayDataList[_displayDataList.length] = displayData;
			_displayDataList.fixed = true;
		}
		else
		{
			throw new ArgumentError();
		}
	}

	public DisplayData getDisplayData(String displayName)
	{
		int i = _displayDataList.size();
		while(i -- > 0)
		{
			if(_displayDataList[i].name == displayName)
			{
				return _displayDataList[i];
			}
		}

		return null;
	}

	public ArrayList<DisplayData> getDisplayDataList()
	{
		return _displayDataList;
	}
}
