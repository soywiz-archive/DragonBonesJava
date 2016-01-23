package dragonBones.cache;
import dragonBones.core.ICacheUser;

import java.util.ArrayList;

public class TimelineCache
{
	public String name;
	public ArrayList<FrameCache> frameCacheList = new ArrayList<FrameCache>();
	public FrameCache currentFrameCache;
	public TimelineCache()
	{
	}

	public void  addFrame()
	{
	}
	public void update(int frameIndex)
	{
		currentFrameCache.copy(frameCacheList.get(frameIndex));
	}

	public void bindCacheUser(ICacheUser cacheUser)
	{
		cacheUser.frameCache = currentFrameCache;
	}
}
