package dragonBones.cache;
import dragonBones.core.IAnimationState;
import dragonBones.core.ICacheUser;
import dragonBones.core.ICacheableArmature;
import dragonBones.core.ISlotCacheGenerator;
import dragonBones.objects.AnimationData;
import dragonBones.objects.ArmatureData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//use namespace dragonBones_internal;

public class AnimationCacheManager
{
	public ICacheableArmature cacheGeneratorArmature;
	public ArmatureData armatureData;
	public double frameRate;
	public Map<String, AnimationCache> animationCacheDic = new HashMap<>();
//		public Object boneFrameCacheDic = {};
	public Map<String, FrameCache> slotFrameCacheDic = new HashMap<>();
	public AnimationCacheManager()
	{
	}

	public static AnimationCacheManager initWithArmatureData(ArmatureData armatureData)
	{
		return initWithArmatureData(armatureData, 0);
	}

	public static AnimationCacheManager initWithArmatureData(ArmatureData armatureData, double frameRate)
	{
		AnimationCacheManager output = new AnimationCacheManager();
		output.armatureData = armatureData;
		if(frameRate<=0)
		{
			AnimationData animationData = armatureData.getAnimationDataList().get(0);
			if(animationData != null)
			{
				output.frameRate = animationData.frameRate;
			}
		}
		else
		{
			output.frameRate = frameRate;
		}

		return output;
	}

	public void initAllAnimationCache()
	{
		for (AnimationData animationData : armatureData.getAnimationDataList())
		{
			animationCacheDic.put(animationData.name, AnimationCache.initWithAnimationData(animationData,armatureData));
		}
	}

	public void initAnimationCache(String animationName)
	{
		animationCacheDic.put(animationName, AnimationCache.initWithAnimationData(armatureData.getAnimationData(animationName),armatureData));
	}

	public void bindCacheUserArmatures(ArrayList<ICacheableArmature> armatures)
	{
		for (ICacheableArmature armature : armatures)
		{
			bindCacheUserArmature(armature);
		}

	}

	public void bindCacheUserArmature(ICacheableArmature armature)
	{
		armature.getAnimation().animationCacheManager = this;

		Map<String, ISlotCacheGenerator> slotDic = armature.getSlotDic();
//			for each(cacheUser in armature._boneDic)
//			{
//				cacheUser.frameCache = boneFrameCacheDic[cacheUser.name];
//			}
		for (ISlotCacheGenerator cacheUser : slotDic.values())
		{
			cacheUser.setFrameCache(slotFrameCacheDic.get(cacheUser.getName()));
		}
	}

	public void setCacheGeneratorArmature(ICacheableArmature armature)
	{
		cacheGeneratorArmature = armature;

		Map<String, ISlotCacheGenerator> slotDic = armature.getSlotDic();

//			for each(cacheUser in armature._boneDic)
//			{
//				boneFrameCacheDic[cacheUser.name] = new FrameCache();
//			}
		for (ISlotCacheGenerator cacheUser : armature.getSlotDic().values())
		{
			slotFrameCacheDic.put(cacheUser.getName(), new SlotFrameCache());
		}

		for (AnimationCache animationCache : animationCacheDic.values())
		{
//				animationCache.initBoneTimelineCacheDic(armature._boneDic, boneFrameCacheDic);
			animationCache.initSlotTimelineCacheDic(slotDic, slotFrameCacheDic);
		}
	}

	public void generateAllAnimationCache(boolean loop)
	{
		for (AnimationCache animationCache : animationCacheDic.values())
		{
			generateAnimationCache(animationCache.name, loop);
		}
	}

	public void generateAnimationCache(String animationName, boolean loop)
	{
		boolean temp = cacheGeneratorArmature.getEnableCache();
		cacheGeneratorArmature.setEnableCache(false);
		AnimationCache animationCache = animationCacheDic.get(animationName);
		if(animationCache == null)
		{
			return;
		}

		IAnimationState animationState = cacheGeneratorArmature.getAnimation().ianimationState;
		double passTime = 1 / frameRate;

		if (loop)
		{
			cacheGeneratorArmature.getAnimation().gotoAndPlay(animationName,0,-1,0);
		}
		else
		{
			cacheGeneratorArmature.getAnimation().gotoAndPlay(animationName,0,-1,1);
		}

		boolean tempEnableEventDispatch = cacheGeneratorArmature.getEnableEventDispatch();
		cacheGeneratorArmature.setEnableEventDispatch(false);
		double lastProgress;
		do
		{
			lastProgress = animationState.getProgress();
			cacheGeneratorArmature.advanceTime(passTime);
			animationCache.addFrame();
		}
		while (animationState.getProgress() >= lastProgress && animationState.getProgress() < 1);

		cacheGeneratorArmature.setEnableEventDispatch(tempEnableEventDispatch);
		resetCacheGeneratorArmature();
		cacheGeneratorArmature.setEnableCache(temp);
	}

	/**
	 * 将缓存生成器骨架重置，生成动画缓存后调用。
	 */
	public void resetCacheGeneratorArmature()
	{
		cacheGeneratorArmature.resetAnimation();
	}

	public AnimationCache getAnimationCache(String animationName)
	{
		return animationCacheDic.get(animationName);
	}
}
