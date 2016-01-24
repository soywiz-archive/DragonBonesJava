package dragonBones.objects;

import dragonBones.core.DragonBones;
import dragonBones.core.dragonBones_internal;
import dragonBones.objects.AnimationData;
import dragonBones.objects.ArmatureData;
import dragonBones.objects.BoneData;
import dragonBones.objects.DBTransform;
import dragonBones.objects.DisplayData;
import dragonBones.objects.DragonBonesData;
import dragonBones.objects.Frame;
import dragonBones.objects.SkinData;
import dragonBones.objects.SlotData;
import dragonBones.objects.Timeline;
import dragonBones.objects.TransformFrame;
import dragonBones.objects.TransformTimeline;
import dragonBones.textures.TextureData;
import dragonBones.utils.ConstValues;
import dragonBones.utils.DBDataUtil;

import flash.geom.ColorTransform;
import flash.geom.Point;
import flash.geom.Rectangle;
import flash.utils.Dictionary;

import java.util.Map;

//use namespace dragonBones_internal;

public final class ObjectDataParser
{
	private static DragonBonesData tempDragonBonesData;

	public static Map<String, TextureData> parseTextureAtlasData(Object rawData, double scale = 1)
	{
		Object textureAtlasData = {};
		textureAtlasData.__name = rawData[ConstValues.A_NAME];
		Rectangle subTextureFrame;
		for (Object subTextureObject : rawData[ConstValues.SUB_TEXTURE])
		{
			String subTextureName = subTextureObject[ConstValues.A_NAME];
			Rectangle subTextureRegion = new Rectangle();
			subTextureRegion.x = int(subTextureObject[ConstValues.A_X]) / scale;
			subTextureRegion.y = int(subTextureObject[ConstValues.A_Y]) / scale;
			subTextureRegion.width = int(subTextureObject[ConstValues.A_WIDTH]) / scale;
			subTextureRegion.height = int(subTextureObject[ConstValues.A_HEIGHT]) / scale;

			boolean rotated = subTextureObject[ConstValues.A_ROTATED] == "true";

			double frameWidth = int(subTextureObject[ConstValues.A_FRAME_WIDTH]) / scale;
			double frameHeight = int(subTextureObject[ConstValues.A_FRAME_HEIGHT]) / scale;

			if(frameWidth > 0 && frameHeight > 0)
			{
				subTextureFrame = new Rectangle();
				subTextureFrame.x = int(subTextureObject[ConstValues.A_FRAME_X]) / scale;
				subTextureFrame.y = int(subTextureObject[ConstValues.A_FRAME_Y]) / scale;
				subTextureFrame.width = frameWidth;
				subTextureFrame.height = frameHeight;
			}
			else
			{
				subTextureFrame = null;
			}

			textureAtlasData[subTextureName] = new TextureData(subTextureRegion, subTextureFrame, rotated);
		}

		return textureAtlasData;
	}

	public static DragonBonesData parseDragonBonesData(Object rawDataToParse)
	{
		if(!rawDataToParse)
		{
			throw new ArgumentError();
		}

		String version = rawDataToParse[ConstValues.A_VERSION];
		switch (version)
		{
			case "2.3":
			case "3.0":
				return Object3DataParser.parseSkeletonData(rawDataToParse);
				break;
			case DragonBones.DATA_VERSION:
				break;

			default:
				throw new Error("Nonsupport version!");
		}

		int frameRate = int(rawDataToParse[ConstValues.A_FRAME_RATE]);

		DragonBonesData outputDragonBonesData=  new DragonBonesData();
		outputDragonBonesData.name = rawDataToParse[ConstValues.A_NAME];
		outputDragonBonesData.isGlobalData = rawDataToParse[ConstValues.A_IS_GLOBAL] == "0" ? false : true;
		tempDragonBonesData = outputDragonBonesData;

		for (Object armatureObject : rawDataToParse[ConstValues.ARMATURE])
		{
			outputDragonBonesData.addArmatureData(parseArmatureData(armatureObject, frameRate));
		}

		tempDragonBonesData = null;

		return outputDragonBonesData;
	}

	private static ArmatureData parseArmatureData(Object armatureDataToParse, int frameRate)
	{
		ArmatureData outputArmatureData = new ArmatureData();
		outputArmatureData.name = armatureDataToParse[ConstValues.A_NAME];

		for (Object boneObject : armatureDataToParse[ConstValues.BONE])
		{
			outputArmatureData.addBoneData(parseBoneData(boneObject));
		}

		for (Object slotObject : armatureDataToParse[ConstValues.SLOT])
		{
			outputArmatureData.addSlotData(parseSlotData(slotObject));
		}

		for (Object skinObject : armatureDataToParse[ConstValues.SKIN])
		{
			outputArmatureData.addSkinData(parseSkinData(skinObject));
		}

		if(tempDragonBonesData.isGlobalData)
		{
			DBDataUtil.transformArmatureData(outputArmatureData);
		}

		outputArmatureData.sortBoneDataList();

		for (Object animationObject : armatureDataToParse[ConstValues.ANIMATION])
		{
			AnimationData animationData = parseAnimationData(animationObject, frameRate);
			DBDataUtil.addHideTimeline(animationData, outputArmatureData, true);
			DBDataUtil.transformAnimationData(animationData, outputArmatureData, tempDragonBonesData.isGlobalData);
			outputArmatureData.addAnimationData(animationData);
		}

		return outputArmatureData;
	}

	//把bone的初始transform解析并返回
	private static BoneData parseBoneData(Object boneObject)
	{
		BoneData boneData = new BoneData();
		boneData.name = boneObject[ConstValues.A_NAME];
		boneData.parent = boneObject[ConstValues.A_PARENT];
		boneData.length = Number(boneObject[ConstValues.A_LENGTH]);
		boneData.inheritRotation = getBoolean(boneObject, ConstValues.A_INHERIT_ROTATION, true);
		boneData.inheritScale = getBoolean(boneObject, ConstValues.A_INHERIT_SCALE, true);

		parseTransform(boneObject[ConstValues.TRANSFORM], boneData.transform);
		if(tempDragonBonesData.isGlobalData)//绝对数据
		{
			boneData.global.copy(boneData.transform);
		}
		return boneData;
	}

	private static SkinData parseSkinData(Object skinObject)
	{
		SkinData skinData = new SkinData();
		skinData.name = skinObject[ConstValues.A_NAME];

		for (Object slotObject : skinObject[ConstValues.SLOT])
		{
			skinData.addSlotData(parseSlotDisplayData(slotObject));
		}

		return skinData;
	}

	private static SlotData parseSlotDisplayData(Object slotObject)
	{
		SlotData slotData = new SlotData();
		slotData.name = slotObject[ConstValues.A_NAME];
		for (Object displayObject : slotObject[ConstValues.DISPLAY])
		{
			slotData.addDisplayData(parseDisplayData(displayObject));
		}

		return slotData;
	}

	private static SlotData parseSlotData(Object slotObject)
	{
		SlotData slotData = new SlotData();
		slotData.name = slotObject[ConstValues.A_NAME];
		slotData.parent = slotObject[ConstValues.A_PARENT];
		slotData.zOrder = getNumber(slotObject, ConstValues.A_Z_ORDER, 0) || 0;
		slotData.blendMode = slotObject[ConstValues.A_BLENDMODE];
		slotData.displayIndex = slotObject[ConstValues.A_DISPLAY_INDEX];
		//for each(var displayObject:Object in slotObject[ConstValues.DISPLAY])
		//{
			//slotData.addDisplayData(parseDisplayData(displayObject));
		//}

		return slotData;
	}

	private static DisplayData parseDisplayData(Object displayObject)
	{
		DisplayData displayData = new DisplayData();
		displayData.name = displayObject[ConstValues.A_NAME];
		displayData.type = displayObject[ConstValues.A_TYPE];
		parseTransform(displayObject[ConstValues.TRANSFORM], displayData.transform, displayData.pivot);
		displayData.pivot.x = NaN;
		displayData.pivot.y = NaN;
		if(tempDragonBonesData!=null)
		{
			tempDragonBonesData.addDisplayData(displayData);
		}

		return displayData;
	}

	/** @private */
	private static AnimationData parseAnimationData(Object animationObject, int frameRate)
	{
		AnimationData animationData = new AnimationData();
		animationData.name = animationObject[ConstValues.A_NAME];
		animationData.frameRate = frameRate;
		animationData.duration = Math.ceil((Number(animationObject[ConstValues.A_DURATION]) || 1) * 1000 / frameRate);
		animationData.playTimes = int(getNumber(animationObject, ConstValues.A_PLAY_TIMES, 1));
		animationData.fadeTime = getNumber(animationObject, ConstValues.A_FADE_IN_TIME, 0) || 0;
		animationData.scale = getNumber(animationObject, ConstValues.A_SCALE, 1) || 0;
		//use frame tweenEase, NaN
		//overwrite frame tweenEase, [-1, 0):ease in, 0:line easing, (0, 1]:ease out, (1, 2]:ease in out
		animationData.tweenEasing = getNumber(animationObject, ConstValues.A_TWEEN_EASING, NaN);
		animationData.autoTween = getBoolean(animationObject, ConstValues.A_AUTO_TWEEN, true);

		for (Object frameObject : animationObject[ConstValues.FRAME])
		{
			Frame frame = parseTransformFrame(frameObject, frameRate);
			animationData.addFrame(frame);
		}

		parseTimeline(animationObject, animationData);

		int lastFrameDuration = animationData.duration;
		for (Object timelineObject : animationObject[ConstValues.BONE])
		{
			TransformTimeline timeline = parseTransformTimeline(timelineObject, animationData.duration, frameRate);
			if (timeline.frameList.length > 0)
			{
				lastFrameDuration = Math.min(lastFrameDuration, timeline.frameList[timeline.frameList.length - 1].duration);
				animationData.addTimeline(timeline);
			}

		}

		for (Object slotTimelineObject : animationObject[ConstValues.SLOT])
		{
			SlotTimeline slotTimeline = parseSlotTimeline(slotTimelineObject, animationData.duration, frameRate);
			if (slotTimeline.frameList.length > 0)
			{
				lastFrameDuration = Math.min(lastFrameDuration, slotTimeline.frameList[slotTimeline.frameList.length - 1].duration);
				animationData.addSlotTimeline(slotTimeline);
			}

		}

		if(animationData.frameList.length > 0)
		{
			lastFrameDuration = Math.min(lastFrameDuration, animationData.frameList[animationData.frameList.length - 1].duration);
		}
		//取得timeline中最小的lastFrameDuration并保存
		animationData.lastFrameDuration = lastFrameDuration;

		return animationData;
	}

	private static TransformTimeline parseTransformTimeline(Object timelineObject, int duration, int frameRate)
	{
		TransformTimeline outputTimeline = new TransformTimeline();
		outputTimeline.name = timelineObject[ConstValues.A_NAME];
		outputTimeline.scale = getNumber(timelineObject, ConstValues.A_SCALE, 1) || 0;
		outputTimeline.offset = getNumber(timelineObject, ConstValues.A_OFFSET, 0) || 0;
		outputTimeline.originPivot.x = getNumber(timelineObject, ConstValues.A_PIVOT_X, 0) || 0;
		outputTimeline.originPivot.y = getNumber(timelineObject, ConstValues.A_PIVOT_Y, 0) || 0;
		outputTimeline.duration = duration;

		for (Object frameObject : timelineObject[ConstValues.FRAME])
		{
			TransformFrame frame = parseTransformFrame(frameObject, frameRate);
			outputTimeline.addFrame(frame);
		}

		parseTimeline(timelineObject, outputTimeline);

		return outputTimeline;
	}

	private static SlotTimeline parseSlotTimeline(Object timelineObject, int duration, int frameRate)
	{
		SlotTimeline timeline = new SlotTimeline();
		timeline.name = timelineObject[ConstValues.A_NAME];
		timeline.scale = getNumber(timelineObject, ConstValues.A_SCALE, 1) || 0;
		timeline.offset = getNumber(timelineObject, ConstValues.A_OFFSET, 0) || 0;
		//timeline.originPivot.x = getNumber(timelineXML, ConstValues.A_PIVOT_X, 0) || 0;
		//timeline.originPivot.y = getNumber(timelineXML, ConstValues.A_PIVOT_Y, 0) || 0;
		timeline.duration = duration;

		for (Object frameObject : timelineObject[ConstValues.FRAME])
		{
			SlotFrame frame = parseSlotFrame(frameObject, frameRate);
			timeline.addFrame(frame);
		}

		parseTimeline(timelineObject, timeline);

		return timeline;
	}

	private static Frame parseMainFrame(Object frameObject, int frameRate)
	{
		Frame frame = new Frame();
		parseFrame(frameObject, frame, frameRate);
		return frame;
	}

	private static TransformFrame parseTransformFrame(Object frameObject, int frameRate)
	{
		TransformFrame outputFrame = new TransformFrame();
		parseFrame(frameObject, outputFrame, frameRate);

		outputFrame.visible = !getBoolean(frameObject, ConstValues.A_HIDE, false);

		//NaN:no tween, 10:auto tween, [-1, 0):ease in, 0:line easing, (0, 1]:ease out, (1, 2]:ease in out
		outputFrame.tweenEasing = getNumber(frameObject, ConstValues.A_TWEEN_EASING, 10);
		outputFrame.tweenRotate = int(getNumber(frameObject, ConstValues.A_TWEEN_ROTATE, 0));
		outputFrame.tweenScale = getBoolean(frameObject, ConstValues.A_TWEEN_SCALE, true);
//			outputFrame.displayIndex = int(getNumber(frameObject, ConstValues.A_DISPLAY_INDEX, 0));

		parseTransform(frameObject[ConstValues.TRANSFORM], outputFrame.transform, outputFrame.pivot);
		if(tempDragonBonesData.isGlobalData)//绝对数据
		{
			outputFrame.global.copy(outputFrame.transform);
		}

		outputFrame.scaleOffset.x = getNumber(frameObject, ConstValues.A_SCALE_X_OFFSET, 0) || 0;
		outputFrame.scaleOffset.y = getNumber(frameObject, ConstValues.A_SCALE_Y_OFFSET, 0) || 0;
		return outputFrame;
	}

	private static SlotFrame parseSlotFrame(Object frameObject, int frameRate)
	{
		SlotFrame frame = new SlotFrame();
		parseFrame(frameObject, frame, frameRate);

		frame.visible = !getBoolean(frameObject, ConstValues.A_HIDE, false);

		//NaN:no tween, 10:auto tween, [-1, 0):ease in, 0:line easing, (0, 1]:ease out, (1, 2]:ease in out
		frame.tweenEasing = getNumber(frameObject, ConstValues.A_TWEEN_EASING, 10);
		frame.displayIndex = int(getNumber(frameObject,ConstValues.A_DISPLAY_INDEX,0));

		//如果为NaN，则说明没有改变过zOrder
		frame.zOrder = getNumber(frameObject, ConstValues.A_Z_ORDER, tempDragonBonesData.isGlobalData ? NaN:0);

		Object colorTransformObject = frameObject[ConstValues.COLOR];
		if(colorTransformObject)
		{
			frame.color = new ColorTransform();
			parseColorTransform(colorTransformObject, frame.color);
		}

		return frame;
	}

	private static void parseTimeline(Object timelineObject, Timeline outputTimeline)
	{
		int position = 0;
		Frame frame;
		for (frame : outputTimeline.frameList.getKeys())
		{
			frame.position = position;
			position += frame.duration;
		}
		//防止duration计算有误差
		if(frame)
		{
			frame.duration = outputTimeline.duration - frame.position;
		}
	}

	private static void parseFrame(Object frameObject, Frame outputFrame, int frameRate):void
	{
		outputFrame.duration = Math.round((Number(frameObject[ConstValues.A_DURATION])) * 1000 / frameRate);
		outputFrame.action = frameObject[ConstValues.A_ACTION];
		outputFrame.event = frameObject[ConstValues.A_EVENT];
		outputFrame.sound = frameObject[ConstValues.A_SOUND];
		if (frameObject[ConstValues.A_CURVE] != null && frameObject[ConstValues.A_CURVE].length == 4)
		{
			outputFrame.curve = new CurveData();
			outputFrame.curve.pointList = [new Point(frameObject[ConstValues.A_CURVE][0],
													 frameObject[ConstValues.A_CURVE][1]),
										   new Point(frameObject[ConstValues.A_CURVE][2],
													 frameObject[ConstValues.A_CURVE][3])];
		}
	}

	private static function parseTransform(transformObject:Object, transform:DBTransform, pivot:Point = null):void
	{
		if(transformObject)
		{
			if(transform)
			{
				transform.x = getNumber(transformObject,ConstValues.A_X,0) || 0;
				transform.y = getNumber(transformObject,ConstValues.A_Y,0) || 0;
				transform.skewX = getNumber(transformObject,ConstValues.A_SKEW_X,0) * ConstValues.ANGLE_TO_RADIAN || 0;
				transform.skewY = getNumber(transformObject,ConstValues.A_SKEW_Y,0) * ConstValues.ANGLE_TO_RADIAN || 0;
				transform.scaleX = getNumber(transformObject, ConstValues.A_SCALE_X, 1) || 0;
				transform.scaleY = getNumber(transformObject, ConstValues.A_SCALE_Y, 1) || 0;
			}
			if(pivot)
			{
				pivot.x = getNumber(transformObject,ConstValues.A_PIVOT_X,0) || 0;
				pivot.y = getNumber(transformObject,ConstValues.A_PIVOT_Y,0) || 0;
			}
		}
	}

	private static void parseColorTransform(Object colorTransformObject, ColorTransform colorTransform)
	{
		if(colorTransformObject != null)
		{
			if(colorTransform != null)
			{
				colorTransform.alphaOffset = (int)(colorTransformObject[ConstValues.A_ALPHA_OFFSET]);
				colorTransform.redOffset = (int)(colorTransformObject[ConstValues.A_RED_OFFSET]);
				colorTransform.greenOffset = (int)(colorTransformObject[ConstValues.A_GREEN_OFFSET]);
				colorTransform.blueOffset = (int)(colorTransformObject[ConstValues.A_BLUE_OFFSET]);

				colorTransform.alphaMultiplier = (int)(getNumber(colorTransformObject, ConstValues.A_ALPHA_MULTIPLIER,100)) * 0.01;
				colorTransform.redMultiplier = (int)(getNumber(colorTransformObject,ConstValues.A_RED_MULTIPLIER,100)) * 0.01;
				colorTransform.greenMultiplier = (int)(getNumber(colorTransformObject,ConstValues.A_GREEN_MULTIPLIER,100)) * 0.01;
				colorTransform.blueMultiplier = (int)(getNumber(colorTransformObject,ConstValues.A_BLUE_MULTIPLIER,100)) * 0.01;
			}
		}
	}

	private static function getBoolean(data:Object, key:String, defaultValue:Boolean):Boolean
	{
		if(data && key in data)
		{
			switch(String(data[key]))
			{
				case "0":
				case "NaN":
				case "":
				case "false":
				case "null":
				case "undefined":
					return false;

				case "1":
				case "true":
				default:
					return true;
			}
		}
		return defaultValue;
	}

	private static double getNumber(Map<String, Object> data, String key, double defaultValue)
	{
		if(data != null && data.containsKey(key))
		{
			Object value = data.get(key);
			if (value instanceof Double) return (double) value;
			if (value instanceof String) {
				switch((String)value)
				{
					case "NaN":
					case "":
					case "false":
					case "null":
					case "undefined":
						return Double.NaN;
				}
			}
		}
		return defaultValue;
	}
}
