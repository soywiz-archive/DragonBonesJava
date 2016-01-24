package flash.utils;

public class ByteArray {
	public byte[] bytes;
	private int length;
	private int position;

	public ByteArray(byte[] bytes) {
		this(bytes, 0, bytes.length);
	}

	public ByteArray(byte[] bytes, int position, int length) {
		this.bytes = bytes;
		this.position = position;
		this.length = length;
	}

	public void writeBytes(ByteArray that) {
	}

	public void writeBytes(ByteArray that, int pos, int len) {
	}

	public void compress() {
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void writeInt(int value) {
	}


	public void setPosition(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public void writeObject(Object object) {
		throw new Error();
	}

	public byte get(int index) {
		throw new Error();
	}

	public int readInt() {
		throw new Error();
	}

	public void uncompress() {
		throw new Error();
	}

	public Object readObject() {
		throw new Error();
	}
}
