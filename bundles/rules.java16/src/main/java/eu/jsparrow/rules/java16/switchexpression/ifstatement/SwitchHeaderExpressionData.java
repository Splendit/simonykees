package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Stores informations about a variable which may be used in connection with the
 * transformation of an if statement to a switch statement or to a switch
 * expression.
 * <p>
 * Examples for operations where such a variable can be found:
 * <ul>
 * <li>Variable {@code x} in {@code x == 1}</li>
 * <li>Variable {@code s} in {@code "A".equals(s)}</li>
 * </ul>
 * 
 * @since 4.13.0
 * 
 */
public class SwitchHeaderExpressionData {
	private static final List<String> TYPES_FOR_EQUALS_INFIX_EXPRESSION = Collections.unmodifiableList(Arrays.asList(
			char.class.getName(),
			int.class.getName(),
			long.class.getName()));

	private static final List<String> TYPES_FOR_EQUALS_METHOD_INVOCATION = Collections
		.singletonList(java.lang.String.class.getName());

	private final SimpleName switchHeaderExpression;
	private final ITypeBinding operandType;

	static Optional<SwitchHeaderExpressionData> findSwitchHeaderExpressionData(
			EqualsOperationForSwitch equalsOperation) {

		List<String> supportedOperandTypes;
		if (equalsOperation.getOperationNodeType() == ASTNode.INFIX_EXPRESSION) {
			supportedOperandTypes = TYPES_FOR_EQUALS_INFIX_EXPRESSION;
		} else {
			supportedOperandTypes = TYPES_FOR_EQUALS_METHOD_INVOCATION;
		}
		Predicate<ITypeBinding> typeBindingPredicate = typeBinding -> ClassRelationUtil.isContentOfTypes(typeBinding,
				supportedOperandTypes);
		SimpleName simpleName = equalsOperation.getSwitchHeaderExpression();

		return findSupportedVariableBinding(simpleName)
			.map(IVariableBinding::getType)
			.filter(typeBindingPredicate)
			.map(typeBinding -> new SwitchHeaderExpressionData(simpleName, typeBinding));
	}

	private static Optional<IVariableBinding> findSupportedVariableBinding(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			return Optional.empty();
		}
		if (binding.getKind() != IBinding.VARIABLE) {
			return Optional.empty();
		}
		return Optional.of((IVariableBinding) binding)
			.filter(variableBinding -> !variableBinding.isField());
	}

	private SwitchHeaderExpressionData(SimpleName switchHeaderExpression,
			ITypeBinding operandType) {
		this.switchHeaderExpression = switchHeaderExpression;
		this.operandType = operandType;
	}

	SimpleName getSwitchHeaderExpression() {
		return switchHeaderExpression;
	}

	ITypeBinding getSwitchHeaderExpressionType() {
		return operandType;
	}
}
