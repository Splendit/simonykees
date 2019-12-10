package eu.jsparrow.core.visitor.make_final;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * This helper visitor collects all fields that have been declared and assigned
 * somewhere in the {@link CompilationUnit} or {@link TypeDeclaration} which
 * accepts this visitor.
 *
 * @since 3.12.0
 */
public class PrivateFieldAssignmentASTVisitor extends AbstractMakeFinalHelperVisitor {

	private final List<VariableDeclarationFragment> assignedFragments = new ArrayList<>();

	/*
	 * Initialisers are already checked in another precondition.
	 */
	@Override
	public boolean visit(Initializer initializer) {
		return false;
	}

	/*
	 * Constructors are already checked in another precondition.
	 */
	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		return !methodDeclaration.isConstructor();
	}

	@Override
	public boolean visit(Assignment assignment) {
		Expression leftHandSide = assignment.getLeftHandSide();

		addAssignedFragment(leftHandSide);

		return true;
	}

	@Override
	public boolean visit(PostfixExpression postfixExpression) {
		Expression operand = postfixExpression.getOperand();

		addAssignedFragment(operand);

		return true;
	}

	@Override
	public boolean visit(PrefixExpression prefixExpression) {
		PrefixExpression.Operator operator = prefixExpression.getOperator();
		if (!PrefixExpression.Operator.INCREMENT.equals(operator)
				&& !PrefixExpression.Operator.DECREMENT.equals(operator)) {
			return true;
		}

		Expression operand = prefixExpression.getOperand();

		addAssignedFragment(operand);

		return true;
	}

	private void addAssignedFragment(Expression expression) {
		VariableDeclarationFragment variableDeclarationFragment = extractFieldDeclarationFragmentFromExpression(
				expression);

		if (variableDeclarationFragment != null && !assignedFragments.contains(variableDeclarationFragment)) {
			assignedFragments.add(variableDeclarationFragment);
		}
	}

	/**
	 * @return the collected {@link VariableDeclarationFragment}s, which are
	 *         assigned somewhere in the corresponding class
	 */
	public List<VariableDeclarationFragment> getAssigendVariableDeclarationFragments() {
		return assignedFragments;
	}
}
