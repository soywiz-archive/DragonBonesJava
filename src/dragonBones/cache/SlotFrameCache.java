package dragonBones.cache
{
	import flash.geom.ColorTransform;

	public class SlotFrameCache extends FrameCache
	{
		public ColorTransform colorTransform;
		public int displayIndex = -1;
//		public var zOrder:int;
		public SlotFrameCache()
		{
			super();
		}
		
		//浅拷贝提高效率
		@Override
		public void copy(FrameCache frameCache)
		{
			super.copy(frameCache);
			colorTransform = ((SlotFrameCache)frameCache).colorTransform;
			displayIndex = ((SlotFrameCache)frameCache).displayIndex;
		}
		
		@Override
		public void clear()
		{
			super.clear();
			colorTransform = null;
			displayIndex = -1;
		}
	}
}