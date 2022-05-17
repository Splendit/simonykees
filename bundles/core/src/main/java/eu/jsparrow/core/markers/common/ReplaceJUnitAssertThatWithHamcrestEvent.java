package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.junit.ReplaceJUnitAssertThatWithHamcrestASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ReplaceJUnitAssertThatWithHamcrestASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public interface ReplaceJUnitAssertThatWithHamcrestEvent {

	/**
	 * 
	 * @param importDeclaration
	 *            the import declaration of JUnit assertThat.
	 */
	default void addMarkerEvent(ImportDeclaration importDeclaration) {
	}

	/**
	 * 
	 * @param methodInvocation
	 *            the invocation of JUnit assertThat
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}

}
