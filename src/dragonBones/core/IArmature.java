package dragonBones.core;

import dragonBones.animation.Animation;
import dragonBones.animation.IAnimatable;

public interface IArmature extends IAnimatable {
	IAnimation getAnimation();

	void resetAnimation();
}
