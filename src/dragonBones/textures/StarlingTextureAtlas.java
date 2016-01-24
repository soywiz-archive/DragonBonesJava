package dragonBones.textures;
/**
* Copyright 2012-2013. DragonBones. All Rights Reserved.
* @playerversion Flash 10.0, Flash 10
* @langversion 3.0
* @version 2.0
*/
import flash.display.BitmapData;

import dragonBones.objects.DataParser;

import starling.textures.SubTexture;
import starling.textures.Texture;
import starling.textures.TextureAtlas;

import java.util.HashMap;
import java.util.Map;
//use namespace dragonBones_internal;

/**
 * The StarlingTextureAtlas creates and manipulates TextureAtlas from starling.display.DisplayObject.
 */
public class StarlingTextureAtlas extends TextureAtlas implements ITextureAtlas
{
	public BitmapData _bitmapData;
	/**
	 * @private
	 */
	protected Map<String, Texture> _subTextureDic;
	/**
	 * @private
	 */
	protected boolean _isDifferentConfig;
	/**
	 * @private
	 */
	protected double _scale;
	/**
	 * @private
	 */
	protected String _name;
	/**
	 * The name of this StarlingTextureAtlas instance.
	 */
	public String getName()
	{
		return _name;
	}
	/**
	 * Creates a new StarlingTextureAtlas instance.
	 * @param texture A texture instance.
	 * @param textureAtlasRawData A textureAtlas config data
	 * @param isDifferentConfig
	 */
	//public StarlingTextureAtlas(Texture texture, Object textureAtlasRawData, boolean isDifferentConfig = false)
	public StarlingTextureAtlas(Texture texture, Object textureAtlasRawData, boolean isDifferentConfig)
	{
		super(texture, null);
		if (texture != null)
		{
			_scale = texture.getScale();
			_isDifferentConfig = isDifferentConfig;
		}
		_subTextureDic = new HashMap<>();
		parseData(textureAtlasRawData);
	}
	/**
	 * Clean up all resources used by this StarlingTextureAtlas instance.
	 */
	@Override public void dispose()
	{
		super.dispose();
		for (Texture subTexture : _subTextureDic.values())
		{
			subTexture.dispose();
		}
		_subTextureDic = null;

		if (_bitmapData != null)
		{
			_bitmapData.dispose();
		}
		_bitmapData = null;
	}

	/**
	 * Get the Texture with that name.
	 * @param name The name ofthe Texture instance.
	 * @return The Texture instance.
	 */
	@Override public Texture getTexture(String name)
	{
		Texture texture = _subTextureDic.get(name);
		if (texture == null)
		{
			texture = super.getTexture(name);
			if (texture != null)
			{
				_subTextureDic.put(name, texture);
			}
		}
		return texture;
	}
	/**
	 * @private
	 */
	protected void parseData(Object textureAtlasRawData)
	{
		Map<String, TextureData> textureAtlasData = DataParser.parseTextureAtlasData(textureAtlasRawData, _isDifferentConfig ? _scale : 1);
		_name = (String) (Object)textureAtlasData.get("__name");
		textureAtlasData.remove("__name");
		for(String subTextureName : textureAtlasData.keySet())
		{
			TextureData textureData = textureAtlasData.get(subTextureName);
			//, textureData.rotated
			this.addRegion(subTextureName, textureData.region, textureData.frame);
		}
	}
}
