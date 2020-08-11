package eu.jsparrow.ui.preview.comparator;

import java.util.Comparator;

import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * This is a comparator for sorting the entries of the rules table viewer on the
 * summary page. This has been taken from: <a href=
 * "https://www.vogella.com/tutorials/EclipseJFaceTable/article.html#sort-content-of-table-columns">https://www.vogella.com/tutorials/EclipseJFaceTable/article.html#sort-content-of-table-columns</a>
 * <p/>
 * Note: This super class has been extracted from the original class
 * SummaryPageRuleTableViewerComparator for 3.20.0.
 * 
 * @since 3.20.0
 */
public class SortableViewerComparator extends ViewerComparator {

	protected int propertyIndex;
	protected static final int DESCENDING = 1;
	protected int direction = DESCENDING;

	public SortableViewerComparator() {
		this.propertyIndex = 0;
		direction = DESCENDING;
	}

	public SortableViewerComparator(Comparator<? super String> comparator) {
		super(comparator);
	}

	public int getDirection() {
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}

}