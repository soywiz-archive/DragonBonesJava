package dragonBones.objects;

/** @private */
public class Frame
{
	public int position;
	public int duration;

	public String action;
	public String event;
	public String sound;
	public CurveData curve;

	public Frame()
	{
		position = 0;
		duration = 0;
	}

	public void dispose()
	{
	}
}
