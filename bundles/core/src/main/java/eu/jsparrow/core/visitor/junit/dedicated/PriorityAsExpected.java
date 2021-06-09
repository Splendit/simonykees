package eu.jsparrow.core.visitor.junit.dedicated;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;

public class PriorityAsExpected {

	private static final int PRIORITY_LITERAL = 5;
	private static final int PRIORITY_CONSTANT = 4;
	private static final int PRIORITY_FINAL_FIELD = 3;
	private static final int PRIORITY_FINAL_LOCAL = 2;
	private static final int NO_PRIORITY = 0;

	public int getPriorityAsExpectedValue(Expression expression) {
		int nodeType = expression.getNodeType();
		if (nodeType == ASTNode.BOOLEAN_LITERAL ||
				nodeType == ASTNode.CHARACTER_LITERAL ||
				nodeType == ASTNode.NUMBER_LITERAL ||
				nodeType == ASTNode.STRING_LITERAL) {
			return PRIORITY_LITERAL;
		}

		if (nodeType == ASTNode.FIELD_ACCESS) {
			FieldAccess fieldAccess = (FieldAccess) expression;
			return getPriorityAsExpectedValueFromBinding(fieldAccess.resolveFieldBinding());
		}

		if (nodeType == ASTNode.SUPER_FIELD_ACCESS) {
			SuperFieldAccess superfieldAccess = (SuperFieldAccess) expression;
			return getPriorityAsExpectedValueFromBinding(superfieldAccess.resolveFieldBinding());
		}

		if (nodeType == ASTNode.QUALIFIED_NAME) {
			QualifiedName qualifiedName = (QualifiedName) expression;
			IBinding binding = qualifiedName.resolveBinding();
			if (binding != null && binding.getKind() == IBinding.VARIABLE) {
				return getPriorityAsExpectedValueFromBinding((IVariableBinding) binding);
			}
		}

		if (nodeType == ASTNode.SIMPLE_NAME) {
			SimpleName simpleName = (SimpleName) expression;
			IBinding binding = simpleName.resolveBinding();
			if (binding != null && binding.getKind() == IBinding.VARIABLE) {
				return getPriorityAsExpectedValueFromBinding((IVariableBinding) binding);
			}
		}

		return NO_PRIORITY;
	}

	public int getPriorityAsExpectedValueFromBinding(IVariableBinding variableBinding) {
		int modifiers = variableBinding.getModifiers();
		if (Modifier.isFinal(modifiers)) {
			if (variableBinding.isField()) {
				if (Modifier.isStatic(modifiers)) {
					return PRIORITY_CONSTANT;
				}
				return PRIORITY_FINAL_FIELD;
			}
			return PRIORITY_FINAL_LOCAL;
		}
		return NO_PRIORITY;
	}
}
