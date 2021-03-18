package eu.jsparrow.core.visitor.junit.jupiter.assertthrows;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Helper class analyzing a {@link MethodInvocation}-node . If the
 * {@link MethodInvocation} represents the invocation of
 * {@code org.junit.Assert.assertThrows}, then all necessary informations for a
 * possible transformation are collected in an instance of
 * {@link JUnit4AssertThrowsInvocationAnalysisResult}.
 * 
 * @since 3.29.0
 *
 */
public class JUnit4AssertThrowsInvocationAnalyzer {

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