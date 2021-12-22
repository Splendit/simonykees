package eu.jsparrow.rules.java16.switchexpression;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;

public interface UseSwitchExpressionEvent {

	default void addMarkerEvent(SwitchStatement switchStatement, Statement newStatement) {
	}

}
