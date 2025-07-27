package eu.jsparrow.core.visitor.security.random;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Intended to be used exclusively by
 * {@link UseSecureRandomASTVisitor#visit(ClassInstanceCreation)}.
 * <p>
 * This visitor will find out whether a given {@link ClassInstanceCreation} is
 * an invocation of
 * <ul>
 * <li>{@link java.util.Random#Random()} or</li>
 * <li>{@link java.util.Random#Random(long)}</li>
 * </ul>
 * which can be replaced by an invocation of
 * {@link java.security.SecureRandom#SecureRandom()}.
 * 
 * @since 3.20.0
 *
 */
class NewRandomAnalyzer {

	private static final SignatureData SIGNATURE_WITHOUT_PARAMETER = new SignatureData(
			java.util.Random.class, java.util.Random.class.getSimpleName());
	private static final SignatureData SIGNATURE_WITH_SEED_PARAMETER = new SignatureData(
			java.util.Random.class, java.util.Random.class.getSimpleName(), long.class);
	private Expression seedArgument;
	private Expression randomExpressionToReplace;
	private Statement randomConstructionStatement;
	private Expression assignmentTarget;
	private Block blockOfConstructionStatement;

	private Expression findAssignmentTarget(Expression randomExpression) {
		StructuralPropertyDescriptor locationInParent = randomExpression.getLocationInParent();
		if (locationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			return ((VariableDeclarationFragment) randomExpression.getParent()).getName();
		} else if (locationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			return ((Assignment) randomExpression.getParent()).getLeftHandSide();
		}
		return null;
	}

	private Statement findConstructionStatement(Expression randomExpression) {
		if (randomExpression
			.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) randomExpression
				.getParent();
			if (variableDeclarationFragment.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				return (VariableDeclarationStatement) variableDeclarationFragment.getParent();
			}
		} else if (randomExpression.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) randomExpression.getParent();
			if (assignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				return (ExpressionStatement) assignment.getParent();
			}
		}
		return null;
	}

	private SignatureData findRandomConstructorSignature(ClassInstanceCreation classInstanceCreation) {
		IMethodBinding constructorBinding = classInstanceCreation.resolveConstructorBinding();
		if (SIGNATURE_WITHOUT_PARAMETER.isEquivalentTo(constructorBinding)) {
			return SIGNATURE_WITHOUT_PARAMETER;
		}
		if (SIGNATURE_WITH_SEED_PARAMETER.isEquivalentTo(constructorBinding)) {
			return SIGNATURE_WITH_SEED_PARAMETER;
		}
		return null;
	}

	/**
	 * 
	 * @return true if the given {@link ClassInstanceCreation} can be
	 *         transformed, otherwise {@code false}.
	 */
	public boolean analyze(ClassInstanceCreation classInstanceCreation) {
		SignatureData randomConstructorSignature = findRandomConstructorSignature(classInstanceCreation);
		if (randomConstructorSignature == null) {
			return false;
		}

		randomExpressionToReplace = classInstanceCreation;
		while (randomExpressionToReplace.getLocationInParent() == ParenthesizedExpression.EXPRESSION_PROPERTY) {
			randomExpressionToReplace = (Expression) randomExpressionToReplace.getParent();
		}

		if (randomExpressionToReplace.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY
				|| randomExpressionToReplace.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			return false;
		}

		if (randomConstructorSignature == SIGNATURE_WITH_SEED_PARAMETER) {
			seedArgument = ASTNodeUtil.convertToTypedList(classInstanceCreation.arguments(), Expression.class)
				.get(0);

			assignmentTarget = findAssignmentTarget(randomExpressionToReplace);
			if (assignmentTarget == null) {
				return false;
			}

			randomConstructionStatement = findConstructionStatement(randomExpressionToReplace);
			if (randomConstructionStatement == null) {
				return false;
			}

			Block parentBlock = ASTNodeUtil.findParentBlock(randomConstructionStatement)
				.orElse(null);
			if (parentBlock == null) {
				return false;
			}
			
			blockOfConstructionStatement = parentBlock;
		}
		return true;
	}

	/**
	 * 
	 * @return an {@link Expression} representing the seed in the case of the
	 *         invocation of {@link java.util.Random#Random(long)}, otherwise
	 *         {@code null}.
	 */
	public Expression getSeedArgument() {
		return seedArgument;
	}

	/**
	 * Getter for the expression to be replaced by an invocation of
	 * {@link java.security.SecureRandom#SecureRandom()}.
	 * 
	 * @return an instance of {@link Expression} if the invocation of
	 *         {@link #analyze()} has returned {@code true} before, otherwise
	 *         {@code null}.
	 */
	public Expression getRandomExpressionToReplace() {
		return randomExpressionToReplace;
	}

	/**
	 * 
	 * @return an instance of {@link Statement} if both a previous invocation of
	 *         {@link #analyze()} has returned true and a seed argument has been
	 *         found, otherwise {@code null}.
	 */
	public Statement getRandomConstructionStatement() {
		return randomConstructionStatement;
	}

	/**
	 * 
	 * @return an instance of {@link Expression} if both a previous invocation
	 *         of {@link #analyze()} has returned {@code true} and a seed
	 *         argument has been found, otherwise {@code null}.
	 */
	public Expression getAssignmentTarget() {
		return assignmentTarget;
	}

	/**
	 * 
	 * @return an instance of {@link Block} if both a previous invocation of
	 *         {@link #analyze()} has returned {@code true} and a seed argument
	 *         has been found, otherwise {@code null}.
	 */
	public Block getBlockOfConstructionStatement() {
		return blockOfConstructionStatement;
	}
}
