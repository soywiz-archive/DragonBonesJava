package dragonBones.core;

import java.util.Map;

public interface ICacheableArmature extends IArmature {
	boolean getEnableCache();

	void setEnableCache(boolean value);

	boolean getEnableEventDispatch();

	void setEnableEventDispatch(boolean value);

	Map<String, ISlotCacheGenerator> getSlotDic();
}
