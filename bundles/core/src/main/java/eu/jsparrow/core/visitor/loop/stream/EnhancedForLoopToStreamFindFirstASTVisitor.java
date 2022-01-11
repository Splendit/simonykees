package eu.jsparrow.core.visitor.loop.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.utils.MethodDeclarationUtils;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

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
 * @since 2.1.1
 *
 */
public class EnhancedForLoopToStreamFindFirstASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	private static final String FIND_FIRST = "findFirst"; //$NON-NLS-1$
	private static final String OR_ELSE = "orElse"; //$NON-NLS-1$

	@Override
	public boolean visit(EnhancedForStatement forLoop) {

		Expression loopExpression = forLoop.getExpression();
		if (isConditionalExpression(loopExpression)) {
			return true;
		}
		SingleVariableDeclaration loopParameter = forLoop.getParameter();
		if(isGeneratedNode(loopParameter.getType())) {
			return true;
		}

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
			if (containsNonEffectiveVariable(tailingMap)) {
				return true;
			}

			/*
			 * if the type of the stream preceding findFirst() does not match
			 * with the type of the orElse() expression, then an explicit
			 * casting should be add by using a findFirst().map() The
			 * boxingExpresions list, is used for storing the mapping
			 * expressions in such cases.
			 */
			List<Expression> boxingExpresions = new ArrayList<>();
			Expression orElseExpression = checkInitializerImlicitCasting(varDeclFragment, tailingMap, boxingExpresions,
					loopExpression);
			List<Expression> mapCopyTargets = tailingMap.stream()
				.map(e -> (Expression) astRewrite.createCopyTarget(e))
				.collect(Collectors.toList());
			mapCopyTargets.addAll(boxingExpresions);

			MethodInvocation methodInvocation = createStreamFindFirstInitalizer(loopExpression, ifCondition,
					loopParameter, orElseExpression, mapCopyTargets);

			astRewrite.replace(varDeclFragment.getInitializer(), methodInvocation, null);
			replaceLoopWithFragment(forLoop, varDeclFragment);
			getCommentRewriter().saveRelatedComments(forLoop);
			addMarkerEvent(forLoop);
			onRewrite();

		} else if ((returnStatement = isConvertableWithReturn(thenStatement, forLoop, loopParameter.getName(),
				tailingMap)) != null) {
			if (containsNonEffectiveVariable(tailingMap)) {
				return true;
			}
			if (!isNonPrimitiveTypeCompatible(returnStatement.getExpression(), loopExpression, tailingMap)) {
				return true;
			}
			List<Expression> boxingExpresions = new ArrayList<>();
			Expression orElseExpression = boxReturnExpressionIfPrimitive(returnStatement, tailingMap, boxingExpresions,
					loopExpression);
			List<Expression> mapCopyTargets = tailingMap.stream()
				.map(e -> (Expression) astRewrite.createCopyTarget(e))
				.collect(Collectors.toList());
			mapCopyTargets.addAll(boxingExpresions);
			MethodInvocation methodInvocation = createStreamFindFirstInitalizer(loopExpression, ifCondition,
					loopParameter, orElseExpression, mapCopyTargets);
			astRewrite.replace(returnStatement.getExpression(), methodInvocation, null);
			astRewrite.remove(forLoop, null);
			getCommentRewriter().saveRelatedComments(forLoop);
			addMarkerEvent(forLoop);
			onRewrite();
		}

		return true;
	}

	private boolean containsNonEffectiveVariable(List<Expression> tailingMap) {
		return tailingMap.stream()
			.anyMatch(this::containsNonEffectivelyFinalVariable);
	}

	/**
	 * Boxes the expression if its type is a primitive type. Creates a copy
	 * target of the given expression. The expression to be boxed should be part
	 * of the ast.
	 * 
	 * @param expression
	 *            an expression to be boxed
	 * @param expectedType
	 *            the type to be boxed to
	 * @return the new boxed expression if its original type is primitve, or the
	 *         unchanged expression otherwise.
	 */
	private Expression boxIfPrimitive(Expression expression, ITypeBinding expectedType) {
		if (expectedType == null || !expectedType.isPrimitive()) {
			/*
			 * The returned expression should be a new node.
			 */
			return (Expression) astRewrite.createCopyTarget(expression);
		}

		AST ast = expression.getAST();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName(VALUE_OF));
		ListRewrite miRewrite = astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);
		miRewrite.insertFirst((Expression) astRewrite.createCopyTarget(expression), null);
		String expressionName = ClassRelationUtil.findBoxedTypeOfPrimitive(expectedType);
		methodInvocation.setExpression(ast.newSimpleName(expressionName));

		return methodInvocation;
	}

	/**
	 * Checks for type compatibility between initializer of the declaration
	 * fragment, the expression in the taliningMap (if any) and the type of the
	 * elements of the loop expression. Adds a casting expression to the
	 * boxingExpression if the tailingMap is empty and the type of the elements
	 * of the loop expression is not compatible with the initializer of the
	 * declaration fragment.
	 * 
	 * @param declarationFragment
	 *            the declaration fragment of the variable being assigned in the
	 *            loop body
	 * @param tailingMap
	 *            list of mapping expressions
	 * @param boxingExpression
	 *            empty list.
	 * @param loopExpression
	 *            the expression representing the object which is being iterated
	 *            in the loop.
	 * @return the initializer of the declaration fragment wrapped in a boxing
	 *         expression if necessary.
	 */
	private Expression checkInitializerImlicitCasting(VariableDeclarationFragment declarationFragment,
			List<Expression> tailingMap, List<Expression> boxingExpression, Expression loopExpression) {
		Expression initializer = declarationFragment.getInitializer();
		ITypeBinding expectedType = declarationFragment.getName()
			.resolveTypeBinding();
		return checkImplicitCasting(tailingMap, boxingExpression, loopExpression, initializer, expectedType);
	}

	/**
	 * Checks for type compatibility between the expression of the give return
	 * statement, the expression in the taliningMap (if any) and the type of the
	 * elements of the loop expression. Adds a casting expression to the
	 * boxingExpression if the tailingMap is empty and the type of the elements
	 * of the loop expression is not compatible with the initializer of the
	 * declaration fragment.
	 * 
	 * @param returnStatement
	 *            the return statement following the loop
	 * @param tailingMap
	 *            list of mapping expressions
	 * @param boxingExpression
	 *            empty list.
	 * @param loopExpression
	 *            the expression representing the object which is being iterated
	 *            in the loop.
	 * @return the expression of the return statement wrapped in a boxing
	 *         expression if necessary.
	 */
	private Expression boxReturnExpressionIfPrimitive(ReturnStatement returnStatement, List<Expression> tailingMap,
			List<Expression> boxingExpression, Expression loopExpression) {
		Expression orElseCandidate = returnStatement.getExpression();
		ITypeBinding expectedOrElseType = MethodDeclarationUtils.findExpectedReturnType(returnStatement);

		return checkImplicitCasting(tailingMap, boxingExpression, loopExpression, orElseCandidate, expectedOrElseType);
	}

	/**
	 * Checks whether a boxing is needed for the expression of the tailingMap or
	 * the orElse candidate. Considers three cases:
	 * <ul>
	 * <li>the stream type has the same type as the orElse candidate. In this
	 * case no boxing is added</li>
	 * <li>the stream type has the same type as the expected orElse type, but is
	 * different from the actual orElse type. In this case a boxing of the
	 * actual orElse type is needed</li>
	 * <li>the actual orElse type has the same type as the expected orElse, but
	 * is different form the stream type. In this case either the expression in
	 * the tailingMap is boxed, or if the tailing map is empty, a new boxing
	 * expression is created and added to the boxing expression list.</li>
	 * </ul>
	 * 
	 * @param tailingMap
	 *            (possibly empty) list of tailing map expression
	 * @param boxingExpression
	 *            an empty list
	 * @param loopExpression
	 *            the expression representing the object beining iterated by the
	 *            loop
	 * @param orElseCandidate
	 *            the expression to be put as a parameter in the orElse
	 *            invocation
	 * @param expectedOrElseType
	 *            the expected return type of the orElse. It can be different
	 *            from the type of the current orElse candidate. For Example,
	 *            the expected type can be {@code double} but the actual type
	 *            can be {@code int}.
	 * 
	 * @return the orElse candidate wrapped into a boxing expression if needed.
	 */
	private Expression checkImplicitCasting(List<Expression> tailingMap, List<Expression> boxingExpression,
			Expression loopExpression, Expression orElseCandidate, ITypeBinding expectedOrElseType) {
		ITypeBinding orElseType = orElseCandidate.resolveTypeBinding();
		String orElseTypeBoxed = ClassRelationUtil.findBoxedTypeOfPrimitive(orElseType);
		ITypeBinding streamType;
		String streamTypeBoxed;
		if (tailingMap.isEmpty()) {
			streamType = findCollectionType(loopExpression.resolveTypeBinding());
		} else {
			streamType = tailingMap.get(tailingMap.size() - 1)
				.resolveTypeBinding();
		}

		streamType = ClassRelationUtil.findFirstTypeBound(streamType);

		streamTypeBoxed = ClassRelationUtil.findBoxedTypeOfPrimitive(streamType);

		if (!streamTypeBoxed.equals(orElseTypeBoxed)) {
			String expectedReturnTypeBoxed = ClassRelationUtil.findBoxedTypeOfPrimitive(expectedOrElseType);
			if (!expectedReturnTypeBoxed.equals(orElseTypeBoxed)) {
				orElseCandidate = boxIfPrimitive(orElseCandidate, expectedOrElseType);
			}

			if (!expectedReturnTypeBoxed.equals(streamTypeBoxed)
					&& (ClassRelationUtil.isBoxedType(streamType) || streamType.isPrimitive())) {
				if (tailingMap.isEmpty()) {
					AST ast = orElseCandidate.getAST();
					ExpressionMethodReference castingMethod = ast.newExpressionMethodReference();
					castingMethod.setExpression(ast.newSimpleName(expectedReturnTypeBoxed));
					castingMethod.setName(ast.newSimpleName(VALUE_OF));
					boxingExpression.add(castingMethod);
				} else {
					Expression exp = tailingMap.remove(tailingMap.size() - 1);
					boxingExpression.add(boxIfPrimitive(exp, expectedOrElseType));
				}
			}
		}
		return orElseCandidate;
	}

	private boolean isNonPrimitiveTypeCompatible(Expression orElseExpression, Expression loopExpression,
			List<Expression> tailingMap) {
		ITypeBinding orElseType = orElseExpression.resolveTypeBinding();
		ITypeBinding streamType;

		if (orElseExpression.getNodeType() == ASTNode.NULL_LITERAL) {
			return true;
		}

		if (ClassRelationUtil.isBoxedType(orElseType) || orElseType.isPrimitive()) {
			return true;
		}

		if (tailingMap.isEmpty()) {
			streamType = findCollectionType(loopExpression.resolveTypeBinding());
		} else {
			Expression lastMap = tailingMap.get(tailingMap.size() - 1);
			streamType = lastMap.resolveTypeBinding();
		}

		if (streamType == null) {
			return false;
		}
		return ClassRelationUtil.compareITypeBinding(orElseType, streamType) || ClassRelationUtil
			.isInheritingContentOfTypes(orElseType, Collections.singletonList(streamType.getQualifiedName()));
	}

	private ITypeBinding findCollectionType(ITypeBinding expressionTypeBinding) {
		ITypeBinding streamType;
		if (expressionTypeBinding.isParameterizedType()) {
			ITypeBinding[] typeArguments = expressionTypeBinding.getTypeArguments();
			streamType = typeArguments[0];
			return streamType;
		}

		ITypeBinding superType = expressionTypeBinding.getSuperclass();
		if (superType != null) {
			return findCollectionType(expressionTypeBinding.getSuperclass());
		}
		return null;
	}

	/**
	 * Checks whether a loop using a break statement is convertible to a
	 * {@link Stream#findFirst()} expression. Furthermore, adds the
	 * right-hand-side of the of the assignment expression to the tailingMap
	 * list, if the return expression does not exactly match the loop variable.
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
		Assignment assignmentAfterBreak = isAssignmentAndBreak(thenStatement);
		if (assignmentAfterBreak != null) {
			Expression rhs = assignmentAfterBreak.getRightHandSide();
			if (ASTNode.NULL_LITERAL != rhs.getNodeType()) {
				tailingMap.addAll(wrapNonIdentical(rhs, loopParameterName));
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
	 * {@link Stream#findFirst()} expression. Furthermore, adds the expression
	 * of the return statement to the tailingMap list, if the return expression
	 * does not exactly match the loop variable.
	 * 
	 * @param statement
	 *            the body of the if statement occurring in the loop
	 * @param forLoop
	 *            the loop being analyzed
	 * @param parameter
	 *            a node representing the name of the loop variable
	 * @param tailingMap
	 *            an empty list.
	 * 
	 * @return the declaration of the variable which is being assigned in the
	 *         body of the loop, or {@code null} if the transformation is not
	 *         possible.
	 */
	private ReturnStatement isConvertableWithReturn(Statement statement, EnhancedForStatement forLoop,
			SimpleName parameter, List<Expression> tailingMap) {
		ReturnStatement returnStatement = isReturnBlock(statement);
		if (returnStatement != null) {
			Expression returnedExpression = returnStatement.getExpression();

			if (returnedExpression != null && ASTNode.NULL_LITERAL != returnedExpression.getNodeType()
					&& !containsUndefinedTypes(returnedExpression)) {
				tailingMap.addAll(wrapNonIdentical(returnedExpression, parameter));
				ReturnStatement followingReturnStatement = isFollowedByReturnStatement(forLoop);
				if (followingReturnStatement != null && followingReturnStatement.getExpression() != null) {
					return followingReturnStatement;
				}
			}
		}
		return null;
	}

	private boolean containsUndefinedTypes(Expression returnedExpression) {
		ITypeBinding returnTypeBinding = returnedExpression.resolveTypeBinding();
		return !isTypeSafe(returnTypeBinding);
	}

	/**
	 * Checks if the given expression is identical with the given parameter.
	 * Otherwise wraps the expression in a list and returns it.
	 * 
	 * @param expression
	 *            the expression to be checked.
	 * @param parameter
	 *            a simple name to compare the expression to.
	 * @return a list of a single expression if the given expression is not
	 *         identical with the given simple name, or an empty list otherwise.
	 */
	private List<Expression> wrapNonIdentical(Expression expression, SimpleName parameter) {
		List<Expression> tailingMapExpressions = new ArrayList<>();
		if (ASTNode.SIMPLE_NAME != expression.getNodeType() || !((SimpleName) expression).getIdentifier()
			.equals(parameter.getIdentifier())) {
			tailingMapExpressions.add(expression);
		}

		return tailingMapExpressions;
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
			Expression mapParameter;

			if (ASTNode.EXPRESSION_METHOD_REFERENCE == mapExpression.getNodeType()) {
				mapParameter = mapExpression;
			} else {
				LambdaExpression mapLambdaExpression = ast.newLambdaExpression();
				mapLambdaExpression.setBody(mapExpression);
				mapLambdaExpression.setParentheses(false);
				lambdaRewrite = astRewrite.getListRewrite(mapLambdaExpression, LambdaExpression.PARAMETERS_PROPERTY);
				VariableDeclarationFragment mapDeclFragment = ast.newVariableDeclarationFragment();
				mapDeclFragment.setName((SimpleName) astRewrite.createCopyTarget(loopParameter.getName()));
				lambdaRewrite.insertFirst(mapDeclFragment, null);
				mapParameter = mapLambdaExpression;
			}

			ListRewrite tailingMapRewrite = astRewrite.getListRewrite(map, MethodInvocation.ARGUMENTS_PROPERTY);
			tailingMapRewrite.insertFirst(mapParameter, null);
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
