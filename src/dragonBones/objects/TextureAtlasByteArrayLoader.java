package dragonBones.objects;

import flash.display.Bitmap;
import flash.display.Loader;
import flash.display.MovieClip;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.system.ApplicationDomain;
import flash.system.LoaderContext;
import flash.utils.ByteArray;

public class TextureAtlasByteArrayLoader extends Loader
{
	private static const LoaderContext loaderContext = new LoaderContext(false, ApplicationDomain.currentDomain);

	public TextureAtlasByteArrayLoader()
	{
		super();
		loaderContext.allowCodeImport = true;
	}

	@Override
	public void loadBytes(ByteArray bytes, LoaderContext context=null)
	{
		context = context == null ? loaderContext : context;
		super.loadBytes(bytes, context);
	}
}
