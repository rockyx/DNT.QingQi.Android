package dnt.diag.data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public abstract class LiveDataItemCalc {
	protected Charset cs;
	protected ByteBuffer bb;
	protected LiveDataItem item;
	protected LiveDataBuffer buffer;

	public LiveDataItemCalc(LiveDataItem item) {
		cs = Charset.forName("US-ASCII");
		bb = ByteBuffer.allocate(100);
		this.item = item;
		this.buffer = item.getEcuResponseBuff();
	}

	public abstract String calc();
}
