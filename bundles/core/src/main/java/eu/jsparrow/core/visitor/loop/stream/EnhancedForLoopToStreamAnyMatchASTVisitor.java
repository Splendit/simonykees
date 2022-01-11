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
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.OperatorUtil;

/**
 * Analyzes the occurrences of {@link EnhancedForStatement}s and checks whether
 * a transformation to {@link Stream#anyMatch}, {@link Stream#allMatch} or
 * {@link Stream#noneMatch} is possible. Considers loops interrupted with a
 * {@link BreakStatement} and loops interrupted with a {@link ReturnStatement}.
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
 * @since 2.1.1
 *
 */
public class EnhancedForLoopToStreamAnyMatchASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	private static final String ANY_MATCH = "anyMatch"; //$NON-NLS-1$
	private static final String ALL_MATCH = "allMatch"; //$NON-NLS-1$
	private static final String NONE_MATCH = "noneMatch"; //$NON-NLS-1$

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {

		if (isConditionalExpression(enhancedForStatement.getExpression())) {
			return true;
		}
		
		SingleVariableDeclaration loopParameter = enhancedForStatement.getParameter();
		if(isGeneratedNode(loopParameter.getType())) {
			return true;
		}

		IfStatement ifStatement = isConvertableInterruptedLoop(enhancedForStatement);
		if (ifStatement == null) {
			return true;
		}

		Expression ifCondition = ifStatement.getExpression();
		Statement thenStatement = ifStatement.getThenStatement();

		Assignment assignment = isAssignmentAndBreak(thenStatement);
		if (assignment != null) {
			analyzeLoopWithBreakStatement(enhancedForStatement, ifCondition, assignment);
			return true;
		}

		ReturnStatement returnStatement = isReturnBlock(thenStatement);
		if (returnStatement != null) {
			analyzeLoopWithReturnStatement(enhancedForStatement, ifCondition, returnStatement);
		}

		return true;
	}

	private void analyzeLoopWithReturnStatement(EnhancedForStatement enhancedForStatement, Expression ifCondition,
			ReturnStatement returnStatement) {
		Expression returnedExpression = returnStatement.getExpression();

		if (returnedExpression == null || ASTNode.BOOLEAN_LITERAL != returnedExpression.getNodeType()) {
			return;
		}

		BooleanLiteral booleanLiteral = (BooleanLiteral) returnedExpression;
		boolean booleanLiteralValue = booleanLiteral.booleanValue();
		ReturnStatement followingReturnStatement = isFollowedByReturnStatement(enhancedForStatement);

		if (followingReturnStatement == null) {
			return;
		}
		Expression followingReturnedExpression = followingReturnStatement.getExpression();
		if (followingReturnedExpression == null
				|| ASTNode.BOOLEAN_LITERAL != followingReturnedExpression.getNodeType()) {
			return;
		}

		boolean followingBooleanLiteralValue = ((BooleanLiteral) followingReturnedExpression).booleanValue();

		Expression matchExpression;
		if (booleanLiteralValue && !followingBooleanLiteralValue) {
			/*
			 * use anyMatch replace the return statement with a Stream::AnyMatch
			 */

			matchExpression = (Expression) astRewrite.createCopyTarget(ifCondition);
			replaceLoopWithReturnStatement(enhancedForStatement, followingReturnStatement, ANY_MATCH, matchExpression);

		} else if (!booleanLiteralValue && followingBooleanLiteralValue) {
			if (useNoneMatch(ifCondition)) {
				/*
				 * replace return expression by Stream::noneMatch
				 */
				matchExpression = (Expression) astRewrite.createCopyTarget(ifCondition);
				replaceLoopWithReturnStatement(enhancedForStatement, followingReturnStatement, NONE_MATCH,
						matchExpression);
			} else {
				/*
				 * replace return expression by Stream::allMatch
				 */
				matchExpression = OperatorUtil.createNegatedExpression(ifCondition, astRewrite);
				replaceLoopWithReturnStatement(enhancedForStatement, followingReturnStatement, ALL_MATCH,
						matchExpression);
			}

		}
	}

	private void analyzeLoopWithBreakStatement(EnhancedForStatement enhancedForStatement, Expression ifCondition,
			Assignment assignment) {
		Expression lhs = assignment.getLeftHandSide();
		Expression rhs = assignment.getRightHandSide();
		if (ASTNode.BOOLEAN_LITERAL != rhs.getNodeType() || ASTNode.SIMPLE_NAME != lhs.getNodeType()) {
			return;
		}
		boolean loopAssignmentValue = ((BooleanLiteral) rhs).booleanValue();
		SimpleName boolVarName = (SimpleName) lhs;
		VariableDeclarationFragment declarationFragment = findBoolDeclFragment(boolVarName, enhancedForStatement);

		if (declarationFragment == null) {
			return;
		}

		Expression initializer = declarationFragment.getInitializer();
		if (initializer.getNodeType() != ASTNode.BOOLEAN_LITERAL) {
			return;
		}

		BooleanLiteral booleanInitializer = (BooleanLiteral) initializer;
		boolean initializerValue = booleanInitializer.booleanValue();

		Expression matchExpression;
		if (!initializerValue && loopAssignmentValue) {
			/*
			 * replace initialization of the boolean variable with
			 * Stream::anyMatch
			 */
			matchExpression = (Expression) astRewrite.createCopyTarget(ifCondition);
			replaceLoopWithBreakStatement(enhancedForStatement, declarationFragment, matchExpression, ANY_MATCH);
		} else if (initializerValue && !loopAssignmentValue) {
			if (useNoneMatch(ifCondition)) {
				/*
				 * replace initialization of the boolean variable with
				 * Stream::allMatch
				 */
				matchExpression = (Expression) astRewrite.createCopyTarget(ifCondition);
				replaceLoopWithBreakStatement(enhancedForStatement, declarationFragment, matchExpression, NONE_MATCH);
			} else {
				/*
				 * replace initialization of the boolean variable with
				 * Stream::allMatch
				 */
				matchExpression = OperatorUtil.createNegatedExpression(ifCondition, astRewrite);
				replaceLoopWithBreakStatement(enhancedForStatement, declarationFragment, matchExpression, ALL_MATCH);
			}
		}
	}

	private void replaceLoopWithReturnStatement(EnhancedForStatement enhancedForStatement,
			ReturnStatement followingReturnStatement, String methodName, Expression matchExpression) {
		SingleVariableDeclaration enhancedForParameter = enhancedForStatement.getParameter();
		Expression enhancedForExp = enhancedForStatement.getExpression();
		MethodInvocation methodInvocation = createStreamAnymatchInitalizer(enhancedForExp, matchExpression,
				enhancedForParameter, methodName);
		astRewrite.replace(followingReturnStatement.getExpression(), methodInvocation, null);
		astRewrite.remove(enhancedForStatement, null);
		getCommentRewriter().saveRelatedComments(enhancedForStatement);
		addMarkerEvent(enhancedForStatement);
		onRewrite();
	}

	private void replaceLoopWithBreakStatement(EnhancedForStatement enhancedForStatement,
			VariableDeclarationFragment declarationFragment, Expression matchExpression, String methodName) {
		Expression enhancedForExpression = enhancedForStatement.getExpression();
		SingleVariableDeclaration enhancedForParameter = enhancedForStatement.getParameter();
		Expression initializer = declarationFragment.getInitializer();

		MethodInvocation methodInvocation = createStreamAnymatchInitalizer(enhancedForExpression, matchExpression,
				enhancedForParameter, methodName);
		astRewrite.replace(initializer, methodInvocation, null);
		replaceLoopWithFragment(enhancedForStatement, declarationFragment);
		getCommentRewriter().saveRelatedComments(enhancedForStatement);
		addMarkerEvent(enhancedForStatement);
		onRewrite();
	}

	private boolean useNoneMatch(Expression ifCondition) {
		if (ifCondition.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
			PrefixExpression infixExpression = (PrefixExpression) ifCondition;
			return infixExpression.getOperator() != PrefixExpression.Operator.NOT;
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
			SingleVariableDeclaration param, String methodName) {
		AST ast = astRewrite.getAST();
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName(STREAM));
		stream.setExpression(createExpressionForStreamMethodInvocation(enhancedForExp));

		MethodInvocation anyMatch = ast.newMethodInvocation();
		anyMatch.setName(ast.newSimpleName(methodName));
		anyMatch.setExpression(stream);

		LambdaExpression anyMatchCondition = ast.newLambdaExpression();
		anyMatchCondition.setBody(ifCondition);
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
					return declFragment;
				}
			}
		}

		return null;
	}
}
