package flash.events;

public class Event {
	public Object target;
	public String type;

	public Event(String type, boolean b, boolean cancelable) {
		this.type = type;
	}
}
