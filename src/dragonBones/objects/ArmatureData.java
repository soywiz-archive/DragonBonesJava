package dragonBones.objects;

import java.util.ArrayList;

/** @private */
final public class ArmatureData
{
	public String name;

	private ArrayList<BoneData> _boneDataList;
	private ArrayList<SkinData> _skinDataList;
	private ArrayList<SlotData> _slotDataList;
	private ArrayList<AnimationData> _animationDataList;

	public ArmatureData()
	{
		_boneDataList = new ArrayList<BoneData>(0, true);
		_skinDataList = new ArrayList<SkinData>(0, true);
		_slotDataList = new ArrayList<SlotData>(0, true);
		_animationDataList = new ArrayList<AnimationData>(0, true);

		//_areaDataList = new Vector.<IAreaData>(0, true);
	}

	public void setSkinData(String skinName)
	{
		for (var i:int = 0, len:int = _slotDataList.length; i < len; i++)
		{
			_slotDataList[i].dispose();
		}
		var skinData:SkinData;
		if(!skinName && _skinDataList.length > 0)
		{
			skinData = _skinDataList[0];
		}
		else
		{
			for (i = 0, len = _skinDataList.length; i < len; i++)
			{
				if (_skinDataList[i].name == skinName)
				{
					skinData = _skinDataList[i];
					break;
				}
			}
		}

		if (skinData)
		{
			var slotData:SlotData;
			for (i = 0, len = skinData.slotDataList.length; i < len; i++)
			{
				slotData = getSlotData(skinData.slotDataList[i].name);
				if (slotData)
				{
					for (var j:int = 0, jLen:int = skinData.slotDataList[i].displayDataList.length; j < jLen; j++)
					{
						slotData.addDisplayData(skinData.slotDataList[i].displayDataList[j]);
					}
				}
			}
		}
	}

	public void dispose()
	{
		var i:int = _boneDataList.length;
		while(i --)
		{
			_boneDataList[i].dispose();
		}
		i = _skinDataList.length;
		while(i --)
		{
			_skinDataList[i].dispose();
		}
		i = _slotDataList.length;
		while(i --)
		{
			_slotDataList[i].dispose();
		}
		i = _animationDataList.length;
		while(i --)
		{
			_animationDataList[i].dispose();
		}

		_boneDataList.fixed = false;
		_boneDataList.length = 0;
		_skinDataList.fixed = false;
		_skinDataList.length = 0;
		_slotDataList.fixed = false;
		_slotDataList.length = 0;
		_animationDataList.fixed = false;
		_animationDataList.length = 0;
		//_animationsCached。clear();
		_boneDataList = null;
		_skinDataList = null;
		_slotDataList = null;
		_animationDataList = null;
	}

	public BoneData getBoneData(String boneName)
	{
		var i:int = _boneDataList.length;
		while(i --)
		{
			if(_boneDataList[i].name == boneName)
			{
				return _boneDataList[i];
			}
		}
		return null;
	}

	public SlotData getSlotData(String slotName)
	{
		if(!slotName && _slotDataList.length > 0)
		{
			return _slotDataList[0];
		}
		var i:int = _slotDataList.length;
		while(i --)
		{
			if(_slotDataList[i].name == slotName)
			{
				return _slotDataList[i];
			}
		}

		return null;
	}

	public SkinData getSkinData(String skinName)
	{
		if(!skinName && _skinDataList.length > 0)
		{
			return _skinDataList[0];
		}
		var i:int = _skinDataList.length;
		while(i --)
		{
			if(_skinDataList[i].name == skinName)
			{
				return _skinDataList[i];
			}
		}

		return null;
	}

	public AnimationData getAnimationData(String animationName)
	{
		var i:int = _animationDataList.length;
		while(i --)
		{
			if(_animationDataList[i].name == animationName)
			{
				return _animationDataList[i];
			}
		}
		return null;
	}

	public void addBoneData(BoneData boneData)
	{
		if(!boneData)
		{
			throw new ArgumentError();
		}

		if (_boneDataList.indexOf(boneData) < 0)
		{
			_boneDataList.fixed = false;
			_boneDataList[_boneDataList.length] = boneData;
			_boneDataList.fixed = true;
		}
		else
		{
			throw new ArgumentError();
		}
	}

	public void addSlotData(SlotData slotData)
	{
		if(!slotData)
		{
			throw new ArgumentError();
		}

		if(_slotDataList.indexOf(slotData) < 0)
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

	public void addSkinData(SkinData skinData)
	{
		if(!skinData)
		{
			throw new ArgumentError();
		}

		if(_skinDataList.indexOf(skinData) < 0)
		{
			_skinDataList.fixed = false;
			_skinDataList[_skinDataList.length] = skinData;
			_skinDataList.fixed = true;
		}
		else
		{
			throw new ArgumentError();
		}
	}

	public void addAnimationData(AnimationData animationData)
	{
		if(!animationData)
		{
			throw new ArgumentError();
		}

		if(_animationDataList.indexOf(animationData) < 0)
		{
			_animationDataList.fixed = false;
			_animationDataList[_animationDataList.length] = animationData;
			_animationDataList.fixed = true;
		}
	}

	public void sortBoneDataList()
	{
		var i:int = _boneDataList.length;
		if(i == 0)
		{
			return;
		}

		var helpArray:Array = [];
		while(i -- > 0)
		{
			var boneData:BoneData = _boneDataList[i];
			var level:int = 0;
			var parentData:BoneData = boneData;
			while(parentData)
			{
				level ++;
				parentData = getBoneData(parentData.parent);
			}
			helpArray[i] = [level, boneData];
		}

		helpArray.sortOn("0", Array.NUMERIC);

		i = helpArray.length;
		while(i --)
		{
			_boneDataList[i] = helpArray[i][1];
		}
	}

	public ArrayList<BoneData> getBoneDataList()
	{
		return _boneDataList;
	}
	public ArrayList<SkinData> getSkinDataList()
	{
		return _skinDataList;
	}
	public ArrayList<AnimationData> getAnimationDataList()
	{
		return _animationDataList;
	}

	public ArrayList<SlotData> getSlotDataList()
	{
		return _slotDataList;
	}

	/*
	private var _areaDataList:Vector.<IAreaData>;
	public function get areaDataList():Vector.<IAreaData>
	{
		return _areaDataList;
	}

	public function getAreaData(areaName:String):IAreaData
	{
		if(!areaName && _areaDataList.length > 0)
		{
			return _areaDataList[0];
		}
		var i:int = _areaDataList.length;
		while(i --)
		{
			if(_areaDataList[i]["name"] == areaName)
			{
				return _areaDataList[i];
			}
		}
		return null;
	}

	public function addAreaData(areaData:IAreaData):void
	{
		if(!areaData)
		{
			throw new ArgumentError();
		}

		if(_areaDataList.indexOf(areaData) < 0)
		{
			_areaDataList.fixed = false;
			_areaDataList[_areaDataList.length] = areaData;
			_areaDataList.fixed = true;
		}
	}
	*/
}
