package starling.textures;

import dragonBones.textures.ITextureAtlas;
import flash.geom.Rectangle;

public class TextureAtlas {
	public TextureAtlas(Texture base, Object unknown) {
	}

	public Texture getTexture(String name) {
		throw new Error();
	}

	public void addRegion(String name, Rectangle region, Rectangle frame) {
		throw new Error();
	}

	public Rectangle getFrame(String name) {
		throw new Error();
	}

	public void dispose() {

	}

	public Rectangle getRegion(String name) {
		return null;
	}
}
