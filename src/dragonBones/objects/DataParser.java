package dragonBones.objects;

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
			return ObjectDataParser.parseDragonBonesData(rawData);
		}
		return null;
	}

	public static Object parseTextureAtlasData(Object textureAtlasData, double scale = 1)
	{
		if(textureAtlasData instanceof XML)
		{
			return XMLDataParser.parseTextureAtlasData((XML)textureAtlasData, scale);
		}
		else
		{
			return ObjectDataParser.parseTextureAtlasData(textureAtlasData, scale);
		}
		return null;
	}
}
