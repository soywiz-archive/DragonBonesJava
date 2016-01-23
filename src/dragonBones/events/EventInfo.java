package dragonBones.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * Created by soywiz on 23/1/16.
 */
@Target(ElementType.TYPE)
@Repeatable(EventInfos.class)
public @interface EventInfo {
	String name();

	String type();
}
