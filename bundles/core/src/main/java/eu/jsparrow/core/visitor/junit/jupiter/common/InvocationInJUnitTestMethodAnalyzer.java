package eu.jsparrow.core.visitor.junit.jupiter.common;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class InvocationInJUnitTestMethodAnalyzer {

	boolean isSurroundedWithLambda(MethodDeclaration methodDeclaration, MethodInvocation methodInvocation) {

		ASTNode parent = methodInvocation.getParent();
		while (parent != null) {
			if (parent == methodDeclaration) {
				return false;
			}
			if (parent.getNodeType() == ASTNode.LAMBDA_EXPRESSION) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}
}