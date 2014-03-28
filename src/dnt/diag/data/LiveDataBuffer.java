package dnt.diag.data;

import java.util.Arrays;

public class LiveDataBuffer {
	private byte[] buff;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(buff);
		result = prime * result + length;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LiveDataBuffer other = (LiveDataBuffer) obj;
		if (!Arrays.equals(buff, other.buff))
			return false;
		if (length != other.length)
			return false;
		return true;
	}

	private int length;

	public LiveDataBuffer() {
		buff = new byte[100];
		length = 0;
	}

	public synchronized byte[] getBuff() {
		return buff;
	}

	public synchronized int getLength() {
		return length;
	}
	
	public synchronized void copyTo(byte[] buff, int offset, int length) {
		System.arraycopy(buff, offset, this.buff, 0, length);
		this.length = length;
	}
	
	public synchronized byte get(int index) {
		return buff[index];
	}
}
