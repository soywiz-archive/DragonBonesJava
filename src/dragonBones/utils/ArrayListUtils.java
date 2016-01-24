package dragonBones.utils;

import flash.events.Event;

import java.util.ArrayList;

public class ArrayListUtils {
	static public <T> void setLength(ArrayList<T> array, int length, T value) {
		if (length == 0) {
			array.clear();
		} else {
			while (array.size() > length) {
				array.remove(array.size() - 1);
			}
			while (array.size() < length) {
				array.add(value);
			}
		}
	}

	static public <T> T pop(ArrayList<T> array) {
		return array.get(array.size() - 1);
	}

	public static <T> T shift(ArrayList<T> array) {
		return array.remove(0);
	}

}
