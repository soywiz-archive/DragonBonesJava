package dragonBones.core;
	import dragonBones.animation.BaseAnimation;
	import dragonBones.animation.IAnimatable;

	public interface IArmature extends IAnimatable
	{
		BaseAnimation getAnimation();
		void resetAnimation();
		
	}
