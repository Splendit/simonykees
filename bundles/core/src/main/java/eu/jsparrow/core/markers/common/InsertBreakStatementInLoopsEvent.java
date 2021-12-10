package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;

public interface InsertBreakStatementInLoopsEvent {

	default void addMarkerEvent(EnhancedForStatement forStatement, IfStatement ifStatement, Block ifBodyBlock) {		
	}

	default void addMarkerEvent(EnhancedForStatement forStatement, IfStatement ifStatement, ExpressionStatement expressionStatement) {
	}
}
