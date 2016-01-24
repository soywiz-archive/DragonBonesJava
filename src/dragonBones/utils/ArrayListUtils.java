package dragonBones.utils;

import dragonBones.animation.AnimationState;
import flash.events.Event;

import java.util.ArrayList;

public class ArrayListUtils {
	static public <T> int setLength(ArrayList<T> array, int length, T value) {
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
		return length;
	}

	static public <T> T pop(ArrayList<T> array) {
		return array.get(array.size() - 1);
	}

	public static <T> T shift(ArrayList<T> array) {
		return array.remove(0);
	}

	public static <T> T unshift(ArrayList<T> array, T value) {
		array.add(0, value);
		return value;
	}
}
