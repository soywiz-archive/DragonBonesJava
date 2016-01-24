package dragonBones.utils;

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
}
