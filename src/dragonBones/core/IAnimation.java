package dragonBones.core;

public interface IAnimation {
	boolean hasAnimation(String animationName);
	IAnimationState getAnimationState();
	IAnimationState gotoAndPlay(String name);
	void play();
	void stop();
}
