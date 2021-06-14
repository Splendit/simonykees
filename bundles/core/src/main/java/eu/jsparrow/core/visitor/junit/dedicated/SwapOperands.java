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

/**
 * For two values in an assertion, this class finds out which of the two values
 * represents the expected value. If the right hand side represents the expected
 * value, then a swap of the two values is necessary.
 * 
 * @since 3.31.0
 *
 */
public class SwapOperands {

	private static final int PRIORITY_LITERAL = 50;
	private static final int PRIORITY_CONSTANT = 40;
	private static final int PRIORITY_FINAL_FIELD = 30;
	private static final int PRIORITY_FINAL_LOCAL = 20;
	private static final int PRIORITY_NAME_CONTAINS_EXPECTED = 10;
	private static final int NO_PRIORITY = 0;

	private SwapOperands() {
		// hiding implicit public one
	}

	public static boolean swapOperands(Expression leftOperand, Expression rightOperand) {
		return getPriorityAsExpectedValue(rightOperand) > getPriorityAsExpectedValue(leftOperand);
	}

	private static int getPriorityAsExpectedValue(Expression expression) {
		int nodeType = expression.getNodeType();
		if (nodeType == ASTNode.BOOLEAN_LITERAL ||
				nodeType == ASTNode.CHARACTER_LITERAL ||
				nodeType == ASTNode.NUMBER_LITERAL ||
				nodeType == ASTNode.STRING_LITERAL) {
			return PRIORITY_LITERAL;
		}

		IBinding binding = null;
		if (nodeType == ASTNode.FIELD_ACCESS) {
			binding = ((FieldAccess) expression).resolveFieldBinding();
		}

		if (nodeType == ASTNode.SUPER_FIELD_ACCESS) {
			binding = ((SuperFieldAccess) expression).resolveFieldBinding();
		}

		if (nodeType == ASTNode.QUALIFIED_NAME) {
			binding = ((QualifiedName) expression).resolveBinding();
		}

		if (nodeType == ASTNode.SIMPLE_NAME) {
			binding = ((SimpleName) expression).resolveBinding();
		}

		if (binding != null) {
			return getPriorityAsExpectedValueFromBinding(binding);
		}

		return NO_PRIORITY;
	}

	private static int getPriorityAsExpectedValueFromBinding(IBinding binding) {
		if (binding.getKind() != IBinding.VARIABLE) {
			return NO_PRIORITY;
		}

		IVariableBinding variableBinding = (IVariableBinding) binding;

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

		String variableName = variableBinding.getName()
			.toLowerCase();
		if (variableName.contains("expected")) { //$NON-NLS-1$
			return PRIORITY_NAME_CONTAINS_EXPECTED;
		}

		return NO_PRIORITY;
	}
}