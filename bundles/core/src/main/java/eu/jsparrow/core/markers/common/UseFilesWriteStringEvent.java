package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.TryStatement;

public interface UseFilesWriteStringEvent {

	default void addMarkerEvent(TryStatement tryStatement) {}
	
	default void addMarkerEvent(ExpressionStatement expressionStatement) {}
}
