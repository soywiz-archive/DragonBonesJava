package dragonBones.core;
	import dragonBones.animation.Animation;
	import dragonBones.animation.BaseAnimation;
	import dragonBones.animation.IAnimatable;

	public interface IArmature extends IAnimatable
	{
		Animation getAnimation();
		void resetAnimation();
		
	}
