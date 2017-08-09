package at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
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

import at.splendit.simonykees.core.visitor.sub.LocalVariableUsagesASTVisitor;

/**
 * Analyzes the occurrences of {@link EnhancedForStatement}s and checks whether
 * a transformation to {@link Stream#findFirst()} is possible. Considers two
 * cases:
 * 
 * <ul>
 * <li>The for loop is only used to for assigning a variable:
 * 
 * <pre>
 * <code>
 * 		String nonEmpty = strings.get(0);
 *		for(String value : strings) {
 *			if(!value.isEmpty()) {
 *				nonEmpty = value;
 *				break;
 *			}
 *		}
 * </code>
 * </pre>
 * 
 * is transformed into:
 * 
 * <pre>
 * <code>
 * 	String nonEmpty = strings.stream().filter(value -> !value.isEmpty()).findFirst().orElse(strings.get(0));
 * </code>
 * </pre>
 * 
 * </li>
 * <li>The for loop is only used for computing a value to be returned. For
 * example:
 * 
 * <pre>
 * <code>
 *		for(String value : strings) {
 *			if(!value.isEmpty()) {
 *				return value;
 *			}
 *		}
 *		return strings.get(0);
 * </code>
 * </pre>
 * 
 * is transformed into:
 * 
 * <pre>
 * <code>
 * 	return strings.stream().filter(value -> !value.isEmpty()).findFirst().orElse(strings.get(0));
 * </code>
 * </pre>
 * 
 * </li>
 * 
 * </ul>
 * 
 * A {@link Stream#map(Function)} invocation is used next of {@code findFirst()}
 * if the computed value is more than a simple reference of the loop variable.
 * 
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class EnhancedForLoopToStreamFindFirstASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	private static final String FIND_FIRST = "findFirst"; //$NON-NLS-1$
	private static final String OR_ELSE = "orElse"; //$NON-NLS-1$

	@Override
	public boolean visit(EnhancedForStatement forLoop) {

		Expression loopExpression = forLoop.getExpression();
		SingleVariableDeclaration loopParameter = forLoop.getParameter();

		IfStatement ifStatement = isConvertableInterruptedLoop(forLoop);
		if (ifStatement == null) {
			return true;
		}

		Expression ifCondition = ifStatement.getExpression();
		Statement thenStatement = ifStatement.getThenStatement();
		VariableDeclarationFragment varDeclFragment;
		ReturnStatement returnStatement;
		List<Expression> tailingMap = new ArrayList<>();

		if ((varDeclFragment = isConvertibleWithBreak(thenStatement, forLoop, loopParameter.getName(),
				tailingMap)) != null) {
			/*
			 * replace the initializer with a Stream::findFirst
			 */
			MethodInvocation methodInvocation = createStreamFindFirstInitalizer(loopExpression, ifCondition,
					loopParameter, varDeclFragment.getInitializer(), tailingMap);
			astRewrite.replace(varDeclFragment.getInitializer(), methodInvocation, null);
			replaceLoopWithFragment(forLoop, varDeclFragment);

		} else if ((returnStatement = isConvertableWithReturn(thenStatement, forLoop, loopParameter.getName(),
				tailingMap)) != null) {
			// replace the return statement with a Stream::findFirst
			MethodInvocation methodInvocation = createStreamFindFirstInitalizer(loopExpression, ifCondition,
					loopParameter, returnStatement.getExpression(), tailingMap);
			astRewrite.replace(returnStatement.getExpression(), methodInvocation, null);
			astRewrite.remove(forLoop, null);
		}

		return true;
	}

	/**
	 * Checks whether a loop using a break statement is convertible to a
	 * {@link Stream#findFirst()} expression.
	 * 
	 * @param thenStatement
	 *            the body of the if statement occurring in the loop
	 * @param forLoop
	 *            the loop being analyzed
	 * @param loopParameterName
	 *            a node representing the name of the loop variable
	 * @param tailingMap
	 *            an empty list
	 * 
	 * @return the declaration of the variable which is being assigned in the
	 *         body of the loop, or {@code null} if the transformation is not
	 *         possible.
	 */
	private VariableDeclarationFragment isConvertibleWithBreak(Statement thenStatement, EnhancedForStatement forLoop,
			SimpleName loopParameterName, List<Expression> tailingMap) {
		Assignment assignmentAfterBreak = super.isAssignmentAndBreak(thenStatement);
		if (assignmentAfterBreak != null) {
			Expression rhs = assignmentAfterBreak.getRightHandSide();
			if (isReferencingParameter(rhs, loopParameterName, tailingMap)) {
				Expression lhs = assignmentAfterBreak.getLeftHandSide();
				if (ASTNode.SIMPLE_NAME == lhs.getNodeType()) {
					return findDeclarationFragment(forLoop, (SimpleName) lhs);
				}
			}
		}
		return null;
	}

	/**
	 * Searches the block enclosing the given loop for finding the declaration
	 * fragment of the variable with the given name.
	 * 
	 * @param forLoop
	 *            the loop which is used for assigning the given variable name
	 * @param varName
	 *            the name of the assigned variable
	 * @return the declaration fragment of the assigned variable, or
	 *         {@code null} if it declaration fragment was not found.
	 */
	private VariableDeclarationFragment findDeclarationFragment(EnhancedForStatement forLoop, SimpleName varName) {
		ASTNode loopParent = forLoop.getParent();
		if (ASTNode.BLOCK == loopParent.getNodeType()) {
			Block parentBlock = (Block) loopParent;
			LoopWithBreakStatementVisitor analyzer = new LoopWithBreakStatementVisitor(parentBlock, forLoop, varName);
			parentBlock.accept(analyzer);
			VariableDeclarationFragment declFragment = analyzer.getDeclarationBoolFragment();
			if (declFragment != null && declFragment.getInitializer() != null) {
				return declFragment;
			}
		}
		return null;
	}

	/**
	 * Checks whether a loop using a return statement is convertible to a
	 * {@link Stream#findFirst()} expression.
	 * 
	 * @param thenStatement
	 *            the body of the if statement occurring in the loop
	 * @param forLoop
	 *            the loop being analyzed
	 * @param loopParameterName
	 *            a node representing the name of the loop variable
	 * @param tailingMap
	 *            an empty list
	 * 
	 * @return the declaration of the variable which is being assigned in the
	 *         body of the loop, or {@code null} if the transformation is not
	 *         possible.
	 */
	private ReturnStatement isConvertableWithReturn(Statement thenStatement, EnhancedForStatement forLoop,
			SimpleName parameter, List<Expression> tailingMap) {
		ReturnStatement returnStatement = isReturnBlock(thenStatement);
		if (returnStatement != null) {
			Expression returnedExpression = returnStatement.getExpression();
			if (returnedExpression != null && isReferencingParameter(returnedExpression, parameter, tailingMap)) {
				ReturnStatement followingReturnStatement = isFollowedByReturnStatement(forLoop);
				if (followingReturnStatement != null && followingReturnStatement.getExpression() != null) {
					return followingReturnStatement;
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the given expression has a reference to the variable with the
	 * given name. If the expression is a simple name itself, then its
	 * identifier is compared with the identifier of the given name. Otherwise,
	 * an instance of the {@link LocalVariableUsagesASTVisitor} is used for
	 * searching for references, and if one is found, the expression is added to
	 * the given list.
	 * 
	 * @param expression
	 *            to be searched for references.
	 * @param parameter
	 *            the name of the variable to check the references for.
	 * @param tailingMap
	 *            an empty list used for adding the expression if a reference is
	 *            found.
	 * @return {@code true} if a reference is found or {@code false} otherwise.
	 */
	private boolean isReferencingParameter(Expression expression, SimpleName parameter, List<Expression> tailingMap) {
		if (ASTNode.SIMPLE_NAME == expression.getNodeType()) {
			SimpleName expressionName = (SimpleName) expression;
			return expressionName.getIdentifier().equals(parameter.getIdentifier());
		} else {
			LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(parameter);
			expression.accept(visitor);
			if (!visitor.getUsages().isEmpty()) {
				tailingMap.add(expression);
				return true;
			}
		}

		return false;
	}

	/**
	 * Constructs the expression of the form:
	 * 
	 * <pre>
	 *  <code>
	 *  loopExpression.stream().filter(loopParameter -> ifCondition).findAny().orElse(orElseExpression);
	 *  </code>
	 * </pre>
	 * 
	 * If the list of tailingMaps is not empty, then a chain of map invocation
	 * is added added after the {@code findAny} occurrence.
	 * 
	 * @param loopExpression
	 *            representing the object which is being iterated by the loop
	 * @param ifCondition
	 *            the expression representing the condition of the if statement
	 *            occurring in the loop.
	 * @param loopParameter
	 *            the name of the loop parameter
	 * @param orElseExpression
	 *            the expression to be used as a parameter of the {@code orElse}
	 *            invocation
	 * @param tailingMaps
	 *            a list of mapping expression if a chain of maps need to be
	 *            inserted after {@code findFirst} invocation.
	 * 
	 * @return the resulting chain of method invocations.
	 */
	private MethodInvocation createStreamFindFirstInitalizer(Expression loopExpression, Expression ifCondition,
			SingleVariableDeclaration loopParameter, Expression orElseExpression, List<Expression> tailingMaps) {
		AST ast = astRewrite.getAST();
		/*
		 * creating:
		 * 
		 * loopExpression.stream()
		 */
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName(STREAM));
		stream.setExpression(createExpressionForStreamMethodInvocation(loopExpression));

		/*
		 * creating:
		 * 
		 * loopExpression.stream().filter(param -> ifCondition)
		 */
		MethodInvocation filter = ast.newMethodInvocation();
		filter.setName(ast.newSimpleName(FILTER));
		filter.setExpression(stream);

		LambdaExpression findFirstCondition = ast.newLambdaExpression();
		findFirstCondition.setBody(astRewrite.createMoveTarget(ifCondition));
		ListRewrite lambdaRewrite = astRewrite.getListRewrite(findFirstCondition, LambdaExpression.PARAMETERS_PROPERTY);
		findFirstCondition.setParentheses(false);
		VariableDeclarationFragment newDeclFragment = ast.newVariableDeclarationFragment();
		newDeclFragment.setName((SimpleName) astRewrite.createCopyTarget(loopParameter.getName()));
		lambdaRewrite.insertFirst(newDeclFragment, null);

		ListRewrite filterParamRewrite = astRewrite.getListRewrite(filter, MethodInvocation.ARGUMENTS_PROPERTY);
		filterParamRewrite.insertFirst(findFirstCondition, null);

		/*
		 * creating:
		 * 
		 * loopExpression.stream().filter(param -> ifCondition).findFirst()
		 */
		MethodInvocation findFirst = ast.newMethodInvocation();
		findFirst.setName(ast.newSimpleName(FIND_FIRST));
		findFirst.setExpression(filter);
		MethodInvocation optionalExpression = findFirst;

		for (Expression mapExpression : tailingMaps) {
			/*
			 * creating:
			 * 
			 * loopExpression.stream().filter(param ->
			 * ifCondition).findFirst().map(param -> mapExpression)
			 */
			MethodInvocation map = ast.newMethodInvocation();
			map.setName(ast.newSimpleName(MAP));
			map.setExpression(optionalExpression);
			optionalExpression = map;

			LambdaExpression mapLambdaExpression = ast.newLambdaExpression();
			mapLambdaExpression.setBody(astRewrite.createCopyTarget(mapExpression));
			mapLambdaExpression.setParentheses(false);
			lambdaRewrite = astRewrite.getListRewrite(mapLambdaExpression, LambdaExpression.PARAMETERS_PROPERTY);
			VariableDeclarationFragment mapDeclFragment = ast.newVariableDeclarationFragment();
			mapDeclFragment.setName((SimpleName) astRewrite.createCopyTarget(loopParameter.getName()));
			lambdaRewrite.insertFirst(mapDeclFragment, null);

			ListRewrite tailingMapRewrite = astRewrite.getListRewrite(map, MethodInvocation.ARGUMENTS_PROPERTY);
			tailingMapRewrite.insertFirst(mapLambdaExpression, null);
		}

		/*
		 * creating:
		 * 
		 * loopExpression.stream().filter(param -> ifCondition).findFirst()
		 * [.map(param -> mapExpression)].orElse(orElseExpression)
		 */
		MethodInvocation orElse = ast.newMethodInvocation();
		orElse.setName(ast.newSimpleName(OR_ELSE));
		orElse.setExpression(optionalExpression);

		ListRewrite orElseParamRewrite = astRewrite.getListRewrite(orElse, MethodInvocation.ARGUMENTS_PROPERTY);
		orElseParamRewrite.insertFirst(orElseExpression, null);

		return orElse;
	}
}
