package dragonBones.events;

/**
 * Copyright 2012-2013. DragonBones. All Rights Reserved.
 * @playerversion Flash 10.0
 * @langversion 3.0
 * @version 2.0
 */
import dragonBones.animation.AnimationState;

import flash.events.Event;

/**
 * The SoundEvent provides and defines all sound related events dispatched during an animation.
 *
 * @see dragonBones.Armature
 * @see dragonBones.animation.Animation
 */
public class SoundEvent extends Event
{
	/**
	 * Dispatched when the animation of the animation enter a frame containing sound labels.
	 */
	public static final String SOUND = "sound";

	/**
	 * The armature that is the target of this event.
	 */
	public Armature armature;

	public AnimationState animationState;

	public String sound;

	/**
	 * Creates a new SoundEvent instance.
	 * @param type
	 * @param cancelable
	 */
	public SoundEvent(String type, boolean cancelable = false)
	{
		super(type, false, cancelable);
	}

	/**
	 * @private
	 */
	@Override
	public Event clone()
	{
		SoundEvent event = new SoundEvent(type, cancelable);
		event.armature = armature;
		event.animationState = animationState;
		event.sound = sound;
		return event;
	}
}
