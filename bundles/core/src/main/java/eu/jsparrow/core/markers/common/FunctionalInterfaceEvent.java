package eu.jsparrow.core.markers.common;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public interface FunctionalInterfaceEvent {

	default void addMarkerEvent(ClassInstanceCreation classInstanceCreation, List<SingleVariableDeclaration> parameters,
			Block block) {
	}

}
