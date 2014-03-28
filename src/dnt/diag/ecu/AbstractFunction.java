package dnt.diag.ecu;

import dnt.diag.channel.Channel;
import dnt.diag.db.VehicleDB;
import dnt.diag.formats.Format;

public abstract class AbstractFunction {
	private VehicleDB db;
	private Channel chn;
	private Format format;

	public AbstractFunction(VehicleDB db, Channel chn, Format format) {
		this.db = db;
		this.chn = chn;
		this.format = format;
	}

	protected VehicleDB getDB() {
		return db;
	}

	protected Channel getChannel() {
		return chn;
	}

	protected Format getFormat() {
		return format;
	}
}
