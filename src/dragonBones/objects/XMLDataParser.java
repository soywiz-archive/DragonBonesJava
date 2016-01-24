package dragonBones.objects;

/**
 * Copyright 2012-2013. DragonBones. All Rights Reserved.
 * @playerversion Flash 10.0, Flash 10
 * @langversion 3.0
 * @version 2.0
 */

import dragonBones.core.DragonBones;
import dragonBones.textures.TextureData;
import dragonBones.textures.TextureDataMap;
import dragonBones.utils.ConstValues;
import dragonBones.utils.DBDataUtil;

import flash.XML;
import flash.errors.ArgumentError;
import flash.geom.ColorTransform;
import flash.geom.Point;
import flash.geom.Rectangle;

import static dragonBones.utils.XMLUtils.*;

//use namespace dragonBones_internal;

/**
 * The XMLDataParser class parses xml data from dragonBones generated maps.
 */
final public class XMLDataParser
{
	private static DragonBonesData tempDragonBonesData;

	public static TextureDataMap parseTextureAtlasData(XML rawData, double scale)
	{
		TextureDataMap textureAtlasData = new TextureDataMap();
		textureAtlasData.__name = getString(rawData, ConstValues.A_NAME);
		Rectangle subTextureFrame;
		for (XML subTextureXML : rawData.children(ConstValues.SUB_TEXTURE))
		{
			String subTextureName = getString(subTextureXML, ConstValues.A_NAME);

			Rectangle subTextureRegion = new Rectangle();
			subTextureRegion.setX(getInt(subTextureXML, ConstValues.A_X) / scale);
			subTextureRegion.setY(getInt(subTextureXML, ConstValues.A_Y) / scale);
			subTextureRegion.setWidth(getInt(subTextureXML, ConstValues.A_WIDTH) / scale);
			subTextureRegion.setHeight(getInt(subTextureXML, ConstValues.A_HEIGHT) / scale);
			Boolean rotated = getBoolean(subTextureXML, ConstValues.A_ROTATED, false);

			double frameWidth = getNumber(subTextureXML, ConstValues.A_FRAME_WIDTH, 0) / scale;
			double frameHeight = getNumber(subTextureXML, ConstValues.A_FRAME_HEIGHT, 0) / scale;

			if(frameWidth > 0 && frameHeight > 0)
			{
				subTextureFrame = new Rectangle();
				subTextureFrame.setX(getInt(subTextureXML, ConstValues.A_FRAME_X) / scale);
				subTextureFrame.setY(getInt(subTextureXML, ConstValues.A_FRAME_Y) / scale);
				subTextureFrame.setWidth(frameWidth);
				subTextureFrame.setHeight(frameHeight);
			}
			else
			{
				subTextureFrame = null;
			}

			textureAtlasData.data.put(subTextureName, new TextureData(subTextureRegion, subTextureFrame, rotated));
		}

		return textureAtlasData;
	}

	/**
	 * Parse the SkeletonData.
	 * @param rawData The SkeletonData xml to parse.
	 * @return A SkeletonData instance.
	 */
	public static DragonBonesData parseDragonBonesData(XML rawData)
	{
		if(rawData == null)
		{
			throw new ArgumentError();
		}
		String version = rawData.getString(ConstValues.A_VERSION);
		switch (version)
		{
			case "2.3":
			case "3.0":
				return XML3DataParser.parseSkeletonData(rawData);
			case DragonBones.DATA_VERSION:
				break;

			default:
				throw new Error("Nonsupport version!");
		}

		int frameRate = getInt(rawData, ConstValues.A_FRAME_RATE);

		DragonBonesData outputDragonBonesData = new DragonBonesData();
		outputDragonBonesData.name = getString(rawData, ConstValues.A_NAME);
		outputDragonBonesData.isGlobalData = getBoolean(rawData, ConstValues.A_IS_GLOBAL, true);
		tempDragonBonesData = outputDragonBonesData;
		for (XML armatureXML : rawData.children(ConstValues.ARMATURE))
		{
			outputDragonBonesData.addArmatureData(parseArmatureData(armatureXML, frameRate));
		}
		tempDragonBonesData = null;

		return outputDragonBonesData;
	}

	private static ArmatureData parseArmatureData(XML armatureXML, int frameRate)
	{
		ArmatureData outputArmatureData = new ArmatureData();
		outputArmatureData.name = getString(armatureXML, ConstValues.A_NAME);

		for (XML boneXML : armatureXML.children(ConstValues.BONE))
		{
			outputArmatureData.addBoneData(parseBoneData(boneXML));
		}

		for (XML slotXML : armatureXML.children(ConstValues.SLOT))
		{
			outputArmatureData.addSlotData(parseSlotData(slotXML));
		}
		for (XML skinXML :  armatureXML.children(ConstValues.SKIN))
		{
			outputArmatureData.addSkinData(parseSkinData(skinXML));
		}

		if(tempDragonBonesData.isGlobalData)
		{
			DBDataUtil.transformArmatureData(outputArmatureData);
		}

		outputArmatureData.sortBoneDataList();

		for (XML animationXML : armatureXML.children(ConstValues.ANIMATION))
		{
			AnimationData animationData = parseAnimationData(animationXML, frameRate);
			DBDataUtil.addHideTimeline(animationData, outputArmatureData, true);
			DBDataUtil.transformAnimationData(animationData, outputArmatureData, tempDragonBonesData.isGlobalData);
			outputArmatureData.addAnimationData(animationData);
		}

		return outputArmatureData;
	}

	private static BoneData parseBoneData(XML boneXML)
	{
		BoneData boneData = new BoneData();
		boneData.name = getString(boneXML, ConstValues.A_NAME);
		boneData.parent = getString(boneXML, ConstValues.A_PARENT);
		boneData.length = getNumber(boneXML, ConstValues.A_LENGTH, 0);
		boneData.inheritRotation = getBoolean(boneXML, ConstValues.A_INHERIT_ROTATION, true);
		boneData.inheritScale = getBoolean(boneXML, ConstValues.A_INHERIT_SCALE, true);

		parseTransform(boneXML.first(ConstValues.TRANSFORM), boneData.transform, null);
		if(tempDragonBonesData.isGlobalData)//绝对数据
		{
			boneData.global.copy(boneData.transform);
		}

		return boneData;
	}


	private static SkinData parseSkinData(XML skinXML)
	{
		SkinData skinData = new SkinData();
		skinData.name = skinXML.getString(ConstValues.A_NAME);

		for (XML slotXML : skinXML.children(ConstValues.SLOT))
		{
			skinData.addSlotData(parseSlotDisplayData(slotXML));
		}

		return skinData;
	}

	private static SlotData parseSlotDisplayData(XML slotXML)
	{
		SlotData slotData = new SlotData();
		slotData.name = slotXML.getString(ConstValues.A_NAME);
		for (XML displayXML : slotXML.children(ConstValues.DISPLAY))
		{
			slotData.addDisplayData(parseDisplayData(displayXML));
		}

		return slotData;
	}

	private static SlotData parseSlotData(XML slotXML)
	{
		SlotData slotData = new SlotData();
		slotData.name = slotXML.getString(ConstValues.A_NAME);
		slotData.parent = slotXML.getString(ConstValues.A_PARENT);
		slotData.zOrder = slotXML.getDouble(ConstValues.A_Z_ORDER,0);
		slotData.blendMode = slotXML.getString(ConstValues.A_BLENDMODE);
		slotData.displayIndex = slotXML.getInt(ConstValues.A_DISPLAY_INDEX);
		return slotData;
	}

	private static DisplayData parseDisplayData(XML displayXML)
	{
		DisplayData displayData = new DisplayData();
		displayData.name = displayXML.getString(ConstValues.A_NAME);
		displayData.type = displayXML.getString(ConstValues.A_TYPE);

		parseTransform(displayXML.first(ConstValues.TRANSFORM), displayData.transform, displayData.pivot);

		displayData.pivot.x = Double.NaN;
		displayData.pivot.y = Double.NaN;

		if(tempDragonBonesData!=null)
		{
			tempDragonBonesData.addDisplayData(displayData);
		}

		return displayData;
	}

	/** @private */
	static AnimationData parseAnimationData(XML animationXML, int frameRate)
	{
		AnimationData animationData = new AnimationData();
		animationData.name = getString(animationXML, ConstValues.A_NAME);
		animationData.frameRate = frameRate;
		animationData.duration = Math.round((getInt(animationXML, ConstValues.A_DURATION)) * 1000 / frameRate);
		animationData.playTimes = getInt(animationXML,ConstValues.A_PLAY_TIMES,1);
		animationData.fadeTime = getNumber(animationXML,ConstValues.A_FADE_IN_TIME,0);
		animationData.scale = getNumber(animationXML, ConstValues.A_SCALE, 1);
		//use frame tweenEase, NaN
		//overwrite frame tweenEase, [-1, 0):ease in, 0:line easing, (0, 1]:ease out, (1, 2]:ease in out
		animationData.tweenEasing = getNumber(animationXML, ConstValues.A_TWEEN_EASING, Double.NaN);
		animationData.autoTween = getBoolean(animationXML, ConstValues.A_AUTO_TWEEN, true);

		for (XML frameXML : animationXML.children(ConstValues.FRAME))
		{
			Frame frame = parseTransformFrame(frameXML, frameRate);
			animationData.addFrame(frame);
		}

		parseTimeline(animationXML, animationData);

		int lastFrameDuration = animationData.duration;
		for (XML timelineXML : animationXML.children(ConstValues.BONE))
		{
			TransformTimeline timeline = parseTransformTimeline(timelineXML, animationData.duration, frameRate);
			if (timeline.getFrameList().size() > 0)
			{
				lastFrameDuration = Math.min(lastFrameDuration, timeline.getFrameList().get(timeline.getFrameList().size() - 1).duration);
				animationData.addTimeline(timeline);
			}

		}

		for (XML slotTimelineXML : animationXML.children(ConstValues.SLOT))
		{
			SlotTimeline slotTimeline = parseSlotTimeline(slotTimelineXML, animationData.duration, frameRate);
			if (slotTimeline.getFrameList().size() > 0)
			{
				lastFrameDuration = Math.min(lastFrameDuration, slotTimeline.getFrameList().get(slotTimeline.getFrameList().size() - 1).duration);
				animationData.addSlotTimeline(slotTimeline);
			}
		}

		if(animationData.getFrameList().size() > 0)
		{
			lastFrameDuration = Math.min(lastFrameDuration, animationData.getFrameList().get(animationData.getFrameList().size() - 1).duration);
		}
		animationData.lastFrameDuration = lastFrameDuration;

		return animationData;
	}

	private static TransformTimeline parseTransformTimeline(XML timelineXML, int duration, int frameRate)
	{
		TransformTimeline timeline = new TransformTimeline();
		timeline.name = getString(timelineXML, ConstValues.A_NAME);
		timeline.scale = getNumber(timelineXML, ConstValues.A_SCALE, 1);
		timeline.offset = getNumber(timelineXML, ConstValues.A_OFFSET, 0);
		timeline.originPivot.x = getNumber(timelineXML, ConstValues.A_PIVOT_X, 0);
		timeline.originPivot.y = getNumber(timelineXML, ConstValues.A_PIVOT_Y, 0);
		timeline.duration = duration;

		for (XML frameXML : timelineXML.children(ConstValues.FRAME))
		{
			TransformFrame frame = parseTransformFrame(frameXML, frameRate);
			timeline.addFrame(frame);
		}

		parseTimeline(timelineXML, timeline);

		return timeline;
	}

	private static SlotTimeline parseSlotTimeline(XML timelineXML, int duration, int frameRate)
	{
		SlotTimeline timeline = new SlotTimeline();
		timeline.name = getString(timelineXML, ConstValues.A_NAME);
		timeline.scale = getNumber(timelineXML, ConstValues.A_SCALE, 1);
		timeline.offset = getNumber(timelineXML, ConstValues.A_OFFSET, 0);
		timeline.duration = duration;

		for (XML frameXML : timelineXML.children(ConstValues.FRAME))
		{
			SlotFrame frame = parseSlotFrame(frameXML, frameRate);
			timeline.addFrame(frame);
		}

		parseTimeline(timelineXML, timeline);

		return timeline;
	}

	private static Frame parseMainFrame(XML frameXML, int frameRate)
	{
		Frame frame = new Frame();
		parseFrame(frameXML, frame, frameRate);
		return frame;
	}

	private static SlotFrame parseSlotFrame(XML frameXML, int frameRate)
	{
		SlotFrame frame = new SlotFrame();
		parseFrame(frameXML, frame, frameRate);

		frame.visible = !getBoolean(frameXML, ConstValues.A_HIDE, false);

		//NaN:no tween, 10:auto tween, [-1, 0):ease in, 0:line easing, (0, 1]:ease out, (1, 2]:ease in out
		frame.tweenEasing = getNumber(frameXML, ConstValues.A_TWEEN_EASING, 10);
		frame.displayIndex = getInt(frameXML,ConstValues.A_DISPLAY_INDEX,0);

		//如果为NaN，则说明没有改变过zOrder
		frame.zOrder = getNumber(frameXML, ConstValues.A_Z_ORDER, tempDragonBonesData.isGlobalData ? Double.NaN:0);

		XML colorTransformXML = frameXML.first(ConstValues.COLOR);
		if(colorTransformXML != null)
		{
			frame.color = new ColorTransform();
			parseColorTransform(colorTransformXML, frame.color);
		}

		return frame;
	}

	private static TransformFrame parseTransformFrame(XML frameXML, int frameRate)
	{
		TransformFrame frame = new TransformFrame();
		parseFrame(frameXML, frame, frameRate);

		frame.visible = !getBoolean(frameXML, ConstValues.A_HIDE, false);

		//NaN:no tween, 10:auto tween, [-1, 0):ease in, 0:line easing, (0, 1]:ease out, (1, 2]:ease in out
		frame.tweenEasing = getNumber(frameXML, ConstValues.A_TWEEN_EASING, 10);
		frame.tweenRotate = getInt(frameXML,ConstValues.A_TWEEN_ROTATE,0);
		frame.tweenScale = getBoolean(frameXML, ConstValues.A_TWEEN_SCALE, true);
//			frame.displayIndex = int(getNumber(frameXML,ConstValues.A_DISPLAY_INDEX,0));


		parseTransform(frameXML.first(ConstValues.TRANSFORM), frame.transform, frame.pivot);
		if(tempDragonBonesData.isGlobalData)//绝对数据
		{
			frame.global.copy(frame.transform);
		}

		frame.scaleOffset.x = getNumber(frameXML, ConstValues.A_SCALE_X_OFFSET, 0);
		frame.scaleOffset.y = getNumber(frameXML, ConstValues.A_SCALE_Y_OFFSET, 0);

		return frame;
	}

	private static void parseTimeline(XML timelineXML, Timeline timeline)
	{
		int position = 0;
		Frame frame = null;
		for (Frame frame2 : timeline.getFrameList())
		{
			frame = frame2;
			frame.position = position;
			position += frame.duration;
		}
		if(frame != null)
		{
			frame.duration = timeline.duration - frame.position;
		}
	}

	private static void parseFrame(XML frameXML, Frame frame, int frameRate)
	{
		frame.duration = Math.round((getInt(frameXML, ConstValues.A_DURATION)) * 1000 / frameRate);
		frame.action = getString(frameXML, ConstValues.A_ACTION);
		frame.event = getString(frameXML, ConstValues.A_EVENT);
		frame.sound = getString(frameXML, ConstValues.A_SOUND);
	}

	private static void parseTransform(XML transformXML, DBTransform transform, Point pivot)
	{
		if(transformXML != null)
		{
			if(transform != null)
			{
				transform.x = getNumber(transformXML,ConstValues.A_X,0);
				transform.y = getNumber(transformXML,ConstValues.A_Y,0);
				transform.skewX = getNumber(transformXML,ConstValues.A_SKEW_X,0) * ConstValues.ANGLE_TO_RADIAN;
				transform.skewY = getNumber(transformXML,ConstValues.A_SKEW_Y,0) * ConstValues.ANGLE_TO_RADIAN;
				transform.scaleX = getNumber(transformXML, ConstValues.A_SCALE_X, 1);
				transform.scaleY = getNumber(transformXML, ConstValues.A_SCALE_Y, 1);
			}
			if(pivot != null)
			{
				pivot.x = getNumber(transformXML,ConstValues.A_PIVOT_X,0);
				pivot.y = getNumber(transformXML,ConstValues.A_PIVOT_Y,0);
			}
		}
	}

	private static void parseColorTransform(XML colorTransformXML, ColorTransform colorTransform)
	{
		if(colorTransformXML != null)
		{
			if(colorTransform != null)
			{
				colorTransform.alphaOffset = getInt(colorTransformXML, ConstValues.A_ALPHA_OFFSET);
				colorTransform.redOffset = getInt(colorTransformXML, ConstValues.A_RED_OFFSET);
				colorTransform.greenOffset = getInt(colorTransformXML, ConstValues.A_GREEN_OFFSET);
				colorTransform.blueOffset = getInt(colorTransformXML, ConstValues.A_BLUE_OFFSET);

				colorTransform.alphaMultiplier = getInt(colorTransformXML, ConstValues.A_ALPHA_MULTIPLIER, 100) * 0.01;
				colorTransform.redMultiplier = getInt(colorTransformXML, ConstValues.A_RED_MULTIPLIER, 100) * 0.01;
				colorTransform.greenMultiplier = getInt(colorTransformXML, ConstValues.A_GREEN_MULTIPLIER, 100) * 0.01;
				colorTransform.blueMultiplier = getInt(colorTransformXML, ConstValues.A_BLUE_MULTIPLIER, 100) * 0.01;
			}
		}
	}
}
