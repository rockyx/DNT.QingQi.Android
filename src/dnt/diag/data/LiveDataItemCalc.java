package dnt.diag.data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public abstract class LiveDataItemCalc {
	protected Charset cs;
	protected ByteBuffer bb;
	protected LiveDataItem item;
	protected LiveDataBuffer buffer;
	protected String oldValue = null;

	public LiveDataItemCalc(LiveDataItem item) {
		cs = Charset.forName("US-ASCII");
		bb = ByteBuffer.allocate(100);
		this.item = item;
		this.buffer = item.getEcuResponseBuff();
	}

	protected abstract String calc();

	protected abstract boolean dataChanged();

	public String getValue() {
		if (oldValue == null) {
			oldValue = calc();
		} else if (dataChanged()) {
			oldValue = calc();
		}
		return oldValue;
	}
}
