package dragonBones.textures;

import flash.geom.Rectangle;

public final class TextureData
{
	public Rectangle region;
	public Rectangle frame;
	public boolean rotated;

	public TextureData(Rectangle region, Rectangle frame, boolean rotated)
	{
		this.region = region;
		this.frame = frame;
		this.rotated = rotated;
	}
}
