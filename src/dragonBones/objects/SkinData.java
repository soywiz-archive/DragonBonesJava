package dragonBones.objects;

import flash.errors.ArgumentError;

import java.util.ArrayList;
import java.util.Objects;

/** @private */
final public class SkinData
{
	public String name;

	private ArrayList<SlotData> _slotDataList;

	public SkinData()
	{
		_slotDataList = new ArrayList<SlotData>();
	}

	public void dispose()
	{
		int i = _slotDataList.size();
		while(i -- > 0)
		{
			_slotDataList.get(i).dispose();
		}
		_slotDataList.clear();
		_slotDataList = null;
	}

	public SlotData getSlotData(String slotName)
	{
		int i = _slotDataList.size();
		while(i -- > 0)
		{
			if(Objects.equals(_slotDataList.get(i).name, slotName))
			{
				return _slotDataList.get(i);
			}
		}
		return null;
	}

	public void  addSlotData(SlotData slotData)
	{
		if(slotData == null)
		{
			throw new ArgumentError();
		}

		if (_slotDataList.indexOf(slotData) < 0)
		{
			_slotDataList.set(_slotDataList.size(), slotData);
		}
		else
		{
			throw new ArgumentError();
		}
	}

	public ArrayList<SlotData> getSlotDataList()
	{
		return _slotDataList;
	}
}
