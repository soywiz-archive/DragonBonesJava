package dragonBones.utils;

import flash.geom.Matrix;
import flash.geom.Point;

import dragonBones.objects.AnimationData;
import dragonBones.objects.ArmatureData;
import dragonBones.objects.BoneData;
import dragonBones.objects.DBTransform;
import dragonBones.objects.Frame;
import dragonBones.objects.SkinData;
import dragonBones.objects.SlotData;
import dragonBones.objects.SlotFrame;
import dragonBones.objects.SlotTimeline;
import dragonBones.objects.TransformFrame;
import dragonBones.objects.TransformTimeline;

import java.util.ArrayList;
import java.util.Objects;

/** @private */
public final class DBDataUtil
{
	public static void transformArmatureData(ArmatureData armatureData)
	{
		ArrayList<BoneData> boneDataList = armatureData.getBoneDataList();
		int i = boneDataList.size();

		while(i -- > 0)
		{
			BoneData boneData = boneDataList.get(i);
			if(boneData.parent != null)
			{
				BoneData parentBoneData = armatureData.getBoneData(boneData.parent);
				if(parentBoneData != null)
				{
					boneData.transform.copy(boneData.global);
					boneData.transform.divParent(parentBoneData.global);
//						TransformUtil.globalToLocal(boneData.transform, parentBoneData.global);
				}
			}
		}
	}

	public static void transformArmatureDataAnimations(ArmatureData armatureData)
	{
		ArrayList<AnimationData> animationDataList = armatureData.getAnimationDataList();
		int i = animationDataList.size();
		while(i -- > 0)
		{
			transformAnimationData(animationDataList.get(i), armatureData, false);
		}
	}

	public static void transformRelativeAnimationData(AnimationData animationData, ArmatureData armatureData)
	{

	}

	public static void transformAnimationData(AnimationData animationData, ArmatureData armatureData, boolean isGlobalData)
	{
		if(!isGlobalData)
		{
			transformRelativeAnimationData(animationData, armatureData);
			return;
		}

		SkinData skinData = armatureData.getSkinData(null);
		ArrayList<BoneData> boneDataList = armatureData.getBoneDataList();
		ArrayList<SlotData> slotDataList = null;
		if(skinData != null)
		{
			slotDataList = armatureData.getSlotDataList();
		}

		for(int i = 0;i < boneDataList.size();i ++)
		{
			BoneData boneData = boneDataList.get(i);
			//绝对数据是不可能有slotTimeline的
			TransformTimeline timeline = animationData.getTimeline(boneData.name);
			SlotTimeline slotTimeline = animationData.getSlotTimeline(boneData.name);
			if(timeline == null && slotTimeline == null)
			{
				continue;
			}

			SlotData slotData = null;
			if(slotDataList != null)
			{
				for (SlotData slotData2 : slotDataList)
				{
					//找到属于当前Bone的slot(FLash Pro制作的动画一个Bone只包含一个slot)
					if(Objects.equals(slotData2.parent, boneData.name))
					{
						break;
					}
				}
			}

			ArrayList<Frame> frameList = timeline.getFrameList();
			ArrayList<Frame> slotFrameList = null;
			if (slotTimeline != null)
			{
				slotFrameList = slotTimeline.getFrameList();
			}

			DBTransform originTransform = null;
			Point originPivot = null;
			TransformFrame prevFrame = null;
			int frameListLength = frameList.size();
			for(int j = 0;j < frameListLength;j ++)
			{
				TransformFrame frame = (TransformFrame) frameList.get(j);
				//计算frame的transform信息
				setFrameTransform(animationData, armatureData, boneData, frame);

				//转换成相对骨架的transform信息
				frame.transform.x -= boneData.transform.x;
				frame.transform.y -= boneData.transform.y;
				frame.transform.skewX -= boneData.transform.skewX;
				frame.transform.skewY -= boneData.transform.skewY;
				frame.transform.scaleX /= boneData.transform.scaleX;
				frame.transform.scaleY /= boneData.transform.scaleY;

				//if(!timeline.transformed)
				//{
					//if(slotData)
					//{
						////frame.zOrder -= slotData.zOrder;
					//}
				//}

				//如果originTransform不存在说明当前帧是第一帧，将当前帧的transform保存至timeline的originTransform
				//if(!originTransform)
				//{
					//originTransform = timeline.originTransform;
					//originTransform.copy(frame.transform);
					//originTransform.skewX = TransformUtil.formatRadian(originTransform.skewX);
					//originTransform.skewY = TransformUtil.formatRadian(originTransform.skewY);
					//originPivot = timeline.originPivot;
					//originPivot.x = frame.pivot.x;
					//originPivot.y = frame.pivot.y;
				//}
				//
				//frame.transform.x -= originTransform.x;
				//frame.transform.y -= originTransform.y;
				//frame.transform.skewX = TransformUtil.formatRadian(frame.transform.skewX - originTransform.skewX);
				//frame.transform.skewY = TransformUtil.formatRadian(frame.transform.skewY - originTransform.skewY);
				//frame.transform.scaleX /= originTransform.scaleX;
				//frame.transform.scaleY /= originTransform.scaleY;
				//
				//if(!timeline.transformed)
				//{
					//frame.pivot.x -= originPivot.x;
					//frame.pivot.y -= originPivot.y;
				//}

				if(prevFrame != null)
				{
					double dLX = frame.transform.skewX - prevFrame.transform.skewX;

					if(prevFrame.tweenRotate != 0)
					{

						if(prevFrame.tweenRotate > 0)
						{
							if(dLX < 0)
							{
								frame.transform.skewX += Math.PI * 2;
								frame.transform.skewY += Math.PI * 2;
							}

							if(prevFrame.tweenRotate > 1)
							{
								frame.transform.skewX += Math.PI * 2 * (prevFrame.tweenRotate - 1);
								frame.transform.skewY += Math.PI * 2 * (prevFrame.tweenRotate - 1);
							}
						}
						else
						{
							if(dLX > 0)
							{
								frame.transform.skewX -= Math.PI * 2;
								frame.transform.skewY -= Math.PI * 2;
							}

							if(prevFrame.tweenRotate < 1)
							{
								frame.transform.skewX += Math.PI * 2 * (prevFrame.tweenRotate + 1);
								frame.transform.skewY += Math.PI * 2 * (prevFrame.tweenRotate + 1);
							}
						}
					}
					else
					{
						frame.transform.skewX = prevFrame.transform.skewX + TransformUtil.formatRadian(frame.transform.skewX - prevFrame.transform.skewX);
						frame.transform.skewY = prevFrame.transform.skewY + TransformUtil.formatRadian(frame.transform.skewY - prevFrame.transform.skewY);
					}
				}
				prevFrame = frame;
			}

			if (slotTimeline != null && slotFrameList != null)
			{
				frameListLength = slotFrameList.size();
				for(int j = 0;j < frameListLength;j ++)
				{
					SlotFrame slotFrame = (SlotFrame) slotFrameList.get(j);

					if(!slotTimeline.transformed)
					{
						if(slotData != null)
						{
							slotFrame.zOrder -= slotData.zOrder;
						}
					}
				}
				slotTimeline.transformed = true;
			}

			timeline.transformed = true;

		}
	}

	//计算frame的transoform信息
	private static void setFrameTransform(AnimationData animationData, ArmatureData armatureData, BoneData boneData, TransformFrame frame)
	{
		frame.transform.copy(frame.global);
		//找到当前bone的父亲列表 并将timeline信息存入parentTimelineList 将boneData信息存入parentDataList
		BoneData parentData = armatureData.getBoneData(boneData.parent);
		if(parentData != null)
		{
			TransformTimeline parentTimeline = animationData.getTimeline(parentData.name);
			if(parentTimeline != null)
			{
				ArrayList<TransformTimeline> parentTimelineList = new ArrayList<TransformTimeline>();
				ArrayList<BoneData> parentDataList = new ArrayList<BoneData>();
				while(parentTimeline != null)
				{
					parentTimelineList.add(parentTimeline);
					parentDataList.add(parentData);
					parentData = armatureData.getBoneData(parentData.parent);
					if(parentData != null)
					{
						parentTimeline = animationData.getTimeline(parentData.name);
					}
					else
					{
						parentTimeline = null;
					}
				}

				int i = parentTimelineList.size();

				DBTransform globalTransform = null;
				Matrix globalTransformMatrix = new Matrix();

				DBTransform currentTransform = new DBTransform();
				Matrix currentTransformMatrix = new Matrix();
				//从根开始遍历
				while(i -- > 0)
				{
					parentTimeline = parentTimelineList.get(i);
					parentData = parentDataList.get(i);
					//一级一级找到当前帧对应的每个父节点的transform(相对transform) 保存到currentTransform，globalTransform保存根节点的transform
					getTimelineTransform(parentTimeline, frame.position, currentTransform, globalTransform == null);

					if(globalTransform == null)
					{
						globalTransform = new DBTransform();
						globalTransform.copy(currentTransform);
					}
					else
					{
						currentTransform.x += parentTimeline.originTransform.x + parentData.transform.x;
						currentTransform.y += parentTimeline.originTransform.y + parentData.transform.y;

						currentTransform.skewX += parentTimeline.originTransform.skewX + parentData.transform.skewX;
						currentTransform.skewY += parentTimeline.originTransform.skewY + parentData.transform.skewY;

						currentTransform.scaleX *= parentTimeline.originTransform.scaleX * parentData.transform.scaleX;
						currentTransform.scaleY *= parentTimeline.originTransform.scaleY * parentData.transform.scaleY;

						TransformUtil.transformToMatrix(currentTransform, currentTransformMatrix);
						currentTransformMatrix.concat(globalTransformMatrix);
						TransformUtil.matrixToTransform(currentTransformMatrix, globalTransform, currentTransform.scaleX * globalTransform.scaleX >= 0, currentTransform.scaleY * globalTransform.scaleY >= 0);

					}

					TransformUtil.transformToMatrix(globalTransform, globalTransformMatrix);
				}
//					TransformUtil.globalToLocal(frame.transform, globalTransform);	
				frame.transform.divParent(globalTransform);
			}
		}
	}

	private static void getTimelineTransform(TransformTimeline timeline, int position, DBTransform retult, boolean isGlobal)
	{
		ArrayList<Frame> frameList = timeline.getFrameList();
		int i = frameList.size();

		while(i -- > 0)
		{
			TransformFrame currentFrame = (TransformFrame) frameList.get(i);
			//找到穿越当前帧的关键帧
			if(currentFrame.position <= position && currentFrame.position + currentFrame.duration > position)
			{
				//是最后一帧或者就是当前帧
				if(i == frameList.size() - 1 || position == currentFrame.position)
				{
					retult.copy(isGlobal?currentFrame.global:currentFrame.transform);
				}
				else
				{
					double tweenEasing = currentFrame.tweenEasing;
					double progress = (position - currentFrame.position) / currentFrame.duration;
					if(tweenEasing != 0 && tweenEasing != 10)
					{
						progress = MathUtil.getEaseValue(progress, tweenEasing);
					}
					TransformFrame nextFrame = (TransformFrame) frameList.get(i + 1);

					DBTransform currentTransform = isGlobal?currentFrame.global:currentFrame.transform;
					DBTransform nextTransform = isGlobal?nextFrame.global:nextFrame.transform;

					retult.x = currentTransform.x + (nextTransform.x - currentTransform.x) * progress;
					retult.y = currentTransform.y + (nextTransform.y - currentTransform.y) * progress;
					retult.skewX = TransformUtil.formatRadian(currentTransform.skewX + (nextTransform.skewX - currentTransform.skewX) * progress);
					retult.skewY = TransformUtil.formatRadian(currentTransform.skewY + (nextTransform.skewY - currentTransform.skewY) * progress);
					retult.scaleX = currentTransform.scaleX + (nextTransform.scaleX - currentTransform.scaleX) * progress;
					retult.scaleY = currentTransform.scaleY + (nextTransform.scaleY - currentTransform.scaleY) * progress;
				}
				break;
			}
		}
	}

	public static void addHideTimeline(AnimationData animationData, ArmatureData armatureData)
	{
		addHideTimeline(animationData, armatureData, false);
	}

	public static void addHideTimeline(AnimationData animationData, ArmatureData armatureData, boolean addHideSlot)
	{
		ArrayList<BoneData> boneDataList =armatureData.getBoneDataList();
		ArrayList<SlotData> slotDataList =armatureData.getSlotDataList();
		int i = boneDataList.size();

		while(i -- > 0)
		{
			BoneData boneData = boneDataList.get(i);
			String boneName = boneData.name;
			if(animationData.getTimeline(boneName) == null)
			{
				if(animationData.hideTimelineNameMap.indexOf(boneName) < 0)
				{
					animationData.hideTimelineNameMap.add(boneName);
				}
			}
		}
		if (addHideSlot)
		{
			i = slotDataList.size();
			SlotData slotData;
			String slotName;
			while (i-- > 0)
			{
				slotData = slotDataList.get(i);
				slotName = slotData.name;
				if (animationData.getSlotTimeline(slotName) == null)
				{
					if (animationData.hideSlotTimelineNameMap.indexOf(slotName) < 0)
					{
						animationData.hideSlotTimelineNameMap.add(slotName);
					}
				}
			}
		}

	}
}
