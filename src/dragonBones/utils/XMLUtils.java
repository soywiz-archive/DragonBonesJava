package dragonBones.utils;

import flash.XML;

public class XMLUtils {
	public static boolean getBoolean(XML data, String key)
	{
		return getBoolean(data, key, false);
	}

	public static boolean getBoolean(XML data, String key, boolean defaultValue)
	{
		String value = (data != null) ? data.getString(key) : "";
		if(value.length() > 0)
		{
			switch(value)
			{
				case "0":
				case "NaN":
				case "":
				case "false":
				case "null":
				case "undefined":
					return false;

				case "1":
				case "true":
				default:
					return true;
			}
		}
		return defaultValue;
	}

	public static int getInt(XML data, String key, int defaultValue) {
		return (int)getNumber(data, key, defaultValue);
	}

	public static int getInt(XML data, String key) {
		return getInt(data, key, 0);
	}

	public static String getString(XML data, String key)
	{
		return getString(data, key, null);
	}

	public static String getString(XML data, String key, String defaultValue)
	{
		String value = (data != null) ? data.getString(key) : defaultValue;
		return (value != null) ? value : defaultValue;
	}

	public static double getNumber(XML data, String key, double defaultValue)
	{
		String value = (data != null) ? data.getString(key) : "";
		if(value.length() > 0)
		{
			switch(value)
			{
				case "NaN":
				case "":
				case "false":
				case "null":
				case "undefined":
					return Double.NaN;

				default:
					return Double.parseDouble(value);
			}
		}
		return defaultValue;
	}
}
