package eu.jsparrow.ui.preview.statistics;

import org.eclipse.swt.widgets.Composite;

public interface StatisticsSection {
	
	default void initializeDataBindings() {}
	
	default void createView(Composite rootComposite) {}
	
	default void updateForSelected() {}

	
}
