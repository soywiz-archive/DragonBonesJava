package dragonBones.core;

import dragonBones.cache.AnimationCacheManager;
import dragonBones.objects.AnimationData;

import java.util.ArrayList;

public interface IAnimation {
	void setAnimationDataList(ArrayList<AnimationData> value);
	boolean hasAnimation(String animationName);
	IAnimationState getAnimationState();
	IAnimationState gotoAndPlay(String name);
	IAnimationState gotoAndPlay(String animationName, double fadeInTime, double duration, double playTimes);
	void setAnimationCacheManager(AnimationCacheManager animationCacheManager);
	void play();
	void stop();
}
