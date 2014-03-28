package dnt.diag.ecu.visteon;

import dnt.diag.ProtocolType;
import dnt.diag.attribute.Attribute;
import dnt.diag.channel.ChannelException;
import dnt.diag.channel.ChannelFactory;
import dnt.diag.commbox.Commbox;
import dnt.diag.db.VehicleDB;
import dnt.diag.ecu.AbstractECU;
import dnt.diag.ecu.DiagException;
import dnt.diag.formats.ISO9141Format;

public class Powertrain extends AbstractECU {

	PowertrainModel model;

	public Powertrain(VehicleDB db, Commbox box, PowertrainModel model) {
		super(db, box);
		this.model = model;
		Attribute attr = getAttribute();
		switch (model) {
		case QM250J_2L:
			attr.klineAddrCode = 0x33;
			attr.klineComLine = 7;
			attr.isoHeader = 0x68;
			attr.klineL = true;
			attr.klineSourceAddress = 0xF1;
			attr.klineTargetAddress = 0x6A;
			setChannel(ChannelFactory.create(attr, getCommbox(),
					ProtocolType.ISO9141_2));
			setFormat(new ISO9141Format(attr));
			setTroubleCode(new PowertrainTroubleCode(this));
			setDataStream(new PowertrainDataStream(this, false));
			setFreezeStream(new PowertrainDataStream(this, true));
			break;
		default:
			throw new DiagException("Unsupport model!!!");
		}
	}

	@Override
	public void channelInit() {
		try {
			getChannel().startCommunicate();
		} catch (ChannelException ex) {
			throw new DiagException(ex.getMessage());
		}
	}

	protected PowertrainModel getModel() {
		return model;
	}
}
