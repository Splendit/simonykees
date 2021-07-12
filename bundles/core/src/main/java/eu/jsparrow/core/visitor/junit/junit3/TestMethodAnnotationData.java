package eu.jsparrow.core.visitor.junit.junit3;

import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Stores the qualified type name for the annotation to be added to a method
 * declaration which has been found in a JUnit 3 test.
 * <p>
 * For the migration to JUnit4:
 * <ul>
 * <li>"org.junit.Test" for test methods</li>
 * <li>"org.junit.Before" for setUp methods</li>
 * <li>"org.junit.After" for tearDown methods</li>
 * </ul>
 * <ul>
 * <p>
 * For the migration to JUnit4:
 * <li>"org.junit.jupiter.api.Test" for test methods</li>
 * <li>"org.junit.jupiter.api.BeforeEach" for setUp methods</li>
 * <li>"org.junit.jupiter.api.AfterEach" for tearDown methods</li>
 * </ul>
 *
 */
public class TestMethodAnnotationData {

	private final MethodDeclaration methodDeclaration;
	private final String annotationQualifiedName;

	public TestMethodAnnotationData(MethodDeclaration methodDeclaration, String annotationQualifiedName) {
		this.methodDeclaration = methodDeclaration;
		this.annotationQualifiedName = annotationQualifiedName;

	}

	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	public String getAnnotationQualifiedName() {
		return annotationQualifiedName;
	}
}
