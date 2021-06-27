package eu.jsparrow.core.visitor.junit.junit3;

import org.eclipse.jdt.core.dom.MethodDeclaration;

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
