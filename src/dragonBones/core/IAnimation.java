package dragonBones.core;

import dragonBones.objects.AnimationData;

import java.util.ArrayList;

public interface IAnimation {
	void setAnimationDataList(ArrayList<AnimationData> value);
	boolean hasAnimation(String animationName);
	IAnimationState getAnimationState();
	IAnimationState gotoAndPlay(String name);
	void play();
	void stop();
}
