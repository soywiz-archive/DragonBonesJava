package dragonBones.factories;

import dragonBones.Armature;
import dragonBones.display.NativeFastSlot;
import dragonBones.fast.FastArmature;
import dragonBones.fast.FastSlot;
import dragonBones.Slot;
import dragonBones.core.dragonBones_internal;
import dragonBones.display.NativeSlot;
import dragonBones.textures.ITextureAtlas;
import dragonBones.textures.NativeTextureAtlas;

import flash.display.MovieClip;
import flash.display.Shape;
import flash.display.Sprite;
import flash.geom.Matrix;
import flash.geom.Rectangle;

//use namespace dragonBones_internal;

/**
* Copyright 2012-2013. DragonBones. All Rights Reserved.
* @playerversion Flash 10.0, Flash 10
* @langversion 3.0
* @version 2.0
*/

public class NativeFactory extends BaseFactory
{
	/**
	 * If enable BitmapSmooth
	 */
	public boolean fillBitmapSmooth;

	/**
	 * If use bitmapData Texture（When using dbswf，you can use vector element，if enable useBitmapDataTexture，dbswf will be force converted to BitmapData）
	 */
	public boolean useBitmapDataTexture;

	public NativeFactory()
	{
		super();
	}

	/** @private */
	protected ITextureAtlas generateTextureAtlas(Object content, Object textureAtlasRawData)
	{
		NativeTextureAtlas textureAtlas = new NativeTextureAtlas(content, textureAtlasRawData, 1, false);
		return textureAtlas;
	}

	/** @private */
	protected Armature generateArmature()
	{
		Sprite display = new Sprite();
		Armature armature = new Armature(display);
		return armature;
	}

	protected FastArmature generateFastArmature()
	{
		FastArmature armature = new FastArmature(new Sprite());
		return armature;
	}

	protected FastSlot generateFastSlot()
	{
		FastSlot slot = new NativeFastSlot();
		return slot;
	}

	/** @private */
	protected Slot generateSlot()
	{
		return new NativeSlot();
	}

	/** @private */
	protected Object generateDisplay(Object textureAtlas, String fullName, double pivotX, double pivotY)
	{
		NativeTextureAtlas nativeTextureAtlas;
		if(textureAtlas instanceof NativeTextureAtlas)
		{
			nativeTextureAtlas = (NativeTextureAtlas)textureAtlas;
		}

		if(nativeTextureAtlas != null)
		{
			MovieClip movieClip = nativeTextureAtlas.getMovieClip();
			if(useBitmapDataTexture && movieClip != null)
			{
				nativeTextureAtlas.movieClipToBitmapData();
			}

			//TO DO 问春雷
			if (!useBitmapDataTexture && movieClip != null && movieClip.getTotalFrames() >= 3)
			{
				movieClip.gotoAndStop(movieClip.getTotalFrames());
				movieClip.gotoAndStop(fullName);
				if (movieClip.getNumChildren() > 0)
				{
					try
					{
						Object displaySWF = movieClip.getChildAt(0);
						displaySWF.x = 0;
						displaySWF.y = 0;
						return displaySWF;
					}
					catch(Throwable e)
					{
						throw new Error("Can not get the movie clip, please make sure the version of the resource compatible with app version!");
					}
				}
			}
			else if(nativeTextureAtlas.getBitmapData() != null)
			{
				Rectangle subTextureRegion = nativeTextureAtlas.getRegion(fullName);
				if (subTextureRegion != null)
				{
					Rectangle subTextureFrame = nativeTextureAtlas.getFrame(fullName);

					if (Double.isNaN(pivotX) || Double.isNaN(pivotX))
					{
						if (subTextureFrame != null)
						{
							pivotX = subTextureFrame.getWidth() / 2 + subTextureFrame.getX();
							pivotY = subTextureFrame.getHeight() / 2 + subTextureFrame.getY();
						}
						else
						{
							pivotX = subTextureRegion.getWidth() / 2;
							pivotY = subTextureRegion.getHeight() / 2;
						}

					}
					else
					{
						if(subTextureFrame != null)
						{
							pivotX += subTextureFrame.getX();
							pivotY += subTextureFrame.getY();
						}
					}

					Shape displayShape = new Shape();
					_helpMatrix.a = 1;
					_helpMatrix.b = 0;
					_helpMatrix.c = 0;
					_helpMatrix.d = 1;
					_helpMatrix.scale(1 / nativeTextureAtlas.scale, 1 / nativeTextureAtlas.scale);
					_helpMatrix.tx = -pivotX - subTextureRegion.x;
					_helpMatrix.ty = -pivotY - subTextureRegion.y;

					displayShape.getGraphics().beginBitmapFill(nativeTextureAtlas.bitmapData, _helpMatrix, false, fillBitmapSmooth);
					displayShape.getGraphics().drawRect(-pivotX, -pivotY, subTextureRegion.width, subTextureRegion.height);

					return displayShape;
				}
			}
			else
			{
				throw new Error();
			}
		}
		return null;
	}
}
