package dragonBones.objects;

final public class BoneData
{
	public String name;
	public String parent;
	public double length;

	public DBTransform global;
	public DBTransform transform;

	public boolean inheritScale;
	public boolean inheritRotation;

	public BoneData()
	{
		length = 0;
		global = new DBTransform();
		transform = new DBTransform();
		inheritRotation = true;
		inheritScale = false;

		//_areaDataList = new Vector.<IAreaData>(0, true);
	}

	public void dispose()
	{
		global = null;
		transform = null;
		/*
		if(_areaDataList)
		{
			for each(var areaData:IAreaData in _areaDataList)
			{
				areaData.dispose();
			}
			_areaDataList.fixed = false;
			_areaDataList.length = 0;
			_areaDataList = null;
		}
		*/
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
