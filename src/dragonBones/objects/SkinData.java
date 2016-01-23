package dragonBones.objects;

/** @private */
final public class SkinData
{
	public String name;

	private ArrayList<SlotData> _slotDataList;

	public SkinData()
	{
		_slotDataList = new ArrayList<SlotData>(0, true);
	}

	public void dispose()
	{
		int i = _slotDataList.size();
		while(i -- > 0)
		{
			_slotDataList[i].dispose();
		}
		_slotDataList.fixed = false;
		_slotDataList.length = 0;
		_slotDataList = null;
	}

	public SlotData getSlotData(String slotName)
	{
		int i = _slotDataList.size();
		while(i -- > 0)
		{
			if(_slotDataList[i].name == slotName)
			{
				return _slotDataList[i];
			}
		}
		return null;
	}

	public void  addSlotData(SlotData slotData)
	{
		if(!slotData)
		{
			throw new ArgumentError();
		}

		if (_slotDataList.indexOf(slotData) < 0)
		{
			_slotDataList.fixed = false;
			_slotDataList[_slotDataList.length] = slotData;
			_slotDataList.fixed = true;
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
