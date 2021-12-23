package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public interface ForToForEachEvent<T> {
	
	default void addMarkerEvent(T loop, Statement loopBody, SimpleName iterableNode,
			SingleVariableDeclaration iteratorDecl) {}

}
