package dragonBones.utils;

import flash.geom.ColorTransform;

public class ColorTransformUtil
{
	static public ColorTransform originalColor = new ColorTransform();

	public static ColorTransform cloneColor(ColorTransform color)
	{
		return new ColorTransform(color.redMultiplier,color.greenMultiplier,color.blueMultiplier,color.alphaMultiplier,color.redOffset,color.greenOffset,color.blueOffset,color.alphaOffset)
	}

	public static boolean isEqual(ColorTransform color1, ColorTransform color2)
	{
		return 	color1.alphaOffset == color2.alphaOffset &&
				color1.redOffset == color2.redOffset &&
				color1.greenOffset == color2.greenOffset &&
				color1.blueOffset == color2.blueOffset &&
				color1.alphaMultiplier == color2.alphaMultiplier &&
				color1.redMultiplier == color2.redMultiplier &&
				color1.greenMultiplier == color2.greenMultiplier &&
				color1.blueMultiplier == color2.blueMultiplier;
	}

	public static void minus(ColorTransform color1, ColorTransform color2, ColorTransform outputColor)
	{
		outputColor.alphaOffset = color1.alphaOffset - color2.alphaOffset;
		outputColor.redOffset = color1.redOffset - color2.redOffset;
		outputColor.greenOffset = color1.greenOffset - color2.greenOffset;
		outputColor.blueOffset = color1.blueOffset - color2.blueOffset;

		outputColor.alphaMultiplier = color1.alphaMultiplier - color2.alphaMultiplier;
		outputColor.redMultiplier = color1.redMultiplier - color2.redMultiplier;
		outputColor.greenMultiplier = color1.greenMultiplier - color2.greenMultiplier;
		outputColor.blueMultiplier = color1.blueMultiplier - color2.blueMultiplier;
	}

	public ColorTransformUtil()
	{
	}
}
