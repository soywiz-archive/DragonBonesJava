package dragonBones.objects;

import flash.geom.Point;

public final class SlotTimeline extends Timeline
{
	public String name;
	public boolean transformed;

	public double offset;

	public SlotTimeline()
	{
		super();
		offset = 0;
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}
}
