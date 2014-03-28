package dnt.diag.formats;

public interface Format {
	public byte[] pack(byte[] src, int offset, int count);

	public byte[] unpack(byte[] src, int offset, int count);

	public byte[] pack(byte... src);

	public byte[] unpack(byte... src);
}
