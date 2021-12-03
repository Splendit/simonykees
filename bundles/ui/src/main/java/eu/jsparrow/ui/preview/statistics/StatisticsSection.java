package eu.jsparrow.ui.preview.statistics;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * A type for the overall statistics section shown in the select rules wizard.
 *
 * @since 4.6.0
 */
public interface StatisticsSection {

	default void initializeDataBindings() {
	}

	default List<Image> createView(Composite rootComposite) {
		return Collections.emptyList();
	}

	default void updateForSelected() {
	}

}
