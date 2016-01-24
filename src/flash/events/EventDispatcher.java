package flash.events;

import flash.Runnable1;

public class EventDispatcher implements IEventDispatcher {
	public EventDispatcher() {
	}

	public EventDispatcher(IEventDispatcher dispatcher) {
	}

	@Override
	public void addEventListener(String type, Runnable1<Event> listener, boolean useCapture, int priority, boolean useWeakReference) {
		throw new RuntimeException();
	}

	@Override
	public void addEventListener(String type, Runnable1<Event> listener) {
		throw new RuntimeException();
	}

	@Override
	public boolean dispatchEvent(Event event) {
		throw new RuntimeException();
	}

	@Override
	public boolean hasEventListener(String type) {
		throw new RuntimeException();
	}

	@Override
	public void removeEventListener(String type, Runnable1<Event> listener, boolean useCapture) {
		throw new RuntimeException();
	}

	@Override
	public void removeEventListener(String type, Runnable1<Event> listener) {
		throw new RuntimeException();
	}

	@Override
	public boolean willTrigger(String type) {
		throw new RuntimeException();
	}
}
