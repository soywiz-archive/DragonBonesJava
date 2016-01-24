package dragonBones.objects;

import dragonBones.events.EventInfo;
import flash.Runnable1;
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
		super(this);
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
		loader.getContentLoaderInfo().addEventListener(Event.COMPLETE, new Runnable1<Event>() {
			@Override
			public void run(Event e) {
				e.target.removeEventListener(Event.COMPLETE, this);
				Loader loader = e.target.loader;
				Object content = e.target.content;
				loader.unloadAndStop();

				if (content instanceof Bitmap)
				{
					textureAtlas =  ((Bitmap)content).getBitmapData();
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

				dispatchEvent(new Event(Event.COMPLETE));
			}
		});
		loader.loadBytes(textureAtlasBytes);
	}
}
