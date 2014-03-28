package dnt.diag.ecu;

import dnt.diag.attribute.Attribute;
import dnt.diag.channel.Channel;
import dnt.diag.commbox.Commbox;
import dnt.diag.db.VehicleDB;
import dnt.diag.formats.Format;

public abstract class AbstractECU {
	private TroubleCodeFunction troubleCode;
	private DataStreamFunction dataStream;
	private DataStreamFunction freezeStream;
	private ActiveTestFunction activeTest;
	VehicleDB db;
	Commbox box;
	Attribute attr;
	Format format;
	Channel chn;

	protected void setTroubleCode(TroubleCodeFunction func) {
		troubleCode = func;
	}

	public TroubleCodeFunction getTroubleCode() {
		return troubleCode;
	}

	protected void setDataStream(DataStreamFunction func) {
		dataStream = func;
	}

	public DataStreamFunction getDataStream() {
		return dataStream;
	}

	protected void setFreezeStream(DataStreamFunction func) {
		freezeStream = func;
	}

	public DataStreamFunction getFreezeStream() {
		return freezeStream;
	}

	protected void setActiveTest(ActiveTestFunction func) {
		activeTest = func;
	}

	public ActiveTestFunction getActiveTest() {
		return activeTest;
	}

	public Commbox getCommbox() {
		return box;
	}

	public void setChannel(Channel chn) {
		this.chn = chn;
	}

	public Channel getChannel() {
		return chn;
	}

	public VehicleDB getDB() {
		return db;
	}

	public Format getFormat() {
		return format;
	}
	
	protected void setFormat(Format format) {
		this.format = format;
	}

	public Attribute getAttribute() {
		return attr;
	}

	public AbstractECU(VehicleDB db, Commbox box) {
		troubleCode = null;
		dataStream = null;
		freezeStream = null;
		activeTest = null;
		this.db = db;
		this.box = box;
		attr = new Attribute();
		format = null;
		chn = null;
	}

	public abstract void channelInit();
}
