package dragonBones.textures;

/**
* Copyright 2012-2013. DragonBones. All Rights Reserved.
* @playerversion Flash 10.0, Flash 10
* @langversion 3.0
* @version 2.0
*/
import flash.display.BitmapData;
import flash.display.MovieClip;
import flash.geom.Rectangle;

import dragonBones.core.dragonBones_internal;
import dragonBones.objects.DataParser;

//use namespace dragonBones_internal;

/**
 * The NativeTextureAtlas creates and manipulates TextureAtlas from traditional flash.display.DisplayObject.
 */
public class NativeTextureAtlas implements ITextureAtlas
{
	/**
	 * @private
	 */
	protected Object _subTextureDataDic;
	/**
	 * @private
	 */
	protected boolean _isDifferentConfig;
	/**
	 * @private
	 */
	protected String _name;
	/**
	 * The name of this NativeTextureAtlas instance.
	 */
	public String getName()
	{
		return _name;
	}

	protected MovieClip _movieClip;
	/**
	 * The MovieClip created by this NativeTextureAtlas instance.
	 */
	public MovieClip getMovieClip()
	{
		return _movieClip;
	}

	protected BitmapData _bitmapData;
	/**
	 * The BitmapData created by this NativeTextureAtlas instance.
	 */
	public BitmapData getBitmapData()
	{
		return _bitmapData;
	}

	protected double _scale;
	/**
	 * @private
	 */
	public double getScale()
	{
		return _scale;
	}
	/**
	 * Creates a new NativeTextureAtlas instance.
	 * @param texture A MovieClip or Bitmap.
	 * @param textureAtlasRawData The textureAtlas config data.
	 * @param textureScale A scale value (x and y axis)
	 * @param isDifferentConfig
	 */
	public NativeTextureAtlas(Object texture, Object textureAtlasRawData, double textureScale = 1, boolean isDifferentConfig = false)
	{
		_scale = textureScale;
		_isDifferentConfig = isDifferentConfig;
		if (texture instanceof BitmapData)
		{
			_bitmapData = (BitmapData)texture;
		}
		else if (texture instanceof MovieClip)
		{
			_movieClip = (MovieClip)texture;
			_movieClip.stop();
		}
		parseData(textureAtlasRawData);
	}
	/**
	 * Clean up all resources used by this NativeTextureAtlas instance.
	 */
	public void dispose()
	{
		_movieClip = null;
		if (_bitmapData)
		{
			_bitmapData.dispose();
		}
		_bitmapData = null;
	}
	/**
	 * The area occupied by all assets related to that name.
	 * @param name The name of these assets.
	 * @return Rectangle The area occupied by all assets related to that name.
	 */
	public Rectangle getRegion(String name)
	{
		TextureData textureData = (TextureData)_subTextureDataDic[name];
		if(textureData)
		{
			return textureData.region;
		}

		return null;
	}

	public Rectangle getFrame(String name)
	{
		TextureData textureData = (TextureData)_subTextureDataDic[name];
		if(textureData)
		{
			return textureData.frame;
		}

		return null;
	}

	protected void parseData(Object textureAtlasRawData)
	{
		_subTextureDataDic = DataParser.parseTextureAtlasData(textureAtlasRawData, _isDifferentConfig ? _scale : 1);
		_name = _subTextureDataDic.__name;

		delete _subTextureDataDic.__name;
	}

	private void movieClipToBitmapData()
	{
		if (!_bitmapData && _movieClip)
		{
			_movieClip.gotoAndStop(1);
			_bitmapData = new BitmapData(getNearest2N(_movieClip.width), getNearest2N(_movieClip.height), true, 0xFF00FF);
			_bitmapData.draw(_movieClip);
			_movieClip.gotoAndStop(_movieClip.totalFrames);
		}
	}

	private int getNearest2N(int _n)
	{
		return _n & _n - 1?1 << _n.toString(2).length:_n;
	}
}
