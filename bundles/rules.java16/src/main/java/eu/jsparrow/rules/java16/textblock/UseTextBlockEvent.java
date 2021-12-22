package eu.jsparrow.rules.java16.textblock;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.TextBlock;

public interface UseTextBlockEvent {
	
	default void addMarkerEvent(InfixExpression node, TextBlock newNode) {}

}