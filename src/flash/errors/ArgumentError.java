package flash.errors;

public class ArgumentError extends RuntimeException {
	public ArgumentError() {
	}

	public ArgumentError(String message) {
		super(message);
	}
}
