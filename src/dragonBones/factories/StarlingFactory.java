package dragonBones.factories;

/**
* Copyright 2012-2013. DragonBones. All Rights Reserved.
* @playerversion Flash 10.0, Flash 10
* @langversion 3.0
* @version 2.0
*/
import dragonBones.Armature;
import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.display.MovieClip;
import flash.geom.Rectangle;

import dragonBones.Slot;
import dragonBones.display.StarlingFastSlot;
import dragonBones.display.StarlingSlot;
import dragonBones.fast.FastArmature;
import dragonBones.fast.FastSlot;
import dragonBones.textures.ITextureAtlas;
import dragonBones.textures.StarlingTextureAtlas;

import starling.core.Starling;
import starling.display.Image;
import starling.display.Sprite;
import starling.textures.SubTexture;
import starling.textures.Texture;
import starling.textures.TextureAtlas;

//use namespace dragonBones_internal;

/**
 * A object managing the set of armature resources for Starling engine. It parses the raw data, stores the armature resources and creates armature instances.
 * @see dragonBones.Armature
 */

/**
 * A StarlingFactory instance manages the set of armature resources for the starling DisplayList. It parses the raw data (ByteArray), stores the armature resources and creates armature instances.
 * <p>Create an instance of the StarlingFactory class that way:</p>
 * <listing>
 * import flash.events.Event;
 * import dragonBones.factorys.BaseFactory;
 *
 * [Embed(source = "../assets/Dragon2.png", mimeType = "application/octet-stream")]
 *	private static const ResourcesData:Class;
 * var factory:StarlingFactory = new StarlingFactory();
 * factory.addEventListener(Event.COMPLETE, textureCompleteHandler);
 * factory.parseData(new ResourcesData());
 * </listing>
 * @see dragonBones.Armature
 */
public class StarlingFactory extends BaseFactory
{
	/**
	 * Whether to generate mapmaps (true) or not (false).
	 */
	public boolean generateMipMaps;
	/**
	 * Whether to optimize for rendering (true) or not (false).
	 */
	public boolean optimizeForRenderToTexture;
	/**
	 * Apply a scale for SWF specific texture. Use 1 for no scale.
	 */
	public double scaleForTexture;

	/**
	 * Creates a new StarlingFactory instance.
	 */
	public StarlingFactory()
	{
		super();
		scaleForTexture = 1;
	}

	/** @private */
	protected ITextureAtlas generateTextureAtlas(Object content, Object textureAtlasRawData)
	{
		Texture texture;
		BitmapData bitmapData;
		if (content instanceof BitmapData)
		{
			bitmapData = (BitmapData)content;
			texture = Texture.fromBitmapData(bitmapData, generateMipMaps, optimizeForRenderToTexture);
		}
		else if (content instanceof MovieClip)
		{
			MovieClip movieClip = (MovieClip)content;
			int width = (int)(getNearest2N(movieClip.getWidth()) * scaleForTexture);
			int height = (int)(getNearest2N(movieClip.getHeight()) * scaleForTexture);

//				_helpMatrix.a = 1;
//				_helpMatrix.b = 0;
//				_helpMatrix.c = 0;
//				_helpMatrix.d = 1;
			_helpMatrix.scale(scaleForTexture, scaleForTexture);
			_helpMatrix.tx = 0;
			_helpMatrix.ty = 0;
			movieClip.gotoAndStop(1);
			bitmapData = new BitmapData(width, height, true, 0xFF00FF);
			bitmapData.draw(movieClip, _helpMatrix);
			movieClip.gotoAndStop(movieClip.getTotalFrames());
			texture = Texture.fromBitmapData(bitmapData, generateMipMaps, optimizeForRenderToTexture, scaleForTexture);
		}
		else
		{
			throw new Error();
		}
		StarlingTextureAtlas textureAtlas = new StarlingTextureAtlas(texture, textureAtlasRawData, false);
		if (Starling.handleLostContext)
		{
			textureAtlas._bitmapData = bitmapData;
		}
		else
		{
			bitmapData.dispose();
		}
		return textureAtlas;
	}

	/** @private */
	protected Armature generateArmature()
	{
		return new Armature(new Sprite());
	}

	/** @private */
	protected FastArmature generateFastArmature()
	{
		return new FastArmature(new Sprite());
	}

	/** @private */
	protected Slot generateSlot()
	{
		return new StarlingSlot();
	}

	/**
	 * @private
	 * Generates an Slot instance.
	 * @return Slot An Slot instance.
	 */
	protected FastSlot generateFastSlot()
	{
		return new StarlingFastSlot();
	}

	/** @private */
	protected Object generateDisplay(Object textureAtlas, String fullName, double pivotX, double pivotY)
	{
		SubTexture subTexture = (SubTexture)((TextureAtlas)textureAtlas).getTexture(fullName);
		if (subTexture != null)
		{
			Image image = new Image(subTexture);
			if (Double.isNaN(pivotX) || Double.isNaN(pivotY))
			{
				Rectangle subTextureFrame = ((TextureAtlas)textureAtlas).getFrame(fullName);
				if(subTextureFrame != null)
				{
					pivotX = subTextureFrame.getWidth() / 2;//pivotX;
					pivotY = subTextureFrame.getHeight() / 2;// pivotY;
				}
				else
				{
					pivotX = subTexture.getWidth() / 2;//pivotX;
					pivotY = subTexture.getHeight() / 2;// pivotY;
				}

			}
			image.setPivotX(pivotX);
			image.setPivotY(pivotY);

			return image;
		}
		return null;
	}

	private int getNearest2N(int _n)
	{
		return ((_n & _n - 1) != 0) ?1 << Integer.toString(_n, 2).length():_n;
	}
}
