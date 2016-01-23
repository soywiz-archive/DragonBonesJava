package dragonBones.cache;

import dragonBones.core.ISlotCacheGenerator;
import dragonBones.utils.ColorTransformUtil;

public class SlotTimelineCache extends TimelineCache
{
	public ISlotCacheGenerator cacheGenerator;
	public SlotTimelineCache()
	{
		super();
	}

	@Override
	public void addFrame()
	{
		SlotFrameCache cache = new SlotFrameCache();
		cache.globalTransform.copy(cacheGenerator.global);
		cache.globalTransformMatrix.copyFrom(cacheGenerator.globalTransformMatrix);
		if(cacheGenerator.colorChanged)
		{
			cache.colorTransform =  ColorTransformUtil.cloneColor(cacheGenerator.colorTransform);
		}
		cache.displayIndex = cacheGenerator.displayIndex;
		frameCacheList.push(cache);
	}
}
