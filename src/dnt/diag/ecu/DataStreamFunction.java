package dnt.diag.ecu;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dnt.diag.Timer;
import dnt.diag.channel.Channel;
import dnt.diag.channel.ChannelException;
import dnt.diag.data.LiveDataBuffer;
import dnt.diag.data.LiveDataItem;
import dnt.diag.data.LiveDataList;
import dnt.diag.db.DatabaseException;
import dnt.diag.db.VehicleDB;
import dnt.diag.formats.Format;

public abstract class DataStreamFunction extends AbstractFunction {

	public interface ActionRead {
		void execute() throws ChannelException;
	}

	public interface ActionCalc {
		void execute();
	}

	private class ReadThread implements Runnable {
		private LiveDataList lds;
		private byte[] buff;

		public ReadThread(LiveDataList lds) {
			this.lds = lds;
			buff = new byte[128];
		}

		@Override
		public void run() {
			stopRead = false;
			readExp = false;

			Map<String, byte[]> cmdMap = lds.getCommandNeed();

			if (beginRead != null)
				try {
					beginRead.execute();
				} catch (ChannelException e) {
					stopRead = true;
					readExp = true;
				}

			while (true) {
				for (Map.Entry<String, byte[]> entry : cmdMap.entrySet()) {
					
					synchronized (readMutex) {
						if (stopRead) {
							if (endRead != null) {
								try {
									endRead.execute();
								} catch (ChannelException e) {
								}
							}
							return;
						}
					}

					LiveDataBuffer ldBuff = lds.getMsgBuffer(entry.getKey());
					
					byte[] cmd = entry.getValue();
					
					int length = 0;
					
					try {
						length = getChannel().sendAndRecv(cmd, 0, cmd.length,
								buff);
					} catch (ChannelException e) {
					}
					
					ldBuff.copyTo(buff, 0, length);
					
					try {
						Thread.sleep(readInterval.toMilliseconds());
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	private class CalcThread implements Runnable {
		private List<LiveDataItem> items = null;

		public CalcThread(LiveDataList items) {
			this.items = items.getDisplayItems();
		}

		@Override
		public void run() {

			if (beginCalc != null)
				beginCalc.execute();

			while (true) {
				for (LiveDataItem item : items) {
					synchronized (calcMutex) {
						if (stopCalc || readExp) {
							if (endCalc != null)
								endCalc.execute();
							return;
						}
					}

					item.calcValue();

					try {
						Thread.sleep(calcInterval.toMilliseconds());
					} catch (InterruptedException e) {
						// e.printStackTrace();
					}
				}
			}
		}
	}

	protected static void checkOutOfRange(double value, LiveDataItem ld) {
		try {
			double min = Double.valueOf(ld.getMinValue()).doubleValue();
			double max = Double.valueOf(ld.getMaxValue()).doubleValue();

			if (value < min || value > max)
				ld.setOutOfRange(true);
			else
				ld.setOutOfRange(false);
		} catch (NumberFormatException e) {
			// e.printStackTrace();
		}
	}

	private ActionRead beginRead;
	private ActionRead endRead;
	private ActionCalc beginCalc;
	private ActionCalc endCalc;
	private LiveDataList lds;
	private boolean stopRead;
	private boolean stopCalc;
	private Timer readInterval;
	private Timer calcInterval;
	private Object readMutex;
	private Object calcMutex;
	private boolean readExp;
	private String errMsg;
	private ExecutorService executor;

	public DataStreamFunction(VehicleDB db, Channel chn, Format format) {
		super(db, chn, format);
		beginRead = null;
		endRead = null;
		beginCalc = null;
		endCalc = null;
		lds = null;
		stopRead = true;
		stopCalc = true;
		readInterval = Timer.fromMilliseconds(10);
		calcInterval = Timer.fromMilliseconds(1);
		readMutex = new Object();
		calcMutex = new Object();
		readExp = false;
	}

	protected abstract void initCalcFunctions();

	public void setReadInterval(Timer time) {
		readInterval = time;
	}

	public void setCalcInterval(Timer time) {
		calcInterval = time;
	}

	public LiveDataList getLiveDataItems() {
		return lds;
	}

	public boolean queryLiveData(String cls) {
		try {
			lds = getDB().queryLiveData(cls);
			initCalcFunctions();
			return true;
		} catch (DatabaseException e) {
			return false;
		}
	}

	public void setBeginRead(ActionRead act) {
		beginRead = act;
	}

	public void setEndRead(ActionRead act) {
		endRead = act;
	}

	public void setBeginCalc(ActionCalc act) {
		beginCalc = act;
	}

	public void setEndCalc(ActionCalc act) {
		endCalc = act;
	}

	public String getErrMsg() {
		return errMsg;
	}

	private void stopRead() {
		synchronized (readMutex) {
			stopRead = true;
		}
	}

	private void stopCalc() {
		synchronized (calcMutex) {
			stopCalc = true;
		}
	}

	public void stop() throws ChannelException {
		stopCalc();
		stopRead();

		try {
			if (executor != null) {
				executor.shutdown();
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			}
		} catch (InterruptedException e) {
		}
	}

	public void start() throws ChannelException {

		stopRead = false;
		stopCalc = false;
		readExp = false;

		executor = Executors.newCachedThreadPool();
		executor.execute(new ReadThread(lds));
		executor.execute(new CalcThread(lds));

	}

	public void startOnce() {
		executor = Executors.newCachedThreadPool();
		executor.execute(new Runnable() {
			private byte[] buff = new byte[128];

			@Override
			public void run() {
				try {
					if (beginRead != null)
						beginRead.execute();

					Map<String, byte[]> cmdMap = lds.getCommandNeed();
					for (Map.Entry<String, byte[]> entry : cmdMap.entrySet()) {
						LiveDataBuffer buffer = lds.getMsgBuffer(entry.getKey());
						byte[] cmd = entry.getValue();
						int length = getChannel().sendAndRecv(cmd, 0,
								cmd.length, buff);
						buffer.copyTo(buff, 0, length);
					}

					if (endRead != null)
						endRead.execute();

					if (beginCalc != null)
						beginCalc.execute();

					List<LiveDataItem> items = lds.getDisplayItems();
					for (LiveDataItem item : items) {
						item.calcValue();
					}

					if (endCalc != null)
						endCalc.execute();
				} catch (ChannelException e) {
					// e.printStackTrace();
				}
			}
		});

		try {
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
	}
}
