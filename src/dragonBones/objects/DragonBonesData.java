package dragonBones.objects;

import flash.errors.ArgumentError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DragonBonesData {
	public String name;
	public boolean isGlobalData;

	private ArrayList<ArmatureData> _armatureDataList = new ArrayList<ArmatureData>();
	private Map<String, DisplayData> _displayDataDictionary = new HashMap<>();

	public DragonBonesData() {
	}

	public void dispose() {
		for (ArmatureData armatureData : _armatureDataList) {
			armatureData.dispose();
		}
		_armatureDataList.clear();
		_armatureDataList = null;

		removeAllDisplayData();
		_displayDataDictionary = null;
	}

	public ArrayList<ArmatureData> getArmatureDataList() {
		return _armatureDataList;
	}

	public ArmatureData getArmatureDataByName(String armatureName) {
		int i = _armatureDataList.size();
		while (i-- > 0) {
			if (Objects.equals(_armatureDataList.get(i).name, armatureName)) {
				return _armatureDataList.get(i);
			}
		}

		return null;
	}

	public void addArmatureData(ArmatureData armatureData) {
		if (armatureData == null) {
			throw new ArgumentError();
		}

		if (_armatureDataList.indexOf(armatureData) < 0) {
			_armatureDataList.set(_armatureDataList.size(), armatureData);
		} else {
			throw new ArgumentError();
		}
	}

	public void removeArmatureData(ArmatureData armatureData) {
		_armatureDataList.remove(armatureData);
	}

	public void removeArmatureDataByName(String armatureName) {
		int i = _armatureDataList.size();
		while (i-- > 0) {
			if (Objects.equals(_armatureDataList.get(i).name, armatureName)) {
				_armatureDataList.remove(i);
			}
		}
	}

	public DisplayData getDisplayDataByName(String name) {
		return _displayDataDictionary.get(name);
	}

	public void addDisplayData(DisplayData displayData) {
		_displayDataDictionary.put(displayData.name, displayData);
	}

	public void removeDisplayDataByName(String name) {
		_displayDataDictionary.remove(name);
	}

	public void removeAllDisplayData() {
		_displayDataDictionary.clear();
	}
}
