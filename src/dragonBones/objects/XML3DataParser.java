package dragonBones.objects;

import dragonBones.utils.ConstValues;
import dragonBones.utils.DBDataUtil;
import flash.XML;
import flash.errors.ArgumentError;
import flash.geom.ColorTransform;
import flash.geom.Point;

import java.util.Map;
import java.util.Objects;

import static dragonBones.utils.XMLUtils.*;

/**
 * ...
 * @author sukui
 */
final public class XML3DataParser {
	private static DragonBonesData tempDragonBonesData;

	public XML3DataParser() {
	}

	public static DragonBonesData parseSkeletonData(XML rawData) {
		return parseSkeletonData(rawData, false, null);
	}

	/**
	 * Parse the SkeletonData.
	 * @param rawData The SkeletonData xml to parse.
	 * @return A SkeletonData instance.
	 */
	//public static DragonBonesData parseSkeletonData(XML rawData, boolean ifSkipAnimationData= false, Map<String, Object> outputAnimationDictionary= null)
	public static DragonBonesData parseSkeletonData(XML rawData, boolean ifSkipAnimationData, Map<String, Object> outputAnimationDictionary)
	{
		if(rawData == null)
		{
			throw new ArgumentError();
		}
		String version = rawData.getString(ConstValues.A_VERSION);
		switch (version)
		{
			case "2.3":
				//Update2_3To3_0.format(rawData as XML);
				break;

			case "3.0":
				break;

			default:
				throw new Error("Nonsupport version!");
		}

		int frameRate = rawData.getInt(ConstValues.A_FRAME_RATE);

		DragonBonesData data = new DragonBonesData();
		tempDragonBonesData = data;
		data.name = rawData.getString(ConstValues.A_NAME);
		boolean isGlobalData = Objects.equals(rawData.getString(ConstValues.A_IS_GLOBAL), "0") ? false : true;
		for (XML armatureXML : rawData.children(ConstValues.ARMATURE))
		{
			data.addArmatureData(parseArmatureData(armatureXML, data, frameRate, isGlobalData, ifSkipAnimationData, outputAnimationDictionary));
		}

		return data;
	}

	private static ArmatureData parseArmatureData(XML armatureXML, DragonBonesData data, int frameRate, boolean isGlobalData, boolean ifSkipAnimationData, Map<String, Object> outputAnimationDictionary)
	{
		ArmatureData armatureData = new ArmatureData();
		armatureData.name = armatureXML.getString(ConstValues.A_NAME);

		for (XML boneXML : armatureXML.children(ConstValues.BONE))
		{
			armatureData.addBoneData(parseBoneData(boneXML, isGlobalData));
		}

		for ( XML skinXml : armatureXML.children(ConstValues.SKIN))
		{
			for (XML slotXML : skinXml.children(ConstValues.SLOT))
			{
				armatureData.addSlotData(parseSlotData(slotXML));
			}
		}
		for (XML skinXML : armatureXML.children(ConstValues.SKIN))
		{
			armatureData.addSkinData(parseSkinData(skinXML, data));
		}

		if(isGlobalData)
		{
			DBDataUtil.transformArmatureData(armatureData);
		}
		armatureData.sortBoneDataList();

		if(ifSkipAnimationData)
		{
			//if(outputAnimationDictionary!= null)
			//{
				//outputAnimationDictionary[armatureData.name] = new Dictionary();
			//}
			//
			//var index:int = 0;
			//for each(animationXML in armatureXML[ConstValues.ANIMATION])
			//{
				//if(index == 0)
				//{
					//armatureData.addAnimationData(parseAnimationData(animationXML, armatureData, frameRate, isGlobalData));
				//}
				//else if(outputAnimationDictionary != null)
				//{
					//outputAnimationDictionary[armatureData.name][animationXML.@[ConstValues.A_NAME]] = animationXML;
				//}
				//index++;
			//}
		}
		else
		{
			for (XML animationXML : armatureXML.children(ConstValues.ANIMATION))
			{
				//var animationData:AnimationData = parseAnimationData(animationXML, frameRate);
				//DBDataUtil.addHideTimeline(animationData, outputArmatureData);
				//DBDataUtil.transformAnimationData(animationData, outputArmatureData, tempDragonBonesData.isGlobalData);
				//outputArmatureData.addAnimationData(animationData);
				armatureData.addAnimationData(parseAnimationData(animationXML, armatureData, frameRate, isGlobalData));
			}
		}

		//for each(var rectangleXML:XML in armatureXML[ConstValues.RECTANGLE])
		//{
			//armatureData.addAreaData(parseRectangleData(rectangleXML));
		//}
		//
		//for each(var ellipseXML:XML in armatureXML[ConstValues.ELLIPSE])
		//{
			//armatureData.addAreaData(parseEllipseData(ellipseXML));
		//}

		return armatureData;
	}

	private static BoneData parseBoneData(XML boneXML, boolean isGlobalData)
	{
		BoneData boneData = new BoneData();
		boneData.name = boneXML.getString(ConstValues.A_NAME);
		boneData.parent = boneXML.getString(ConstValues.A_PARENT);
		boneData.length = boneXML.getDouble(ConstValues.A_LENGTH);
		boneData.inheritRotation = getBoolean(boneXML, ConstValues.A_INHERIT_ROTATION, true);
		boneData.inheritScale = getBoolean(boneXML, ConstValues.A_INHERIT_SCALE, true);

		parseTransform(boneXML.children(ConstValues.TRANSFORM)[0], boneData.transform, null);
		if(isGlobalData)//绝对数据
		{
			boneData.global.copy(boneData.transform);
		}

		//for each(var rectangleXML:XML in boneXML[ConstValues.RECTANGLE])
		//{
			//boneData.addAreaData(parseRectangleData(rectangleXML));
		//}
		//
		//for each(var ellipseXML:XML in boneXML[ConstValues.ELLIPSE])
		//{
			//boneData.addAreaData(parseEllipseData(ellipseXML));
		//}

		return boneData;
	}

	private static RectangleData parseRectangleData(XML rectangleXML)
	{
		RectangleData rectangleData = new RectangleData();
		rectangleData.name = rectangleXML.getString(ConstValues.A_NAME);
		rectangleData.width = rectangleXML.getDouble(ConstValues.A_WIDTH);
		rectangleData.height = rectangleXML.getDouble(ConstValues.A_HEIGHT);

		parseTransform(rectangleXML.children(ConstValues.TRANSFORM)[0], rectangleData.transform, rectangleData.pivot);

		return rectangleData;
	}

	private static EllipseData parseEllipseData(XML ellipseXML)
	{
		EllipseData ellipseData = new EllipseData();
		ellipseData.name = ellipseXML.getString(ConstValues.A_NAME);
		ellipseData.width = ellipseXML.getDouble(ConstValues.A_WIDTH);
		ellipseData.height = ellipseXML.getDouble(ConstValues.A_HEIGHT);

		parseTransform(ellipseXML.children(ConstValues.TRANSFORM)[0], ellipseData.transform, ellipseData.pivot);

		return ellipseData;
	}

	private static SlotData parseSlotData(XML slotXML)
	{
		SlotData slotData = new SlotData();
		slotData.name = getString(slotXML, ConstValues.A_NAME);
		slotData.parent = getString(slotXML, ConstValues.A_PARENT);
		slotData.zOrder = getNumber(slotXML,ConstValues.A_Z_ORDER,0);
		slotData.blendMode = slotXML.getString(ConstValues.A_BLENDMODE);
		slotData.displayIndex = 0;
		return slotData;
	}

	private static SkinData parseSkinData(XML skinXML, DragonBonesData data)
	{
		SkinData skinData = new SkinData();
		skinData.name = skinXML.getString(ConstValues.A_NAME);

		for (XML slotXML : skinXML.children(ConstValues.SLOT))
		{
			skinData.addSlotData(parseSkinSlotData(slotXML, data));
		}

		return skinData;
	}

	private static SlotData parseSkinSlotData(XML slotXML, DragonBonesData data)
	{
		SlotData slotData = new SlotData();
		slotData.name = slotXML.getString(ConstValues.A_NAME);
		slotData.parent = slotXML.getString(ConstValues.A_PARENT);
		slotData.zOrder = getNumber(slotXML, ConstValues.A_Z_ORDER, 0);
		slotData.blendMode = slotXML.getString(ConstValues.A_BLENDMODE);
		for (XML displayXML : slotXML.children(ConstValues.DISPLAY))
		{
			slotData.addDisplayData(parseDisplayData(displayXML, data));
		}

		return slotData;
	}

	private static DisplayData parseDisplayData(XML displayXML, DragonBonesData data)
	{
		DisplayData displayData = new DisplayData();
		displayData.name = displayXML.getString(ConstValues.A_NAME);
		displayData.type = displayXML.getString(ConstValues.A_TYPE);

		displayData.pivot = new Point();
		//displayData.pivot = data.addSubTexturePivot(
			//0,
			//0,
			//displayData.name
		//);

		parseTransform(displayXML.children(ConstValues.TRANSFORM)[0], displayData.transform, displayData.pivot);

		if (tempDragonBonesData != null)
		{
			tempDragonBonesData.addDisplayData(displayData);
		}
		return displayData;
	}

	/** @private */
	static AnimationData parseAnimationData(XML animationXML, ArmatureData armatureData, int frameRate, boolean isGlobalData)
	{
		AnimationData animationData = new AnimationData();
		animationData.name = animationXML.getString(ConstValues.A_NAME);
		animationData.frameRate = frameRate;
		animationData.duration = Math.round((getInt(animationXML, ConstValues.A_DURATION, 1)) * 1000 / frameRate);
		animationData.playTimes = getInt(animationXML, ConstValues.A_LOOP, 1);
		animationData.fadeTime = getNumber(animationXML, ConstValues.A_FADE_IN_TIME, 0);
		animationData.scale = getNumber(animationXML, ConstValues.A_SCALE, 1);
		//use frame tweenEase, NaN
		//overwrite frame tweenEase, [-1, 0):ease in, 0:line easing, (0, 1]:ease out, (1, 2]:ease in out
		animationData.tweenEasing = getNumber(animationXML, ConstValues.A_TWEEN_EASING, Double.NaN);
		animationData.autoTween = getBoolean(animationXML, ConstValues.A_AUTO_TWEEN, true);

		for (XML frameXML : animationXML.children(ConstValues.FRAME))
		{
			Frame frame = parseTransformFrame(frameXML, frameRate, isGlobalData);
			animationData.addFrame(frame);
		}

		parseTimeline(animationXML, animationData);

		int lastFrameDuration = animationData.duration;
		for (XML timelineXML : animationXML.children(ConstValues.TIMELINE))
		{
			TransformTimeline timeline = parseTransformTimeline(timelineXML, animationData.duration, frameRate, isGlobalData);
			lastFrameDuration = Math.min(lastFrameDuration, timeline.getFrameList().get(timeline.getFrameList().size() - 1).duration);
			animationData.addTimeline(timeline);

			SlotTimeline slotTimeline = parseSlotTimeline(timelineXML, animationData.duration, frameRate, isGlobalData);
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

		DBDataUtil.addHideTimeline(animationData, armatureData);
		DBDataUtil.transformAnimationData(animationData, armatureData, isGlobalData);

		return animationData;
	}

	private static SlotTimeline parseSlotTimeline(XML timelineXML, int duration, int frameRate, boolean isGlobalData)
	{
		SlotTimeline timeline = new SlotTimeline();
		timeline.name = getString(timelineXML, ConstValues.A_NAME);
		timeline.scale = getNumber(timelineXML, ConstValues.A_SCALE, 1);
		timeline.offset = getNumber(timelineXML, ConstValues.A_OFFSET, 0);
		timeline.duration = duration;

		for (XML frameXML : timelineXML.children(ConstValues.FRAME))
		{
			SlotFrame frame = parseSlotFrame(frameXML, frameRate, isGlobalData);
			timeline.addFrame(frame);
		}

		parseTimeline(timelineXML, timeline);

		return timeline;
	}

	private static SlotFrame parseSlotFrame(XML frameXML, int frameRate, boolean isGlobalData)
	{
		SlotFrame frame = new SlotFrame();
		parseFrame(frameXML, frame, frameRate);

		frame.visible = !getBoolean(frameXML, ConstValues.A_HIDE, false);

		//NaN:no tween, 10:auto tween, [-1, 0):ease in, 0:line easing, (0, 1]:ease out, (1, 2]:ease in out
		frame.tweenEasing = getNumber(frameXML, ConstValues.A_TWEEN_EASING, 10);
		frame.displayIndex = getInt(frameXML,ConstValues.A_DISPLAY_INDEX,0);

		//如果为NaN，则说明没有改变过zOrder
		frame.zOrder = getNumber(frameXML, ConstValues.A_Z_ORDER, isGlobalData ? Double.NaN:0);

		XML colorTransformXML = frameXML.children(ConstValues.COLOR_TRANSFORM)[0];
		if(colorTransformXML != null)
		{
			frame.color = new ColorTransform();
			parseColorTransform(colorTransformXML, frame.color);
		}

		return frame;
	}

	private static TransformTimeline parseTransformTimeline(XML timelineXML, int duration, int frameRate, boolean isGlobalData)
	{
		TransformTimeline timeline = new TransformTimeline();
		timeline.name = getString(timelineXML, ConstValues.A_NAME, "");
		timeline.scale = getNumber(timelineXML, ConstValues.A_SCALE, 1);
		timeline.offset = getNumber(timelineXML, ConstValues.A_OFFSET, 0);
		timeline.originPivot.x = getNumber(timelineXML, ConstValues.A_PIVOT_X, 0);
		timeline.originPivot.y = getNumber(timelineXML, ConstValues.A_PIVOT_Y, 0);
		timeline.duration = duration;

		for (XML frameXML : timelineXML.children(ConstValues.FRAME))
		{
			TransformFrame frame = parseTransformFrame(frameXML, frameRate, isGlobalData);
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

	private static TransformFrame parseTransformFrame(XML frameXML, int frameRate, boolean isGlobalData)
	{
		TransformFrame frame = new TransformFrame();
		parseFrame(frameXML, frame, frameRate);

		frame.visible = !getBoolean(frameXML, ConstValues.A_HIDE, false);

		//NaN:no tween, 10:auto tween, [-1, 0):ease in, 0:line easing, (0, 1]:ease out, (1, 2]:ease in out
		frame.tweenEasing = getNumber(frameXML, ConstValues.A_TWEEN_EASING, 10);
		frame.tweenRotate = getInt(frameXML, ConstValues.A_TWEEN_ROTATE,0);
		frame.tweenScale = getBoolean(frameXML, ConstValues.A_TWEEN_SCALE, true);
		//frame.displayIndex = int(getNumber(frameXML, ConstValues.A_DISPLAY_INDEX, 0));

		//如果为NaN，则说明没有改变过zOrder
		//frame.zOrder = getNumber(frameXML, ConstValues.A_Z_ORDER, isGlobalData ? NaN : 0);

		parseTransform(frameXML.children(ConstValues.TRANSFORM)[0], frame.transform, frame.pivot);
		if(isGlobalData)//绝对数据
		{
			frame.global.copy(frame.transform);
		}

		frame.scaleOffset.x = getNumber(frameXML, ConstValues.A_SCALE_X_OFFSET, 0);
		frame.scaleOffset.y = getNumber(frameXML, ConstValues.A_SCALE_Y_OFFSET, 0);

		//var colorTransformXML:XML = frameXML[ConstValues.COLOR_TRANSFORM][0];
		//if(colorTransformXML)
		//{
			//frame.color = new ColorTransform();
			//parseColorTransform(colorTransformXML, frame.color);
		//}

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
		frame.duration = Math.round((frameXML.getInt(ConstValues.A_DURATION, 1)) * 1000 / frameRate);
		frame.action = frameXML.getString(ConstValues.A_ACTION);
		frame.event = frameXML.getString(ConstValues.A_EVENT);
		frame.sound = frameXML.getString(ConstValues.A_SOUND);
	}

	//private static void parseTransform(XML transformXML, DBTransform transform, Point pivot = null)
	private static void parseTransform(XML transformXML, DBTransform transform, Point pivot)
	{
		if(transformXML != null)
		{
			if(transform != null)
			{
				transform.x = getNumber(transformXML, ConstValues.A_X, 0);
				transform.y = getNumber(transformXML, ConstValues.A_Y, 0);
				transform.skewX = getNumber(transformXML, ConstValues.A_SKEW_X, 0) * ConstValues.ANGLE_TO_RADIAN;
				transform.skewY = getNumber(transformXML, ConstValues.A_SKEW_Y, 0) * ConstValues.ANGLE_TO_RADIAN;
				transform.scaleX = getNumber(transformXML, ConstValues.A_SCALE_X, 1);
				transform.scaleY = getNumber(transformXML, ConstValues.A_SCALE_Y, 1);
			}
			if(pivot != null)
			{
				pivot.x = getNumber(transformXML, ConstValues.A_PIVOT_X, 0);
				pivot.y = getNumber(transformXML, ConstValues.A_PIVOT_Y, 0);
			}
		}
	}

	private static void parseColorTransform(XML colorTransformXML, ColorTransform colorTransform)
	{
		if(colorTransformXML != null)
		{
			if(colorTransform != null)
			{
				colorTransform.alphaOffset = colorTransformXML.getInt(ConstValues.A_ALPHA_OFFSET);
				colorTransform.redOffset = colorTransformXML.getInt(ConstValues.A_RED_OFFSET);
				colorTransform.greenOffset = colorTransformXML.getInt(ConstValues.A_GREEN_OFFSET);
				colorTransform.blueOffset = colorTransformXML.getInt(ConstValues.A_BLUE_OFFSET);

				colorTransform.alphaMultiplier = (int)(getNumber(colorTransformXML, ConstValues.A_ALPHA_MULTIPLIER, 100)) * 0.01;
				colorTransform.redMultiplier = (int)(getNumber(colorTransformXML, ConstValues.A_RED_MULTIPLIER, 100)) * 0.01;
				colorTransform.greenMultiplier = (int)(getNumber(colorTransformXML, ConstValues.A_GREEN_MULTIPLIER, 100)) * 0.01;
				colorTransform.blueMultiplier = (int)(getNumber(colorTransformXML, ConstValues.A_BLUE_MULTIPLIER, 100)) * 0.01;
			}
		}
	}

}

