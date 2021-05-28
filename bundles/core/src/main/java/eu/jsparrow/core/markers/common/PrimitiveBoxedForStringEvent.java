package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;

public interface PrimitiveBoxedForStringEvent {

	default void addMarkerEvent(ASTNode node, Expression refactorCandidateExpression, SimpleName name,
			SimpleName refactorPrimitiveType) {
	}

}
