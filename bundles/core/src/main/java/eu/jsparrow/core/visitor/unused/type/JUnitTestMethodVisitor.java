package eu.jsparrow.core.visitor.unused.type;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.visitor.junit.junit3.JUnit3DataCollectorVisitor;
import eu.jsparrow.core.visitor.unused.method.MethodReferencesVisitor;

/**
 * Visitor to verify whether a compilation unit is used for running JUnit3 or
 * JUnit4 or JUnit Jupiter tests.
 * 
 * @since 4.10.0
 */
public class JUnitTestMethodVisitor extends ASTVisitor {
	private boolean jUnitTestMethodFound;
	private TypeDeclaration jUnit3TestCase;

	@Override
	public boolean preVisit2(ASTNode node) {
		return !jUnitTestMethodFound;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		SimpleType testCaseAsSuperType = JUnit3DataCollectorVisitor.findTestCaseAsSuperType(node)
			.orElse(null);
		if (testCaseAsSuperType != null && JUnit3DataCollectorVisitor.isValidJUnit3TestCaseSubclass(node)) {
			jUnit3TestCase = node;
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (isJUnit3TestMethod(node) || MethodReferencesVisitor.isTestAnnotatedMethod(node)) {
			jUnitTestMethodFound = true;
			return false;

		}
		return true;
	}

	private boolean isJUnit3TestMethod(MethodDeclaration methodDeclaration) {
		if (jUnit3TestCase == null || methodDeclaration.getParent() != jUnit3TestCase) {
			return false;
		}
		String methodIdentifier = methodDeclaration.getName()
			.getIdentifier();
		return methodIdentifier.startsWith(JUnit3DataCollectorVisitor.TEST) &&
				JUnit3DataCollectorVisitor.isTestMethodDeclaration(methodDeclaration);
	}

	public boolean isJUnitTestCaseFound() {
		return jUnitTestMethodFound;
	}
}
