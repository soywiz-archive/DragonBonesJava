package dragonBones.objects;

import org.w3c.dom.Document;

import java.util.Map;

public class DataParser
{
	public DataParser()
	{
	}

	public static DragonBonesData parseData(Object rawData)
	{
		if(rawData instanceof Document)
		{
			return XMLDataParser.parseDragonBonesData((Document)rawData);
		}
		else
		{
			return ObjectDataParser.parseDragonBonesData(rawData);
		}
		return null;
	}

	public static Map<String, Object> parseTextureAtlasData(Object textureAtlasData, double scale= 1)
	{
		if(textureAtlasData instanceof Document)
		{
			return XMLDataParser.parseTextureAtlasData((Document)textureAtlasData, scale);
		}
		else
		{
			return ObjectDataParser.parseTextureAtlasData(textureAtlasData, scale);
		}
		return null;
	}
}
