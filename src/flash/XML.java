package flash;

import org.w3c.dom.Node;

public class XML {
	private Node node;

	public XML(Node node) {
		this.node = node;
	}

	public String getString(String name) {
		throw new Error();
	}

	public int getInt(String name) {
		return Integer.parseInt(getString(name));
	}

	public int getInt(String name, int defaultValue) {
		try {
			return getInt(name);
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	public double getDouble(String name) {
		return getDouble(name, Double.NaN);
	}

	public double getDouble(String name, double defaultValue) {
		return Double.parseDouble(getString(name));
	}

	public XML[] children(String tag) {
		throw new Error();
	}

	public XML first(String tag) {
		return children(tag)[0];
	}
}
