package eu.jsparrow.ui.preview.statistics;

import java.time.Duration;

import org.eclipse.swt.widgets.Composite;

public interface StatisticsSection {
	
	default void initializeDataBindings() {}
	
	default void createView(Composite rootComposite) {}
	
	default void updateForSelected() {}
	
	default void updateForSelected(int deltaTotalIssues, Duration deltaTimeSaved, int deltaRequiredCredit) {}

	
}
