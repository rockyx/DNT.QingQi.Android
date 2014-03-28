package dnt.diag.channel;

import dnt.diag.Timer;

public interface Channel {
	public void startHeartbeat(Timer interval, byte[] data, int offset,
			int count) throws ChannelException;

	public void stopHeartbeat() throws ChannelException;

	public void setByteTxInterval(Timer interval) throws ChannelException;

	public void setFrameTxInterval(Timer interval) throws ChannelException;

	public void setByteRxTimeout(Timer timeout) throws ChannelException;

	public void setFrameRxTimeout(Timer timeout) throws ChannelException;

	public int sendAndRecv(byte[] sData, int sOffset, int sCount, byte[] output)
			throws ChannelException;

	public void startCommunicate() throws ChannelException;
}
