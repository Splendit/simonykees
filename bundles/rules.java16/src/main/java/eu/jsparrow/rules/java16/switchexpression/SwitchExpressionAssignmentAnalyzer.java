package eu.jsparrow.rules.java16.switchexpression;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

public class SwitchExpressionAssignmentAnalyzer {

	public static boolean isSupportedAssignmentLeftHandSide(final Expression expression, Statement enclosingStatement,
			CompilationUnit compilationUnit) {

		Expression qualifierToAnalyze = getQualifierToAnalyze(expression);
		if (qualifierToAnalyze.getNodeType() == ASTNode.THIS_EXPRESSION
				|| qualifierToAnalyze.getNodeType() == ASTNode.SUPER_FIELD_ACCESS) {
			return true;
		}

		if (qualifierToAnalyze.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}

		SimpleName simpleName = (SimpleName) qualifierToAnalyze;
		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			return false;
		}
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);
		if (declaringNode == null) {
			return true;
		}
		ASTNode parent = declaringNode.getParent();
		while (parent != null) {
			if (parent == enclosingStatement) {
				return false;
			}
			parent = parent.getParent();
		}
		return true;
	}

	private static Expression getQualifierToAnalyze(Expression expression) {
		if (expression.getNodeType() == ASTNode.QUALIFIED_NAME) {
			QualifiedName qualifiedName = (QualifiedName) expression;
			return getQualifierToAnalyze(qualifiedName.getQualifier());
		}

		if (expression.getNodeType() == ASTNode.FIELD_ACCESS) {
			FieldAccess fieldAccess = (FieldAccess) expression;
			return getQualifierToAnalyze(fieldAccess.getExpression());
		}
		return expression;
	}
}
