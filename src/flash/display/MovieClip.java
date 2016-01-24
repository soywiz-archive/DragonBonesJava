package flash.display;

public class MovieClip extends DisplayObjectContainer {
	private int height;
	private int width;

	public void stop() {
		throw new Error();
	}

	public void gotoAndStop(int index) {
		throw new Error();
	}

	public int getTotalFrames() {
		throw new Error();
	}
}
