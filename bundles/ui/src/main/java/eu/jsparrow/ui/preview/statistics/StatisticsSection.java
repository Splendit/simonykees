package eu.jsparrow.ui.preview.statistics;

import org.eclipse.swt.widgets.Composite;

/**
 * A type for the overall statistics section shown in the select rules wizard.
 *
 * @since 4.6.0
 */
public interface StatisticsSection {

	default void initializeDataBindings() {
	}

	default void createView(Composite rootComposite) {
	}

	default void updateForSelected() {
	}

}
