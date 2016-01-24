package flash.events;

public class Event {
	static public final String COMPLETE = "complete";

	public Object target;
	public String type;

	public Event(String type) {
		this(type, false, false);
	}

	public Event(String type, boolean b, boolean cancelable) {
		this.type = type;
	}
}
