package eu.jsparrow.core.visitor.make_final;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * This helper visitor collects all fields that have been declared and assigned
 * somewhere in the {@link CompilationUnit} or {@link TypeDeclaration} which
 * accepts this visitor.
 *
 * @since 3.12.0
 */
public class PrivateFieldAssignmentASTVisitor extends AbstractMakeFinalHelperVisitor {

	private final List<VariableDeclarationFragment> assignedFragments = new ArrayList<>();
	private Map<ASTNode, List<VariableDeclarationFragment>> currentlySkipped = new HashMap<>();
	private TypeDeclaration typeDeclaration;

	public PrivateFieldAssignmentASTVisitor(TypeDeclaration typeDeclaration) {
		this.typeDeclaration = typeDeclaration;
	}

	/*
	 * Initializers are already checked in another precondition.
	 * 
	 */
	@Override
	public boolean visit(Initializer initializer) {
		if (initializer.getParent() == this.typeDeclaration) {
			storeCurrentlySkipped(initializer);
		}
		return true;
	}

	@Override
	public void endVisit(Initializer initializer) {
		currentlySkipped.remove(initializer);
	}

	/*
	 * Constructors are already checked in another precondition.
	 */
	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		if (methodDeclaration.getParent() == this.typeDeclaration && methodDeclaration.isConstructor()) {
			storeCurrentlySkipped(methodDeclaration);
		}
		return true;
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		currentlySkipped.remove(methodDeclaration);
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

		if (variableDeclarationFragment != null && !assignedFragments.contains(variableDeclarationFragment)
				&& !isCurrentlySkipped(variableDeclarationFragment)) {
			assignedFragments.add(variableDeclarationFragment);
		}
	}

	private boolean isCurrentlySkipped(VariableDeclarationFragment variableDeclarationFragment) {
		return currentlySkipped.values().stream().flatMap(List::stream)
				.anyMatch(skipped -> skipped == variableDeclarationFragment);
	}

	/**
	 * @return the collected {@link VariableDeclarationFragment}s, which are
	 *         assigned somewhere in the corresponding class
	 */
	public List<VariableDeclarationFragment> getAssignedVariableDeclarationFragments() {
		return assignedFragments;
	}

	private void storeCurrentlySkipped(ASTNode initializer) {
		ASTNode parent = initializer.getParent();
		if (parent.getNodeType() == ASTNode.TYPE_DECLARATION) {

			TypeDeclaration typeDeclarationParent = (TypeDeclaration) initializer.getParent();
			List<VariableDeclarationFragment> declaredInType = Arrays.stream(typeDeclarationParent.getFields())
					.flatMap(filed -> ASTNodeUtil
							.convertToTypedList(filed.fragments(), VariableDeclarationFragment.class).stream())
					.collect(Collectors.toList());
			currentlySkipped.put(initializer, declaredInType);
		}
	}
}
