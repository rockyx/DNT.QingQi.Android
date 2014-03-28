package dnt.diag.ecu;

import android.util.SparseArray;

import dnt.diag.channel.Channel;
import dnt.diag.channel.ChannelException;
import dnt.diag.db.VehicleDB;
import dnt.diag.formats.Format;

public abstract class ActiveTestFunction extends AbstractFunction {

	public interface ActionActive {
		void execute(ActiveState state) throws ChannelException;
	}

	private SparseArray<ActionActive> actMap;
	private ActiveState state;

	public ActiveTestFunction(VehicleDB db, Channel chn, Format format) {
		super(db, chn, format);
		actMap = new SparseArray<ActionActive>();
		state = ActiveState.Stop;
	}

	public abstract void execute(int mode);

	public synchronized void changeState(ActiveState state) {
		this.state = state;
	}

	protected SparseArray<ActionActive> getMap() {
		return actMap;
	}

	protected synchronized ActiveState getState() {
		return state;
	}
}
