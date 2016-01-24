package dragonBones.objects;

import dragonBones.textures.TextureData;
import dragonBones.textures.TextureDataMap;
import flash.XML;
import org.w3c.dom.Document;

import java.util.Map;

public class DataParser
{
	public DataParser()
	{
	}

	public static DragonBonesData parseData(Object rawData)
	{
		if(rawData instanceof XML)
		{
			return XMLDataParser.parseDragonBonesData((XML)rawData);
		}
		else
		{
			throw new Error("Not supported JSON/Object parsing");
			//return ObjectDataParser.parseDragonBonesData(rawData);
		}
	}

	//public static Map<String, TextureData> parseTextureAtlasData(Object textureAtlasData, double scale= 1)
	public static TextureDataMap parseTextureAtlasData(Object textureAtlasData, double scale)
	{
		if(textureAtlasData instanceof XML)
		{
			return XMLDataParser.parseTextureAtlasData((XML)textureAtlasData, scale);
		}
		else
		{
			//return ObjectDataParser.parseTextureAtlasData(textureAtlasData, scale);
			throw new Error("Not supported JSON/Object parsing");
		}
	}
}
