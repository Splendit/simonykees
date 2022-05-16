package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public interface ReplaceJUnitAssertThatWithHamcrestEvent {

	default void addMarkerEvent(ImportDeclaration importDeclaration) {
		
	}
	
	default void addMarkerEvent(MethodInvocation methodInvocation) {
		
	}


}
