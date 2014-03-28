package dnt.diag.formats;

import dnt.diag.attribute.Attribute;

public abstract class AbstractFormat implements Format {

	private Attribute attr;

	public AbstractFormat(Attribute attr) {
		this.attr = attr;
	}

	protected Attribute getAttribute() {
		return attr;
	}

	public abstract byte[] pack(byte[] src, int offset, int count);

	public abstract byte[] unpack(byte[] src, int offset, int count);

	public byte[] pack(byte... src) {
		return pack(src, 0, src.length);
	}

	public byte[] unpack(byte... src) {
		return unpack(src, 0, src.length);
	}
}
