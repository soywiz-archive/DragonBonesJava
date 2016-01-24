package dragonBones.cache;
import dragonBones.core.IAnimationState;
import dragonBones.core.ICacheUser;
import dragonBones.core.ICacheableArmature;
import dragonBones.objects.AnimationData;
import dragonBones.objects.ArmatureData;

import java.util.ArrayList;

//use namespace dragonBones_internal;

public class AnimationCacheManager
{
	public ICacheableArmature cacheGeneratorArmature
	public ArmatureData armatureData;
	public double frameRate;
	public Object animationCacheDic = {};
//		public Object boneFrameCacheDic = {};
	public Object slotFrameCacheDic = {};
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
			animationCacheDic[animationData.name] = AnimationCache.initWithAnimationData(animationData,armatureData);
		}
	}

	public void initAnimationCache(String animationName)
	{
		animationCacheDic[animationName] = AnimationCache.initWithAnimationData(armatureData.getAnimationData(animationName),armatureData);
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

		Object slotDic = armature.getSlotDic();
		ICacheUser cacheUser;
//			for each(cacheUser in armature._boneDic)
//			{
//				cacheUser.frameCache = boneFrameCacheDic[cacheUser.name];
//			}
		for (Object cacheUser : slotDic)
		{
			cacheUser.frameCache = slotFrameCacheDic[cacheUser.name];
		}
	}

	public void setCacheGeneratorArmature(ICacheableArmature armature)
	{
		cacheGeneratorArmature = armature;

		Object slotDic = armature.getSlotDic();
		ICacheUser cacheUser;
//			for each(cacheUser in armature._boneDic)
//			{
//				boneFrameCacheDic[cacheUser.name] = new FrameCache();
//			}
		for (Object cacheUser : armature.getSlotDic())
		{
			slotFrameCacheDic[cacheUser.name] = new SlotFrameCache();
		}

		for (AnimationCache animationCache in animationCacheDic)
		{
//				animationCache.initBoneTimelineCacheDic(armature._boneDic, boneFrameCacheDic);
			animationCache.initSlotTimelineCacheDic(slotDic, slotFrameCacheDic);
		}
	}

	public void generateAllAnimationCache(boolean loop)
	{
		for (AnimationCache animationCache : animationCacheDic)
		{
			generateAnimationCache(animationCache.name, loop);
		}
	}

	public void generateAnimationCache(String animationName, boolean loop)
	{
		boolean temp = cacheGeneratorArmature.enableCache;
		cacheGeneratorArmature.enableCache = false;
		AnimationCache animationCache = animationCacheDic[animationName];
		if(!animationCache)
		{
			return;
		}

		IAnimationState animationState = cacheGeneratorArmature.getAnimation().animationState;
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
		return animationCacheDic[animationName];
	}
}
