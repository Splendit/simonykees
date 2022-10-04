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

	boolean analyzeAssignmentLeftHandSideRootQualifier(Expression expression, Statement enclosingStatement,
			CompilationUnit compilationUnit) {
		if (expression.getNodeType() == ASTNode.THIS_EXPRESSION
				|| expression.getNodeType() == ASTNode.SUPER_FIELD_ACCESS) {
			return true;
		}
		if (expression.getNodeType() == ASTNode.SIMPLE_NAME) {
			return !isLocalVariableDeclaredWithinEnclosingStatement((SimpleName) expression, enclosingStatement,
					compilationUnit);
		}
		if (expression.getNodeType() == ASTNode.QUALIFIED_NAME) {
			QualifiedName qualifiedName = (QualifiedName) expression;
			return !analyzeAssignmentLeftHandSideRootQualifier(qualifiedName.getQualifier(), enclosingStatement,
					compilationUnit);
		}
		if (expression.getNodeType() == ASTNode.FIELD_ACCESS) {
			FieldAccess fieldAccess = (FieldAccess) expression;
			return !analyzeAssignmentLeftHandSideRootQualifier(fieldAccess.getExpression(), enclosingStatement,
					compilationUnit);
		}
		return false;
	}

	private boolean isLocalVariableDeclaredWithinEnclosingStatement(SimpleName rootQualifier,
			Statement enclosingStatement,
			CompilationUnit compilationUnit) {
		IBinding binding = rootQualifier.resolveBinding();
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

}
