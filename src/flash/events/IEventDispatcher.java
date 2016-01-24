package flash.events;

import flash.Runnable1;

public interface IEventDispatcher {
	void addEventListener(String type, Runnable1<Event> listener, boolean useCapture, int priority, boolean useWeakReference);
	boolean dispatchEvent(Event event);
	boolean hasEventListener(String type);
	void removeEventListener(String type, Runnable1<Event> listener, boolean useCapture);
	boolean willTrigger(String type);
}
