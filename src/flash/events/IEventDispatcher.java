package flash.events;

import flash.Runnable1;

public interface IEventDispatcher {
	void addEventListener(String type, Runnable1<Event> listener, boolean useCapture, int priority, boolean useWeakReference);
	void addEventListener(String type, Runnable1<Event> listener);
	boolean dispatchEvent(Event event);
	boolean hasEventListener(String type);
	void removeEventListener(String type, Runnable1<Event> listener, boolean useCapture);
	void removeEventListener(String type, Runnable1<Event> listener);
	boolean willTrigger(String type);
}
