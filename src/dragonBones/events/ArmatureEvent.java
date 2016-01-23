package dragonBones.events;

/**
* Copyright 2012-2013. DragonBones. All Rights Reserved.
* @playerversion Flash 10.0, Flash 10
* @langversion 3.0
* @version 2.0
*/
import flash.events.Event;
/**
 * The ArmatureEvent provides and defines all events dispatched directly by an Armature instance.
 *
 *
 * @see dragonBones.animation.Animation
 */
public class ArmatureEvent extends Event
{

	/**
	 * Dispatched after a successful z order update.
	 */
	public static final String Z_ORDER_UPDATED = "zOrderUpdated";

	public ArmatureEvent(String type)
	{
		super(type, false, false);
	}

	/**
	 * @private
	 * @return
	 */
	@Override public Event clone()
	{
		return new ArmatureEvent(type);
	}
}
