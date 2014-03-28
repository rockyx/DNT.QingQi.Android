package dnt.diag;

public final class Timer {
	private long nanoSecs;

	static final double NANO_PER_MICRO;
	static final double NANO_PER_MILLI;
	static final double NANO_PER_SECONDS;

	static {
		NANO_PER_MICRO = 1000;
		NANO_PER_MILLI = 1000000;
		NANO_PER_SECONDS = 1000000000;
	}

	public Timer() {
		nanoSecs = 0;
	}

	public Timer(long nanoSecs) {
		this.nanoSecs = nanoSecs;
	}

	public long toNanoseconds() {
		return nanoSecs;
	}

	public void setNanoseconds(long nanoSecs) {
		this.nanoSecs = nanoSecs;
	}

	public long toMicroseconds() {
		return (long) (nanoSecs / NANO_PER_MICRO);
	}

	public void setMicroseconds(long micro) {
		nanoSecs = (long) (micro * NANO_PER_MICRO);
	}

	public long toMilliseconds() {
		return (long) (nanoSecs / NANO_PER_MILLI);
	}

	public void setMilliseconds(long milli) {
		nanoSecs = (long) (milli * NANO_PER_MILLI);
	}

	public long toSeconds() {
		return (long) (nanoSecs / NANO_PER_SECONDS);
	}

	public void setSeconds(long sec) {
		nanoSecs = (long) (sec * NANO_PER_SECONDS);
	}

	public boolean isNoTimeLimit() {
		return nanoSecs <= 0;
	}

	public static Timer fromMicroseconds(long time) {
		Timer t = new Timer();
		t.setMicroseconds(time);
		return t;
	}

	public static Timer fromMilliseconds(long time) {
		Timer t = new Timer();
		t.setMilliseconds(time);
		return t;
	}

	public static Timer fromSeconds(long time) {
		Timer t = new Timer();
		t.setSeconds(time);
		return t;
	}
}
