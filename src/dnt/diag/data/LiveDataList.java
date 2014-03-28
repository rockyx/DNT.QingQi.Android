package dnt.diag.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class LiveDataList implements Iterable<LiveDataItem> {

	private ArrayList<LiveDataItem> items;
	private Map<String, LiveDataItem> queryByShortName;
	private List<LiveDataItem> needs;
	private Map<String, LiveDataBuffer> bufferMap;
	private Map<String, byte[]> commandNeed;
	private LiveDataComparator comparator;

	public LiveDataList() {
		items = new ArrayList<LiveDataItem>();
		queryByShortName = new HashMap<String, LiveDataItem>();
		needs = new ArrayList<LiveDataItem>();
		bufferMap = new HashMap<String, LiveDataBuffer>();
		commandNeed = new HashMap<String, byte[]>();
		comparator = new LiveDataComparator();
	}

	@Override
	public Iterator<LiveDataItem> iterator() {
		return items.iterator();
	}

	public void add(LiveDataItem item) {
		items.add(item);
		queryByShortName.put(item.getShortName(), item);
		String key = item.getCmdClass() + item.getCmdName();
		if (!bufferMap.containsKey(key))
			bufferMap.put(key, new LiveDataBuffer());
		item.setEcuResponseBuff(bufferMap.get(key));
	}

	public LiveDataItem get(String shortName) {
		if (queryByShortName.containsKey(shortName))
			return queryByShortName.get(shortName);
		else
			return null;
	}

	public int size() {
		return items.size();
	}

	public void makeDisplayItems() {
		needs.clear();
		for (LiveDataItem item : items) {
			if (item.isEnabled() && item.isDisplay()) {
				needs.add(item);
				String key = item.getCmdClass() + item.getCmdName();
				if (!commandNeed.containsKey(key))
					commandNeed.put(key, item.getFormattedCommand());
			}
		}

		Collections.sort(needs, comparator);
	}

	public List<LiveDataItem> getDisplayItems() {
		return needs;
	}

	public Map<String, byte[]> getCommandNeed() {
		return commandNeed;
	}

	public LiveDataBuffer getMsgBuffer(String key) {
		return bufferMap.get(key);
	}
}
