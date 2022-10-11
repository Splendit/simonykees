package eu.jsparrow.rules.java16.switchexpression;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

public class SwitchExpressionAssignmentAnalyzer {

	public static boolean isSupportedFieldOrVariable(final Expression expression, Statement enclosingStatement,
			CompilationUnit compilationUnit) {

		if (expression.getNodeType() == ASTNode.SUPER_FIELD_ACCESS) {
			return true;
		}

		if (expression.getNodeType() == ASTNode.FIELD_ACCESS) {
			FieldAccess fieldAccess = (FieldAccess) expression;
			return fieldAccess.getExpression()
				.getNodeType() == ASTNode.THIS_EXPRESSION;
		}

		if (expression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}

		SimpleName simpleName = (SimpleName) expression;
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

		if (expression.getNodeType() == ASTNode.ARRAY_ACCESS) {
			ArrayAccess arrayAccess = (ArrayAccess) expression;
			Expression index = arrayAccess.getIndex();
			if (index.getNodeType() == ASTNode.NUMBER_LITERAL) {
				Expression array = arrayAccess.getArray();
				return getQualifierToAnalyze(array);
			}
		}
		return expression;
	}

	public static boolean isVariableWithoutSideEffect(Expression expression) {
		int expressionNodeType = expression.getNodeType();

		if (expressionNodeType == ASTNode.FIELD_ACCESS) {
			return isVariableWithoutSideEffect(((FieldAccess) expression).getExpression());
		}

		if (expressionNodeType == ASTNode.ARRAY_ACCESS) {
			return isArrayAccessWithoutSideEffect((ArrayAccess) expression);
		}

		return expressionNodeType == ASTNode.SIMPLE_NAME
				|| expressionNodeType == ASTNode.QUALIFIED_NAME
				|| expressionNodeType == ASTNode.THIS_EXPRESSION
				|| expressionNodeType == ASTNode.SUPER_FIELD_ACCESS;
	}

	private static boolean isArrayAccessWithoutSideEffect(ArrayAccess arrayAccess) {
		Expression index = arrayAccess.getIndex();
		Expression array = arrayAccess.getArray();
		return isVariableWithoutSideEffect(array)
				&& (index.getNodeType() == ASTNode.NUMBER_LITERAL || isVariableWithoutSideEffect(index));
	}

	private SwitchExpressionAssignmentAnalyzer() {
		// private default constructor of utility class in order to hide
		// implicit public one
	}
}
