package dragonBones.events;

/**
* Copyright 2012-2013. DragonBones. All Rights Reserved.
* @playerversion Flash 10.0, Flash 10
* @langversion 3.0
* @version 2.0
*/
import flash.events.Event;

/**
 * The FrameEvent class provides and defines all events dispatched by an Animation or Bone instance entering a new frame.
 *
 *
 * @see dragonBones.animation.Animation
 */
public class FrameEvent extends Event
{
	public static String getMOVEMENT_FRAME_EVENT()
	{
		return  ANIMATION_FRAME_EVENT;
	}

	/**
	 * Dispatched when the animation of the armatrue enter a frame.
	 */
	public static final String ANIMATION_FRAME_EVENT = "animationFrameEvent";

	/**
	 *
	 */
	public static final String BONE_FRAME_EVENT ="boneFrameEvent";

	/**
	 * The entered frame label.
	 */
	public String frameLabel;

	public Object bone;

	/**
	 * The armature that is the target of this event.
	 */
	public Armature getArmature()
	{
		return (Armature)target;
	}

	/**
	 * The animationState instance.
	 */
	public Object animationState;

	/**
	 * Creates a new FrameEvent instance.
	 * @param type
	 * @param cancelable
	 */
	public FrameEvent(String type, boolean cancelable = false)
	{
		super(type, false, cancelable);
	}

	/**
	 * @private
	 *
	 * @return An exact duplicate of the current object.
	 */
	@Override
	public Event clone()
	{
		FrameEvent event = new FrameEvent(type, cancelable);
		event.animationState = animationState;
		event.bone = bone;
		event.animationState = animationState;
		event.frameLabel = frameLabel;
		return event;
	}
}
