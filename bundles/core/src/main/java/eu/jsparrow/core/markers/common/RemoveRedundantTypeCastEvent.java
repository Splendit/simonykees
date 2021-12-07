package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ASTNode;

public interface RemoveRedundantTypeCastEvent {

	default void addMarkerEvent(ASTNode nodeToBeReplaced, ASTNode nodeToBeReplaced2) {
	}
}
