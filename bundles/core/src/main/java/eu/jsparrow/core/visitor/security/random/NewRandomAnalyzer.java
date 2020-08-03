package eu.jsparrow.core.visitor.security.random;

import java.util.Collections;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.visitor.security.common.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Carries out all operations which are necessary to determine whether a given
 * {@link ClassInstanceCreation} is an invocation of
 * {@link java.util.Random#Random()} or {@link java.util.Random#Random(long)}
 * which can be replaced by an invocation of
 * {@link java.security.SecureRandom#SecureRandom()}.
 * 
 * @since 3.20.0
 *
 */
public class NewRandomAnalyzer {

	private static final SignatureData SIGNATURE_WITHOUT_PARAMETER = new SignatureData(
			java.util.Random.class.getName(), java.util.Random.class.getSimpleName(), Collections.emptyList());
	private static final SignatureData SIGNATURE_WITH_SEED_PARAMETER = new SignatureData(
			java.util.Random.class.getName(), java.util.Random.class.getSimpleName(),
			Collections.singletonList(long.class.getSimpleName()));
	private final ClassInstanceCreation classInstanceCreation;
	private Expression seedArgument;
	private Expression nonParenthesizedRandomExpression;
	Statement randomConstructionStatement;
	Expression assignmentTarget;
	Block blockOfConstructionStatement;

	public NewRandomAnalyzer(ClassInstanceCreation classInstanceCreation) {
		this.classInstanceCreation = classInstanceCreation;
	}

	private Expression findAssignmentTarget(Expression randomExpression) {
		if (randomExpression
			.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) randomExpression
				.getParent();
			if (variableDeclarationFragment.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				return variableDeclarationFragment.getName();
			}
		} else if (randomExpression.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) randomExpression.getParent();
			if (assignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				return assignment.getLeftHandSide();
			}
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

	/**
	 * 
	 * @return true if the given {@link ClassInstanceCreation} can be
	 *         transformed, otherwise false.
	 */
	public boolean analyze() {
		IMethodBinding constructorBinding = classInstanceCreation.resolveConstructorBinding();
		boolean isNewRandom = false;
		if (SIGNATURE_WITHOUT_PARAMETER.isEquivalentTo(constructorBinding)) {
			isNewRandom = true;
		} else if (SIGNATURE_WITH_SEED_PARAMETER.isEquivalentTo(constructorBinding)) {
			isNewRandom = true;
			seedArgument = ASTNodeUtil.convertToTypedList(classInstanceCreation.arguments(), Expression.class)
				.get(0);
		}
		if (!isNewRandom) {
			return false;
		}

		nonParenthesizedRandomExpression = classInstanceCreation;
		while (nonParenthesizedRandomExpression.getLocationInParent() == ParenthesizedExpression.EXPRESSION_PROPERTY) {
			nonParenthesizedRandomExpression = (Expression) nonParenthesizedRandomExpression.getParent();
		}

		if (nonParenthesizedRandomExpression.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY
				|| nonParenthesizedRandomExpression.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			return false;
		}

		if (seedArgument == null) {
			return true;
		}

		assignmentTarget = findAssignmentTarget(nonParenthesizedRandomExpression);
		if (assignmentTarget == null) {
			return false;
		}

		randomConstructionStatement = findConstructionStatement(nonParenthesizedRandomExpression);
		if (randomConstructionStatement == null) {
			return false;
		}

		if (randomConstructionStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return false;
		}
		blockOfConstructionStatement = (Block) randomConstructionStatement.getParent();

		return true;
	}

	/**
	 * 
	 * @return the {@link ClassInstanceCreation} which can be transformed,
	 *         provided that {@link #analyze() has returned true.
	 */
	public ClassInstanceCreation getClassInstanceCreation() {
		return classInstanceCreation;
	}

	/**
	 * 
	 * @return an {@link Expression} representing the seed in the case of the
	 *         invocation of {@link java.util.Random#Random(long)}, otherwise
	 *         null.
	 */
	public Expression getSeedArgument() {
		return seedArgument;
	}

	/**
	 * This method will return a non-null value only if a previous invocation of
	 * {@link #analyze()} has returned true before.
	 * 
	 * @return the outermost {@link ParenthesizedExpression} if the
	 *         {@link ClassInstanceCreation} is parenthesized, otherwise the
	 *         {@link ClassInstanceCreation} itself.
	 */
	public Expression getNonParenthesizedRandomExpression() {
		return nonParenthesizedRandomExpression;
	}

	/**
	 * 
	 * @return an instance of {@link Statement} if both a previous invocation of
	 *         {@link #analyze()} has returned true and a seed argument has been
	 *         found, otherwise null.
	 */
	public Statement getRandomConstructionStatement() {
		return randomConstructionStatement;
	}

	/**
	 * 
	 * @return an instance of {@link Expression} if both a previous invocation
	 *         of {@link #analyze()} has returned true and a seed argument has
	 *         been found, otherwise null.
	 */
	public Expression getAssignmentTarget() {
		return assignmentTarget;
	}

	/**
	 * 
	 * @return an instance of {@link Block} if both a previous invocation of
	 *         {@link #analyze()} has returned true and a seed argument has been
	 *         found, otherwise null.
	 */
	public Block getBlockOfConstructionStatement() {
		return blockOfConstructionStatement;
	}
}
