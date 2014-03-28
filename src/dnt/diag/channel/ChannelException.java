package dnt.diag.channel;

import java.io.IOException;

public class ChannelException extends IOException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8937572560232113614L;

	public ChannelException() {

	}

	public ChannelException(String msg) {
		super(msg);
	}
}
