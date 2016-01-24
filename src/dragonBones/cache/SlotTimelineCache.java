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
		cache.globalTransform.copy(cacheGenerator.getGlobal());
		cache.globalTransformMatrix.copyFrom(cacheGenerator.getGlobalTransformMatrix());
		if(cacheGenerator.getColorChanged())
		{
			cache.colorTransform =  ColorTransformUtil.cloneColor(cacheGenerator.getColorTransform());
		}
		cache.displayIndex = cacheGenerator.getDisplayIndex();
		frameCacheList.add(cache);
	}
}
