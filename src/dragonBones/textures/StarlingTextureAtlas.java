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
	 * @param isDifferentXML
	 */
	public StarlingTextureAtlas(Texture texture, Object textureAtlasRawData, boolean isDifferentConfig = false)
	{
		super(texture, null);
		if (texture)
		{
			_scale = texture.scale;
			_isDifferentConfig = isDifferentConfig;
		}
		_subTextureDic = {};
		parseData(textureAtlasRawData);
	}
	/**
	 * Clean up all resources used by this StarlingTextureAtlas instance.
	 */
	@Override public void dispose()
	{
		super.dispose();
		for (SubTexture subTexture : _subTextureDic)
		{
			subTexture.dispose();
		}
		_subTextureDic = null;

		if (_bitmapData)
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
				_subTextureDic[name] = texture;
			}
		}
		return texture;
	}
	/**
	 * @private
	 */
	protected void parseData(Object textureAtlasRawData)
	{
		Object textureAtlasData = DataParser.parseTextureAtlasData(textureAtlasRawData, _isDifferentConfig ? _scale : 1);
		_name = textureAtlasData.get("__name");
		textureAtlasData.remove("__name");
		for(String subTextureName : textureAtlasData)
		{
			TextureData textureData = textureAtlasData[subTextureName];
			//, textureData.rotated
			this.addRegion(subTextureName, textureData.region, textureData.frame);
		}
	}
}
