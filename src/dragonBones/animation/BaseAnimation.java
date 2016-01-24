package dragonBones.animation;

import dragonBones.cache.AnimationCacheManager;
import dragonBones.core.IAnimationState;
import dragonBones.fast.animation.FastAnimationState;

abstract public class BaseAnimation {
	public AnimationCacheManager animationCacheManager;
	public IAnimationState ianimationState;
	abstract public FastAnimationState gotoAndPlay(String animationName, double fadeInTime, double duration, double playTimes);
}
