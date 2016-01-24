package dragonBones.objects;

import flash.errors.ArgumentError;

import java.util.ArrayList;
import java.util.Objects;

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
		_displayDataList = new ArrayList<DisplayData>();
		zOrder = 0;
	}

	public void dispose()
	{
		_displayDataList.clear();
	}

	public void addDisplayData(DisplayData displayData)
	{
		if(displayData == null)
		{
			throw new ArgumentError();
		}
		if (_displayDataList.indexOf(displayData) < 0)
		{
			_displayDataList.set(_displayDataList.size(), displayData);
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
			if(Objects.equals(_displayDataList.get(i).name, displayName))
			{
				return _displayDataList.get(i);
			}
		}

		return null;
	}

	public ArrayList<DisplayData> getDisplayDataList()
	{
		return _displayDataList;
	}
}
