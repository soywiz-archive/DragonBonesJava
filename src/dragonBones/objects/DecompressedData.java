package dragonBones.objects;

import dragonBones.events.EventInfo;
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Loader;
import flash.display.MovieClip;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.ByteArray;

/** Dispatched after a sucessful call to parseTextureAtlasBytes(). */
@EventInfo(name="complete", type="flash.events.Event")
public class DecompressedData extends EventDispatcher
{
	/**
	 * data name.
	 */
	public String name;

	public String textureBytesDataType;
	/**
	 * The xml or JSON for DragonBones data.
	 */
	public Object dragonBonesData;

	/**
	 * The xml or JSON for atlas data.
	 */
	public String textureAtlasData;

	/**
	 * The non parsed textureAtlas bytes.
	 */
	public ByteArray textureAtlasBytes;

	/**
	 * TextureAtlas can be bitmap, movieclip, ATF etc.
	 */
	public Object textureAtlas;

	public DecompressedData()
	{
	}

	public void dispose()
	{
		dragonBonesData = null;
		textureAtlasData = null;
		textureAtlas = null;
		textureAtlasBytes = null;
	}

	public void parseTextureAtlasBytes()
	{
		TextureAtlasByteArrayLoader loader = new TextureAtlasByteArrayLoader();
		loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loaderCompleteHandler);
		loader.loadBytes(textureAtlasBytes);
	}

	private void loaderCompleteHandler(Evenet e)
	{
		e.target.removeEventListener(Event.COMPLETE, loaderCompleteHandler);
		Loader loader = e.target.loader;
		Object content = e.target.content;
		loader.unloadAndStop();

		if (content instanceof Bitmap)
		{
			textureAtlas =  ((Bitmap)content).bitmapData;
		}
		else if (content instanceof Sprite)
		{
			textureAtlas = (MovieClip)((Sprite)content).getChildAt(0);
		}
		else
		{
			//ATF
			textureAtlas = content;
		}

		this.dispatchEvent(new Event(Event.COMPLETE));
	}
}
