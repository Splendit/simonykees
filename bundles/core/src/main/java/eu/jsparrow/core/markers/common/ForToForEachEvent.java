package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public interface ForToForEachEvent<T> {
	
	default void addMarkerEvent(T loop, SimpleName iterableNode,
			SingleVariableDeclaration iteratorDecl) {}

}
