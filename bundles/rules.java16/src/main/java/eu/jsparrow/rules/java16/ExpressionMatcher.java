package eu.jsparrow.rules.java16;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public class ExpressionMatcher {

	private final ASTMatcher astMatcher = new ASTMatcher();

	boolean match(Expression first, Expression second) {

		if (first.getNodeType() == ASTNode.SIMPLE_NAME) {
			return astMatcher.match((SimpleName) first, second);
		}

		if (first.getNodeType() == ASTNode.QUALIFIED_NAME) {
			return astMatcher.match((QualifiedName) first, second);
		}

		if (first.getNodeType() == ASTNode.FIELD_ACCESS) {
			return astMatcher.match((FieldAccess) first, second);
		}

		if (first.getNodeType() == ASTNode.SUPER_FIELD_ACCESS) {
			return astMatcher.match((SuperFieldAccess) first, second);
		}

		if (first.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return astMatcher.match((MethodInvocation) first, second);
		}

		if (first.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION) {
			return astMatcher.match((SuperMethodInvocation) first, second);
		}

		return false;
	}

}
