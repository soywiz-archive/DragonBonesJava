package dragonBones.objects;

import dragonBones.textures.TextureData;
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
	}

	//public static Map<String, TextureData> parseTextureAtlasData(Object textureAtlasData, double scale= 1)
	public static Map<String, TextureData> parseTextureAtlasData(Object textureAtlasData, double scale)
	{
		if(textureAtlasData instanceof Document)
		{
			return XMLDataParser.parseTextureAtlasData((Document)textureAtlasData, scale);
		}
		else
		{
			return ObjectDataParser.parseTextureAtlasData(textureAtlasData, scale);
		}
	}
}
