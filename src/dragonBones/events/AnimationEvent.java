package dragonBones.events;

/**
* Copyright 2012-2013. DragonBones. All Rights Reserved.
* @playerversion Flash 10.0, Flash 10
* @langversion 3.0
* @version 2.0
*/
import flash.events.Event;

/**
 * The AnimationEvent provides and defines all events dispatched during an animation.
 *
 * @see dragonBones.Armature
 * @see dragonBones.animation.Animation
 */
public class AnimationEvent extends Event
{
	/**
	 * 不推荐使用.
	 */
	public static String getMOVEMENT_CHANGE()
	{
		return FADE_IN;
	}

	/**
	 * Dispatched when the playback of an animation fade in.
	 */
	public static final String FADE_IN = "fadeIn";

	/**
	 * Dispatched when the playback of an animation fade out.
	 */
	public static final String FADE_OUT = "fadeOut";

	/**
	 * Dispatched when the playback of an animation starts.
	 */
	public static final String START = "start";

	/**
	 * Dispatched when the playback of a animation stops.
	 */
	public static final String COMPLETE = "complete";

	/**
	 * Dispatched when the playback of a animation completes a loop.
	 */
	public static final String LOOP_COMPLETE = "loopComplete";

	/**
	 * Dispatched when the playback of an animation fade in complete.
	 */
	public static final String FADE_IN_COMPLETE = "fadeInComplete";

	/**
	 * Dispatched when the playback of an animation fade out complete.
	 */
	public static final String FADE_OUT_COMPLETE = "fadeOutComplete";

	/**
	 * 不推荐的API.
	 */
	public String getMovementID()
	{
		return animationName;
	}

	/**
	 * The animationState instance.
	 */
	public Object animationState;

	/**
	 * The armature that is the taget of this event.
	 */
	public Armature getArmature()
	{
		return (Armature)target;
	}

	public String getAnimationName()
	{
		return animationState.name;
	}

	/**
	 * Creates a new AnimationEvent instance.
	 * @param type
	 * @param cancelable
	 */
	public AnimationEvent(String type, boolean cancelable)
	{
		super(type, false, cancelable);
	}

	public AnimationEvent(String type)
	{
		this(type, false);
	}

	/**
	 * @private
	 * @return
	 */
	@Override public Event clone()
	{
		AnimationEvent event = new AnimationEvent(type, cancelable);
		event.animationState = animationState;
		return event;
	}
}
