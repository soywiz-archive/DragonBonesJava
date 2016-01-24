package dragonBones.animation;

import dragonBones.cache.AnimationCacheManager;
import dragonBones.core.IAnimationState;

abstract public class BaseAnimation {
	public AnimationCacheManager animationCacheManager;
	public IAnimationState ianimationState;

	abstract public IAnimationState gotoAndPlay(String animationName, double fadeInTime, double duration, double playTimes);

	public IAnimationState gotoAndPlay(String animationName) {
		return gotoAndPlay(animationName, -1, -1, Double.NaN);
	}

	public IAnimationState gotoAndPlay(String animationName, double fadeInTime, double duration) {
		return gotoAndPlay(animationName, fadeInTime, duration, Double.NaN);
	}
}
