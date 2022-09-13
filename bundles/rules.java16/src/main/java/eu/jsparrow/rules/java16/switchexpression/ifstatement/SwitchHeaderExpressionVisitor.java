package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 *
 */
class SwitchHeaderExpressionVisitor extends AbstractIfExpressionVisitor {

	private SimpleName expectedSwitchHeaderExpression;
	private ITypeBinding expectedOperandType;
	private boolean stopVisit;

	private static Optional<ITypeBinding> findVariableTypeBinding(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			return Optional.empty();
		}
		if (binding.getKind() != IBinding.VARIABLE) {
			return Optional.empty();
		}
		return Optional.of((IVariableBinding) binding)
			.filter(variableBinding -> !variableBinding.isField())
			.map(IVariableBinding::getType);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		if (stopVisit) {
			return false;
		}
		return super.preVisit2(node);
	}

	@Override
	protected boolean analyzeEqualsOperationForSwitch(EqualsOperationForSwitch equalsOperation) {
		stopVisit = true;
		expectedSwitchHeaderExpression = equalsOperation.getSwitchHeaderExpression();
		expectedOperandType = findVariableTypeBinding(expectedSwitchHeaderExpression).orElse(null);
		return expectedOperandType != null;
	}

	public Optional<SimpleName> getSwitchHeaderExpression() {
		return Optional.ofNullable(expectedSwitchHeaderExpression);
	}

	public Optional<ITypeBinding> getSwitchHeaderExpressionType() {
		return Optional.ofNullable(expectedOperandType);
	}

}
