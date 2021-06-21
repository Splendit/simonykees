package eu.jsparrow.core.visitor.junit.junit3;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class JUnit3AssertionAnalysisResult {

	private final MethodInvocation methodInvocation;
	private final List<Expression> assertionArguments;
	private final String classDeclaringMethodReplacement;

	JUnit3AssertionAnalysisResult(MethodInvocation methodInvocation, List<Expression> assertionArguments,
			String classDeclaringMethodReplacement) {
		this.methodInvocation = methodInvocation;
		this.assertionArguments = assertionArguments;
		this.classDeclaringMethodReplacement = classDeclaringMethodReplacement;
	}

	public MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	public List<Expression> getAssertionArguments() {
		return assertionArguments;
	}

	public String getClassDeclaringMethodReplacement() {
		return classDeclaringMethodReplacement;
	}
}