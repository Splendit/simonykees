package eu.jsparrow.core.visitor.junit.dedicated;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Stores all informations which are necessary to transform an invocation of
 * {@code assertTrue(condition)} or {@code assertFalse(condition)} to an
 * invocation of a more specific assertion.
 * 
 * @since 3.31.0
 *
 */
class DedicatedAssertionsAnalysisResult {
	private final String declaringClassQualifiedName;
	private final String newMethodName;
	private final List<Expression> newArguments;

	public DedicatedAssertionsAnalysisResult(String declaringClassQualifiedName, String newMethodName,
			List<Expression> newArguments) {
		this.declaringClassQualifiedName = declaringClassQualifiedName;
		this.newMethodName = newMethodName;
		this.newArguments = newArguments;
	}

	public String getDeclaringClassQualifiedName() {
		return declaringClassQualifiedName;
	}

	public String getNewMethodName() {
		return newMethodName;
	}

	public List<Expression> getNewArguments() {
		return newArguments;
	}
}
