package dragonBones.core;

public interface ICacheableArmature extends IArmature
{
	boolean getEnableCache();
	void setEnableCache(boolean value);

	boolean getEnableEventDispatch();
	void setEnableEventDispatch(boolean value);

	Object getSlotDic();
}
