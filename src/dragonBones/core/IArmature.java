package dragonBones.core;
	import dragonBones.animation.IAnimatable;

	public interface IArmature extends IAnimatable
	{
		Object getAnimation();
		void resetAnimation();
		
	}
