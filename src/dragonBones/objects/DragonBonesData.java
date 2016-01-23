package dragonBones.objects;

import flash.geom.Point;
import flash.utils.Dictionary;

public class DragonBonesData
{
	public String name;
	public boolean isGlobalData;

	private ArrayList<ArmatureData> _armatureDataList = new ArrayList<ArmatureData>(0, true);
	private Dictionary _displayDataDictionary = new Dictionary();

	public DragonBonesData()
	{
	}

	public void dispose()
	{
		for (ArmatureData armatureData : _armatureDataList)
		{
			armatureData.dispose();
		}
		_armatureDataList.fixed = false;
		_armatureDataList.length = 0;
		_armatureDataList = null;

		removeAllDisplayData();
		_displayDataDictionary = null;
	}

	public ArrayList<ArmatureData> getArmatureDataList()
	{
		return _armatureDataList;
	}

	public ArmatureData getArmatureDataByName(String armatureName)
	{
		int i = _armatureDataList.size();
		while(i -- > 0)
		{
			if(_armatureDataList[i].name == armatureName)
			{
				return _armatureDataList[i];
			}
		}

		return null;
	}

	public void addArmatureData(ArmatureData armatureData)
	{
		if(!armatureData)
		{
			throw new ArgumentError();
		}

		if(_armatureDataList.indexOf(armatureData) < 0)
		{
			_armatureDataList.fixed = false;
			_armatureDataList[_armatureDataList.length] = armatureData;
			_armatureDataList.fixed = true;
		}
		else
		{
			throw new ArgumentError();
		}
	}

	public void removeArmatureData(ArmatureData armatureData)
	{
		int index = _armatureDataList.indexOf(armatureData);
		if(index >= 0)
		{
			_armatureDataList.fixed = false;
			_armatureDataList.splice(index, 1);
			_armatureDataList.fixed = true;
		}
	}

	public void removeArmatureDataByName(String armatureName)
	{
		int i = _armatureDataList.length;
		while(i -- > 0)
		{
			if(_armatureDataList[i].name == armatureName)
			{
				_armatureDataList.fixed = false;
				_armatureDataList.splice(i, 1);
				_armatureDataList.fixed = true;
			}
		}
	}

	public DisplayData getDisplayDataByName(String name)
	{
		return _displayDataDictionary[name];
	}

	public void  addDisplayData(DisplayData displayData)
	{
		_displayDataDictionary[displayData.name] = displayData;
	}

	public void removeDisplayDataByName(String name)
	{
		delete _displayDataDictionary[name]
	}

	public void removeAllDisplayData()
	{
		for(String name : _displayDataDictionary.getKeys())
		{
			delete _displayDataDictionary[name];
		}
	}
}
