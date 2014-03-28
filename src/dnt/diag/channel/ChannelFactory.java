package dnt.diag.channel;

import dnt.diag.ProtocolType;
import dnt.diag.attribute.Attribute;
import dnt.diag.commbox.Commbox;

public class ChannelFactory {
	public static Channel create(Attribute attr, Commbox box, ProtocolType type) {
		switch (type) {
		case MikuniECU200:
			return new MikuniECU200Channel(attr, box);
		case MikuniECU300:
			return new MikuniECU300Channel(attr, box);
		case ISO14230:
			return new ISO14230Channel(attr, box);
		case ISO9141_2:
			return new ISO9141Channel(attr, box);
		default:
			return null;
		}
	}
}
