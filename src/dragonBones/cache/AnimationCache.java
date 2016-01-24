package dragonBones.cache;
import dragonBones.objects.AnimationData;
import dragonBones.objects.ArmatureData;
import dragonBones.objects.BoneData;
import dragonBones.objects.SlotData;
import dragonBones.objects.SlotTimeline;
import dragonBones.objects.TransformTimeline;

import java.util.ArrayList;
import java.util.Objects;

public class AnimationCache
{
	public String name;
//		public var boneTimelineCacheList:Vector.<BoneTimelineCache> = new Vector.<BoneTimelineCache>();
	public ArrayList<SlotTimelineCache> slotTimelineCacheList = new ArrayList<SlotTimelineCache>();
//		public var boneTimelineCacheDic:Object = {};
	public Object slotTimelineCacheDic = {};
	public int frameNum = 0;
	public AnimationCache()
	{
	}

	public static AnimationCache initWithAnimationData(AnimationData animationData,ArmatureData armatureData)
	{
		AnimationCache output = new AnimationCache();
		output.name = animationData.name;

		ArrayList<TransformTimeline> boneTimelineList = animationData.getTimelineList();
		String boneName;
		BoneData boneData;
		SlotData slotData;
		SlotTimelineCache slotTimelineCache;
		String slotName;

		for(int i = 0, length = boneTimelineList.size(); i < length; i++)
		{
			boneName = boneTimelineList.get(i).name;
			for (int j = 0, jlen = armatureData.getSlotDataList().size(); j < jlen; j++)
			{
				slotData = armatureData.getSlotDataList().get(j);
				slotName = slotData.name;
				if (Objects.equals(slotData.parent, boneName))
				{
					if (output.slotTimelineCacheDic[slotName] == null)
					{
						slotTimelineCache = new SlotTimelineCache();
						slotTimelineCache.name = slotName;
						output.slotTimelineCacheList.add(slotTimelineCache);
						output.slotTimelineCacheDic[slotName] = slotTimelineCache;
					}

				}
			}
		}
		return output;
	}

//		public function initBoneTimelineCacheDic(boneCacheGeneratorDic:Object, boneFrameCacheDic:Object):void
//		{
//			var name:String;
//			for each(var boneTimelineCache:BoneTimelineCache in boneTimelineCacheDic)
//			{
//				name = boneTimelineCache.name;
//				boneTimelineCache.cacheGenerator = boneCacheGeneratorDic[name];
//				boneTimelineCache.currentFrameCache = boneFrameCacheDic[name];
//			}
//		}

	public void initSlotTimelineCacheDic(Object slotCacheGeneratorDic, Object slotFrameCacheDic)
	{
		String name;
		for (SlotTimelineCache slotTimelineCache : slotTimelineCacheDic)
		{
			name = slotTimelineCache.name;
			slotTimelineCache.cacheGenerator = slotCacheGeneratorDic[name];
			slotTimelineCache.currentFrameCache = slotFrameCacheDic[name];
		}
	}

//		public function bindCacheUserBoneDic(boneDic:Object):void
//		{
//			for(var name:String in boneDic)
//			{
//				(boneTimelineCacheDic[name] as BoneTimelineCache).bindCacheUser(boneDic[name]);
//			}
//		}

	public void bindCacheUserSlotDic(Object slotDic)
	{
		for(String name : slotDic)
		{
			((SlotTimelineCache)slotTimelineCacheDic[name]).bindCacheUser(slotDic[name]);
		}
	}

	public void addFrame()
	{
		frameNum++;
//			var boneTimelineCache:BoneTimelineCache;
//			for(var i:int = 0, length:int = boneTimelineCacheList.length; i < length; i++)
//			{
//				boneTimelineCache = boneTimelineCacheList[i];
//				boneTimelineCache.addFrame();
//			}

		SlotTimelineCache slotTimelineCache;
		for(int i = 0, length = slotTimelineCacheList.size(); i < length; i++)
		{
			slotTimelineCache = slotTimelineCacheList.get(i);
			slotTimelineCache.addFrame();
		}
	}


	public void update(double progress)
	{
		int frameIndex = progress * (frameNum-1);

//			var boneTimelineCache:BoneTimelineCache;
//			for(var i:int = 0, length:int = boneTimelineCacheList.length; i < length; i++)
//			{
//				boneTimelineCache = boneTimelineCacheList[i];
//				boneTimelineCache.update(frameIndex);
//			}

		SlotTimelineCache slotTimelineCache;
		for(int i = 0, length = slotTimelineCacheList.size(); i < length; i++)
		{
			slotTimelineCache = slotTimelineCacheList.get(i);
			slotTimelineCache.update(frameIndex);
		}
	}
}
