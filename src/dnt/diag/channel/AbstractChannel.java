package dnt.diag.channel;

import dnt.diag.Timer;
import dnt.diag.attribute.Attribute;
import dnt.diag.commbox.Commbox;

public abstract class AbstractChannel implements Channel {
	
	private Attribute attr;
	private Commbox commbox;

	public AbstractChannel(Attribute attr, Commbox commbox) {
		this.attr = attr;
		this.commbox = commbox;
	}
	
	protected Commbox getCommbox() {
		return commbox;
	}

	protected Attribute getAttribute() {
		return attr;
	}

	public void startHeartbeat(Timer interval, byte... bs) throws ChannelException {
		startHeartbeat(interval, bs, 0, bs.length);
	}

	protected void leftShiftBuff(byte[] buff, int shiftSize, int length) {
		System.arraycopy(buff, shiftSize, buff, 0, length);
	}

}
