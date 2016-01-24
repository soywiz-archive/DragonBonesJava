package starling.textures;

import flash.display.BitmapData;

public class Texture {
	private int width;
	private int height;

	public static Texture fromBitmapData(BitmapData bitmapData, boolean generateMipMaps, boolean optimizeForRenderToTexture) {
		throw new Error();
	}

	public static Texture fromBitmapData(BitmapData bitmapData, boolean generateMipMaps, boolean optimizeForRenderToTexture, double scaleForTexture) {
		throw new Error();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
