package eu.jsparrow.core.visitor.loop.stream;

import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * Analyzes the occurrences of {@link EnhancedForStatement}s and checks whether
 * a transformation to {@link Stream#anyMatch(java.util.function.Predicate)} is
 * possible. Considers two cases:
 * 
 * <ul>
 * <li>The for loop is only used to for instantiating a {@code boolean}
 * variable. For example:
 * 
 * <pre>
 * <code>
 * boolean containsEmpty = false;
 * for (String value : strings) {
 *     if(value.isEmpty()) {
 *         containsEmpty = true;
 *         break;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * is transformed into:
 * 
 * <pre>
 * <code>{@code
 * boolean containsEmpty = strings.stream().anyMatch(value -> value.isEmpty());
 * }
 * </code>
 * </pre>
 * 
 * </li>
 * <li>The for loop is only used for calculating a {@code boolean} value to be
 * returned. For example:
 * 
 * <pre>
 * <code>
 * for(String value : strings) {
 *     if(value.isEmpty()) {
 *         return true;
 *     }
 * }
 * return false;
 * </code>
 * </pre>
 * 
 * is transformed into:
 * 
 * <pre>
 * <code>{@code
 * return strings.stream().anyMatch(value -> value.isEmpty());
 * }
 * </code>
 * </pre>
 * 
 * </li>
 * </ul>
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class EnhancedForLoopToStreamAnyMatchASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	private static final String ANY_MATCH = "anyMatch"; //$NON-NLS-1$

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {

		SingleVariableDeclaration enhancedForParameter = enhancedForStatement.getParameter();
		Expression enhancedForExp = enhancedForStatement.getExpression();

		IfStatement ifStatement = isConvertableInterruptedLoop(enhancedForStatement);
		if (ifStatement == null) {
			return true;
		}

		Expression ifCondition = ifStatement.getExpression();
		Statement thenStatement = ifStatement.getThenStatement();
		VariableDeclarationFragment booleanDeclFragment;
		ReturnStatement returnStatement;

		if ((booleanDeclFragment = isAssignmentAndBreakBlock(thenStatement, enhancedForStatement)) != null) {
			/*
			 * replace initialization of the boolean variable with
			 * Stream::anyMatch
			 */
			MethodInvocation methodInvocation = createStreamAnymatchInitalizer(enhancedForExp, ifCondition,
					enhancedForParameter);
			astRewrite.replace(booleanDeclFragment.getInitializer(), methodInvocation, null);
			replaceLoopWithFragment(enhancedForStatement, booleanDeclFragment);
			getCommentHelper().saveRelatedComments(enhancedForStatement);
			onRewrite();

		} else if ((returnStatement = isReturnBlock(thenStatement, enhancedForStatement)) != null) {
			// replace the return statement with a Stream::AnyMatch
			MethodInvocation methodInvocation = createStreamAnymatchInitalizer(enhancedForExp, ifCondition,
					enhancedForParameter);
			astRewrite.replace(returnStatement.getExpression(), methodInvocation, null);
			astRewrite.remove(enhancedForStatement, null);
			getCommentHelper().saveRelatedComments(enhancedForStatement);
			onRewrite();
		}

		return true;
	}

	/**
	 * Creates an invocation of
	 * {@link Stream#anyMatch(java.util.function.Predicate)} which is equivalent
	 * to a {@link EnhancedForStatement}.
	 * 
	 * @param enhancedForExp
	 *            the expression of a {@link EnhancedForStatement}.
	 * @param ifCondition
	 *            the expression of a {@link IfStatement} which is the only
	 *            statement occurring in the body of a
	 *            {@link EnhancedForStatement}.
	 * @param param
	 *            the variable declaration occurring as a parameter in the
	 *            {@link EnhancedForStatement}
	 * @return a method invocation of the form: <code>
	 *         enhancedForExp.stream().anyMatch(param -> ifCondition) <code>
	 */
	private MethodInvocation createStreamAnymatchInitalizer(Expression enhancedForExp, Expression ifCondition,
			SingleVariableDeclaration param) {
		AST ast = astRewrite.getAST();
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName(STREAM));
		stream.setExpression(createExpressionForStreamMethodInvocation(enhancedForExp));

		MethodInvocation anyMatch = ast.newMethodInvocation();
		anyMatch.setName(ast.newSimpleName(ANY_MATCH));
		anyMatch.setExpression(stream);

		LambdaExpression anyMatchCondition = ast.newLambdaExpression();
		anyMatchCondition.setBody(astRewrite.createMoveTarget(ifCondition));
		ListRewrite lambdaRewrite = astRewrite.getListRewrite(anyMatchCondition, LambdaExpression.PARAMETERS_PROPERTY);
		anyMatchCondition.setParentheses(false);
		VariableDeclarationFragment newDeclFragment = ast.newVariableDeclarationFragment();
		newDeclFragment.setName((SimpleName) astRewrite.createCopyTarget(param.getName()));
		lambdaRewrite.insertFirst(newDeclFragment, null);

		ListRewrite anyMatchParamRewrite = astRewrite.getListRewrite(anyMatch, MethodInvocation.ARGUMENTS_PROPERTY);
		anyMatchParamRewrite.insertFirst(anyMatchCondition, null);

		return anyMatch;
	}

	/**
	 * Checks whether the body of a <em>then statement</em> consists of a block
	 * of exactly two statements where the first one is an assignment to
	 * {@code true} of a boolean variable and the second one is a
	 * {@link BreakStatement}.
	 * 
	 * @param thenStatement
	 *            a node representing the 'then statement' of an
	 *            {@link IfStatement}.
	 * @param forNode
	 *            the enhanced for-loop containing the {@link IfStatement}.
	 * 
	 * @return the declaration fragment of the boolean variable which is
	 *         assigned in the given thenStatement or {@code null} if it is not
	 *         possible to transform the loop into an invocation of
	 *         {@link Stream#anyMatch(java.util.function.Predicate)}.
	 */
	private VariableDeclarationFragment isAssignmentAndBreakBlock(Statement thenStatement,
			EnhancedForStatement forNode) {

		Assignment assignment = super.isAssignmentAndBreak(thenStatement);
		if (assignment != null) {
			Expression lhs = assignment.getLeftHandSide();
			Expression rhs = assignment.getRightHandSide();
			if (ASTNode.BOOLEAN_LITERAL == rhs.getNodeType() && ASTNode.SIMPLE_NAME == lhs.getNodeType()
					&& ((BooleanLiteral) rhs).booleanValue()) {
				SimpleName boolVarName = (SimpleName) lhs;
				return findBoolDeclFragment(boolVarName, forNode);
			}
		}

		return null;
	}

	/**
	 * Makes use of {@link LoopWithBreakStatementVisitor} for finding the
	 * declaration fragment of the boolean variable that is assigned in the body
	 * of the given {@link EnhancedForStatement}.
	 * 
	 * @param boolVarName
	 *            the name of the boolean variable
	 * @param forNode
	 *            the loop used for assigning the boolean variable
	 * @return the declaration fragment of the boolean variable with the given
	 *         name, or {@code null} if the fragment cannot be found.
	 */
	private VariableDeclarationFragment findBoolDeclFragment(SimpleName boolVarName, EnhancedForStatement forNode) {
		ASTNode loopParent = forNode.getParent();
		if (ASTNode.BLOCK == loopParent.getNodeType()) {
			Block parentBlock = (Block) loopParent;
			LoopWithBreakStatementVisitor analyzer = new LoopWithBreakStatementVisitor(parentBlock, forNode,
					boolVarName);
			parentBlock.accept(analyzer);
			VariableDeclarationFragment declFragment = analyzer.getDeclarationBoolFragment();

			if (declFragment != null && declFragment.getInitializer() != null) {
				Expression initializer = declFragment.getInitializer();
				if (ASTNode.BOOLEAN_LITERAL == initializer.getNodeType()) {
					BooleanLiteral fragmentInit = (BooleanLiteral) initializer;
					if (!fragmentInit.booleanValue()) {
						return declFragment;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Checks whether the given thenStatement consists of a single
	 * {@link ReturnStatement} which returns a boolean {@code true} value and
	 * whether the given enhanced for-loop is followed by a
	 * {@link ReturnStatement} which returns a boolean {@code false} value.
	 * 
	 * @param thenStatement
	 *            a node representing the 'then statement' of a
	 *            {@link IfStatement}.
	 * @param forNode
	 *            a loop having the aforementioned if statement as the only
	 *            statement in the body.
	 * @return the {@link ReturnStatement} following the the given loop, or
	 *         {@code null} if the loop is not followed by a return statement or
	 *         if the transformation is not possible.
	 */
	private ReturnStatement isReturnBlock(Statement thenStatement, EnhancedForStatement forNode) {
		ReturnStatement returnStatement = super.isReturnBlock(thenStatement);
		if (returnStatement != null) {
			Expression returnedExpression = returnStatement.getExpression();
			if (returnedExpression != null && ASTNode.BOOLEAN_LITERAL == returnedExpression.getNodeType()) {
				BooleanLiteral booleanLiteral = (BooleanLiteral) returnedExpression;
				if (booleanLiteral.booleanValue()) {
					return isFollowedByReturnStatement(forNode);

				}
			}

		}

		return null;
	}

	/**
	 * Finds the {@link ReturnStatement} which is placed immediately after the
	 * given {@link EnhancedForStatement} and which returns a {@code false}
	 * value.
	 * 
	 * @param forNode
	 *            represents an enhanced for loop which is expected to be
	 *            followed by a {@code return false;} statement.
	 * 
	 * @return the return statement following the given
	 *         {@link EnhancedForStatement} or {@code null} if the loop is not
	 *         followed by a return statement or the returned value is not
	 *         {@code false}
	 */
	@Override
	protected ReturnStatement isFollowedByReturnStatement(EnhancedForStatement forNode) {
		ReturnStatement followingReturnSt = super.isFollowedByReturnStatement(forNode);
		if (followingReturnSt != null) {
			Expression returnedExpression = followingReturnSt.getExpression();
			if (returnedExpression != null && ASTNode.BOOLEAN_LITERAL == returnedExpression.getNodeType()
					&& !((BooleanLiteral) returnedExpression).booleanValue()) {
				return followingReturnSt;
			}

		}

		return null;
	}
}
