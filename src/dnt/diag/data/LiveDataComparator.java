package dnt.diag.data;

import java.util.Comparator;

public class LiveDataComparator implements Comparator<LiveDataItem> {

	@Override
	public int compare(LiveDataItem lhs, LiveDataItem rhs) {
		Integer left = lhs.getIndexForSort();
		Integer right = rhs.getIndexForSort();
		return left.compareTo(right);
	}
	

}
