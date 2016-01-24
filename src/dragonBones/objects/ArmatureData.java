package dragonBones.objects;

import flash.Pair;
import flash.errors.ArgumentError;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

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
		_boneDataList = new ArrayList<BoneData>();
		_skinDataList = new ArrayList<SkinData>();
		_slotDataList = new ArrayList<SlotData>();
		_animationDataList = new ArrayList<AnimationData>();

		//_areaDataList = new Vector.<IAreaData>(0, true);
	}

	public void setSkinData(String skinName)
	{
		for (int i = 0, len = _slotDataList.size(); i < len; i++)
		{
			_slotDataList.get(i).dispose();
		}
		SkinData skinData = null;
		if(skinName == null && _skinDataList.size() > 0)
		{
			skinData = _skinDataList.get(0);
		}
		else
		{
			for (int i = 0, len = _skinDataList.size(); i < len; i++)
			{
				if (_skinDataList.get(i).name == skinName)
				{
					skinData = _skinDataList.get(i);
					break;
				}
			}
		}

		if (skinData != null)
		{
			SlotData slotData;
			for (int i = 0, len = skinData.getSlotDataList().size(); i < len; i++)
			{
				slotData = getSlotData(skinData.getSlotDataList().get(i).name);
				if (slotData != null)
				{
					for (int j = 0, jLen = skinData.getSlotDataList().get(i).getDisplayDataList().size(); j < jLen; j++)
					{
						slotData.addDisplayData(skinData.getSlotDataList().get(i).getDisplayDataList().get(j));
					}
				}
			}
		}
	}

	public void dispose()
	{
		int i = _boneDataList.size();
		while(i -- > 0)
		{
			_boneDataList.get(i).dispose();
		}
		i = _skinDataList.size();
		while(i -- > 0)
		{
			_skinDataList.get(i).dispose();
		}
		i = _slotDataList.size();
		while(i -- > 0)
		{
			_slotDataList.get(i).dispose();
		}
		i = _animationDataList.size();
		while(i -- > 0)
		{
			_animationDataList.get(i).dispose();
		}

		_boneDataList.clear();
		_skinDataList.clear();
		_slotDataList.clear();
		_animationDataList.clear();
		//_animationsCached。clear();
		_boneDataList = null;
		_skinDataList = null;
		_slotDataList = null;
		_animationDataList = null;
	}

	public BoneData getBoneData(String boneName)
	{
		int i = _boneDataList.size();
		while(i -- > 0)
		{
			if(Objects.equals(_boneDataList.get(i).name, boneName))
			{
				return _boneDataList.get(i);
			}
		}
		return null;
	}

	public SlotData getSlotData(String slotName)
	{
		if(slotName == null && _slotDataList.size() > 0)
		{
			return _slotDataList.get(0);
		}
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

	public SkinData getSkinData(String skinName)
	{
		if(skinName == null && _skinDataList.size() > 0)
		{
			return _skinDataList.get(0);
		}
		int i = _skinDataList.size();
		while(i -- > 0)
		{
			if(Objects.equals(_skinDataList.get(i).name, skinName))
			{
				return _skinDataList.get(i);
			}
		}

		return null;
	}

	public AnimationData getAnimationData(String animationName)
	{
		int i = _animationDataList.size();
		while(i -- > 0)
		{
			if(_animationDataList.get(i).name == animationName)
			{
				return _animationDataList.get(i);
			}
		}
		return null;
	}

	public void addBoneData(BoneData boneData)
	{
		if(boneData == null)
		{
			throw new ArgumentError();
		}

		if (_boneDataList.indexOf(boneData) < 0)
		{
			_boneDataList.set(_boneDataList.size(), boneData);
		}
		else
		{
			throw new ArgumentError();
		}
	}

	public void addSlotData(SlotData slotData)
	{
		if(slotData == null)
		{
			throw new ArgumentError();
		}

		if(_slotDataList.indexOf(slotData) < 0)
		{
			_slotDataList.set(_slotDataList.size(), slotData);
		}
		else
		{
			throw new ArgumentError();
		}
	}

	public void addSkinData(SkinData skinData)
	{
		if(skinData == null)
		{
			throw new ArgumentError();
		}

		if(_skinDataList.indexOf(skinData) < 0)
		{
			_skinDataList.set(_skinDataList.size(), skinData);
		}
		else
		{
			throw new ArgumentError();
		}
	}

	public void addAnimationData(AnimationData animationData)
	{
		if(animationData == null)
		{
			throw new ArgumentError();
		}

		if(_animationDataList.indexOf(animationData) < 0)
		{
			_animationDataList.set(_animationDataList.size(), animationData);
		}
	}

	public void sortBoneDataList()
	{
		int i = _boneDataList.size();
		if(i == 0)
		{
			return;
		}

		ArrayList<Pair<Integer, BoneData>> helpArray = new ArrayList<>();
		while(i -- > 0)
		{
			BoneData boneData = _boneDataList.get(i);
			int level = 0;
			BoneData parentData = boneData;
			while(parentData != null)
			{
				level ++;
				parentData = getBoneData(parentData.parent);
			}
			helpArray.set(i, new Pair(level, boneData));
		}

		helpArray.sort(new Comparator<Pair<Integer, BoneData>>() {
			@Override
			public int compare(Pair<Integer, BoneData> o1, Pair<Integer, BoneData> o2) {
				return Integer.compare(o1.first, o2.first);
			}
		});

		i = helpArray.size();
		while(i -- > 0)
		{
			_boneDataList.set(i, helpArray.get(i).second);
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
