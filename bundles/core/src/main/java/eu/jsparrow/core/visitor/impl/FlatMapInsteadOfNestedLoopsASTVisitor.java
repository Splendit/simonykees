package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.FlatMapInsteadOfNestedLoopsEvent;
import eu.jsparrow.core.rule.impl.LambdaForEachIfWrapperToFilterRule;
import eu.jsparrow.core.rule.impl.LambdaForEachMapRule;
import eu.jsparrow.core.visitor.lambdaforeach.AbstractLambdaForEachASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * This rule transforms a nested for loop to a
 * {@link Stream#flatMap(java.util.function.Function)} call. This is only done,
 * when the for loop has already been transformed to a call to
 * {@link Stream#forEach(java.util.function.Consumer)} or
 * {@link Collection#forEach(java.util.function.Consumer)}. No statement is
 * allowed between the outer and the inner loop. IF there is one, use for
 * example the {@link LambdaForEachIfWrapperToFilterRule} or
 * {@link LambdaForEachMapRule} to eliminate it. The depth of the nested loops
 * is not relevant.
 * 
 * @author Matthias Webhofer, Ardit Ymeri
 * @since 2.1.1
 */
public class FlatMapInsteadOfNestedLoopsASTVisitor extends AbstractLambdaForEachASTVisitor
		implements FlatMapInsteadOfNestedLoopsEvent {

	private int depthCount = 0;
	LinkedList<MethodInvocation> methodInvocationExpressionList = new LinkedList<>();
	LinkedList<Comment> forEachRelatedComments = new LinkedList<>();
	MethodInvocation innerMostMethodInvocation = null;
	private List<MethodInvocation> toBeSkipped = new ArrayList<>();

	/**
	 * work is only done for
	 * {@link Collection#forEach(java.util.function.Consumer)} or
	 * {@link Stream#forEach(java.util.function.Consumer)} method calls.
	 */
	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {
		if (!FOR_EACH.equals(methodInvocationNode.getName()
			.getIdentifier()) || methodInvocationNode.arguments()
				.size() != 1) {
			return toBeSkipped.isEmpty() || depthCount == 0;
		}

		Expression methodArgumentExpression = (Expression) methodInvocationNode.arguments()
			.get(0);
		if ((methodArgumentExpression == null || ASTNode.LAMBDA_EXPRESSION != methodArgumentExpression.getNodeType())) {
			toBeSkipped.add(methodInvocationNode);
			return depthCount == 0;
		}

		LambdaExpression methodArgumentLambda = (LambdaExpression) methodArgumentExpression;
		MethodInvocation innerMethodInvocation = getSingleMethodInvocationFromLambda(methodArgumentLambda);

		if (innerMethodInvocation == null || !isInnerLoopTransformable(innerMethodInvocation.getExpression())
				|| methodArgumentLambda.parameters() == null || methodArgumentLambda.parameters()
					.size() != 1) {
			toBeSkipped.add(methodInvocationNode);
			return depthCount == 0;
		}

		Expression leftMostExpression = ASTNodeUtil.getLeftMostExpressionOfMethodInvocation(innerMethodInvocation);
		if (leftMostExpression == null || ASTNode.SIMPLE_NAME != leftMostExpression.getNodeType()
				|| !checkParamUsage(methodArgumentLambda, innerMethodInvocation)) {
			toBeSkipped.add(methodInvocationNode);
			return depthCount == 0;
		}

		LambdaExpression flatMapLambda = createFlatMapLambda(methodArgumentLambda, (SimpleName) leftMostExpression);
		if (flatMapLambda == null) {
			toBeSkipped.add(methodInvocationNode);
			return depthCount == 0;
		}

		/*
		 * All of the transformation conditions are met
		 */

		depthCount++;
		if (depthCount <= 1) {
			Expression newOuterExpression = addStreamMethodInvocation(methodInvocationNode);
			if (newOuterExpression != null && ASTNode.METHOD_INVOCATION == newOuterExpression.getNodeType()) {
				methodInvocationExpressionList.add((MethodInvocation) newOuterExpression);
			}
		}

		MethodInvocation flatMapMethodInvocation = createFlatMapMethodInvocation(null, flatMapLambda);
		methodInvocationExpressionList.add(flatMapMethodInvocation);

		storeRelatedComments(methodArgumentLambda, innerMethodInvocation);
		MethodInvocation expression = createExpressionForInnerLoop(innerMethodInvocation.getExpression());

		if (expression != null) {
			methodInvocationExpressionList.add(expression);
		}

		innerMostMethodInvocation = innerMethodInvocation;

		return true;
	}

	protected void storeRelatedComments(LambdaExpression methodArgumentLambda, MethodInvocation innerMethodInvocation) {
		CommentRewriter helper = getCommentRewriter();
		ASTNode miParent = innerMethodInvocation.getParent();
		forEachRelatedComments.addAll(helper.findSurroundingComments(miParent));
		ASTNode miGParent = miParent.getParent();
		if (miGParent != null && ASTNode.BLOCK == miGParent.getNodeType()) {
			forEachRelatedComments.addAll(helper.findSurroundingComments(miGParent));
		}
		forEachRelatedComments.addAll(helper.findSurroundingComments(methodArgumentLambda));
	}

	/**
	 * this method evaluates if the given expression is transformable into a
	 * stream. This is done by recursively checking if the method type binding
	 * is of type {@link Stream}. The recursion ends either if a call to
	 * {@link Collection#stream()} is found or if the left {@link Expression} of
	 * the current {@link MethodInvocation} is NOT of type
	 * {@link MethodInvocation}
	 * 
	 * @param innerExpression
	 *            expression to check
	 * @return true, if the expression is transformable, false otherwise.
	 */
	private boolean isInnerLoopTransformable(Expression innerExpression) {
		if (innerExpression != null) {
			if (ASTNode.METHOD_INVOCATION == innerExpression.getNodeType()) {
				MethodInvocation methodInvocationExpression = (MethodInvocation) innerExpression;

				ITypeBinding methodInvocationExpressionType = methodInvocationExpression.resolveTypeBinding();
				List<String> streamTypeList = Collections.singletonList(JAVA_UTIL_STREAM_STREAM);

				if (!STREAM.equals(methodInvocationExpression.getName()
					.getIdentifier())) {
					if ((ClassRelationUtil.isContentOfTypes(methodInvocationExpressionType, streamTypeList)
							|| ClassRelationUtil
								.isInheritingContentOfTypes(methodInvocationExpressionType, streamTypeList))
							&& !STREAM.equals(methodInvocationExpression.getName()
								.getIdentifier())) {
						return this.isInnerLoopTransformable(methodInvocationExpression.getExpression());
					}
				} else {
					return true;
				}
			} else {
				ITypeBinding innerExpressionTypeBinding = innerExpression.resolveTypeBinding();
				List<String> collectionTypeList = Collections.singletonList(JAVA_UTIL_COLLECTION);

				if (ClassRelationUtil.isContentOfTypes(innerExpressionTypeBinding, collectionTypeList)
						|| ClassRelationUtil.isInheritingContentOfTypes(innerExpressionTypeBinding,
								collectionTypeList)) {
					return true;
				}
			}
		}

		return false;

	}

	/**
	 * creates the {@link Expression} for the {@link MethodInvocation} of the
	 * inner loop by recursively walking the {@link Expression}s of the given
	 * {@link MethodInvocation} until a call to {@link Collection#stream()} is
	 * reached and simultaneously creating a whole new {@link MethodInvocation}.
	 * 
	 * @param innerExpression
	 * @return
	 */
	private MethodInvocation createExpressionForInnerLoop(Expression innerExpression) {
		if (innerExpression != null && ASTNode.METHOD_INVOCATION == innerExpression.getNodeType()) {
			MethodInvocation methodInvocationExpression = (MethodInvocation) innerExpression;

			ITypeBinding methodInvocationExpressionType = methodInvocationExpression.resolveTypeBinding();
			List<String> streamTypeList = Collections.singletonList(JAVA_UTIL_STREAM_STREAM);

			if ((ClassRelationUtil.isContentOfTypes(methodInvocationExpressionType, streamTypeList)
					|| ClassRelationUtil.isInheritingContentOfTypes(methodInvocationExpressionType, streamTypeList))
					&& !STREAM.equals(methodInvocationExpression.getName()
						.getIdentifier())) {
				MethodInvocation methodInvocation = innerExpression.getAST()
					.newMethodInvocation();
				methodInvocation.setName(innerExpression.getAST()
					.newSimpleName(methodInvocationExpression.getName()
						.getIdentifier()));

				for (int i = 0; i < methodInvocationExpression.arguments()
					.size(); i++) {
					Expression arg = (Expression) methodInvocationExpression.arguments()
						.get(i);
					if (arg != null) {
						Expression argCopy = (Expression) astRewrite.createCopyTarget(arg);
						ListRewrite args = astRewrite.getListRewrite(methodInvocation,
								MethodInvocation.ARGUMENTS_PROPERTY);
						args.insertLast(argCopy, null);
					}
				}

				methodInvocation
					.setExpression(createExpressionForInnerLoop(methodInvocationExpression.getExpression()));

				return methodInvocation;
			}
		}

		return null;
	}

	/**
	 * creates a whole new {@link MethodInvocation} by reducing the
	 * {@link #methodInvocationExpressionList} with the help of
	 * {@link #joinMethodInvocations(MethodInvocation, MethodInvocation)}. This
	 * assembles all {@link Expression}s, which have been collected in the
	 * {@link #visit(org.eclipse.jdt.core.dom.MethodDeclaration)}, together with
	 * the {@link #innerMostMethodInvocation} to a new {@link MethodInvocation}
	 * which then replaces the old nested loop.
	 */
	@Override
	public void endVisit(MethodInvocation methodInvocationNode) {
		if (FOR_EACH.equals(methodInvocationNode.getName()
			.getIdentifier()) && !toBeSkipped.contains(methodInvocationNode)) {
			depthCount--;

			if (depthCount == 0 && innerMostMethodInvocation != null && innerMostMethodInvocation.arguments() != null
					&& innerMostMethodInvocation.arguments()
						.size() == 1
					&& !methodInvocationExpressionList.isEmpty()) {
				MethodInvocation newMethodInvocation = astRewrite.getAST()
					.newMethodInvocation();
				MethodInvocation expression = methodInvocationExpressionList.stream()
					.reduce(null, this::joinMethodInvocations);
				newMethodInvocation.setExpression(expression);
				newMethodInvocation.setName(astRewrite.getAST()
					.newSimpleName(innerMostMethodInvocation.getName()
						.getIdentifier()));

				ASTNode arg = ASTNode.copySubtree(astRewrite.getAST(),
						(Expression) innerMostMethodInvocation.arguments()
							.get(0));
				ListRewrite argsListRewrite = astRewrite.getListRewrite(newMethodInvocation,
						MethodInvocation.ARGUMENTS_PROPERTY);
				argsListRewrite.insertFirst(arg, null);

				astRewrite.replace(methodInvocationNode, newMethodInvocation, null);
				onRewrite();
				addMarkerEvent(methodInvocationNode);
				saveComments(methodInvocationNode);

				innerMostMethodInvocation = null;
				methodInvocationExpressionList.clear();
				forEachRelatedComments.clear();
			}
		}
		toBeSkipped.remove(methodInvocationNode);
	}

	private void saveComments(MethodInvocation methodInvocationNode) {
		CommentRewriter helper = getCommentRewriter();
		Statement statement = ASTNodeUtil.getSpecificAncestor(methodInvocationNode, Statement.class);
		List<Expression> args = ASTNodeUtil.convertToTypedList(innerMostMethodInvocation.arguments(), Expression.class);
		if (args.isEmpty()) {
			return;
		}
		List<Comment> comments = new ArrayList<>();
		comments.addAll(forEachRelatedComments);
		comments.addAll(helper.findRelatedComments(args.get(0)));
		helper.saveBeforeStatement(statement, comments);
	}

	/**
	 * helper method for
	 * {@link Stream#reduce(java.util.function.BinaryOperator)}, which assembles
	 * the elements of {@link #methodInvocationExpressionList} to a new
	 * {@link MethodInvocation}.
	 * 
	 * @param m1
	 *            left {@link MethodInvocation}, used as {@link Expression} for
	 *            m2
	 * @param m2
	 *            right {@link MethodInvocation}
	 * @return {@link MethodInvocation} m2, with m1 as new {@link Expression}
	 */
	private MethodInvocation joinMethodInvocations(MethodInvocation m1, MethodInvocation m2) {
		MethodInvocation current = m2;

		if (m1 != null) {
			boolean workToDo = true;
			while (workToDo) {
				Expression currentExpression = current.getExpression();

				if (currentExpression != null && ASTNode.METHOD_INVOCATION == currentExpression.getNodeType()) {
					current = (MethodInvocation) currentExpression;
					if (current.getExpression() == null) {
						workToDo = false;
					}
				} else {
					workToDo = false;
				}
			}
			current.setExpression(m1);
		}

		return m2;
	}

	/**
	 * This method checks, if the parameter of the {@link LambdaExpression}
	 * (parameter of {@link Stream#forEach(java.util.function.Consumer)} method
	 * call) is used within a statement, where it couldn't be used anymore after
	 * transformation.
	 * 
	 * @param lambdaExpression
	 *            the {@link LambdaExpression} of a
	 *            {@link Stream#forEach(java.util.function.Consumer)} method
	 *            call.
	 * @param methodInvocation
	 *            the whole inner
	 *            {@link Stream#forEach(java.util.function.Consumer)} method
	 *            invocation of the nested loop.
	 * @return true, if the transformation can take place, false otherwise.
	 */
	private boolean checkParamUsage(LambdaExpression lambdaExpression, MethodInvocation methodInvocation) {
		if (lambdaExpression != null && lambdaExpression.parameters()
			.size() == 1 && methodInvocation != null && methodInvocation.arguments()
				.size() == 1) {
			VariableDeclaration lambdaParam = (VariableDeclaration) lambdaExpression.parameters()
				.get(0);
			Expression methodArg = (Expression) methodInvocation.arguments()
				.get(0);
			LocalVariableUsagesVisitor localVariableVisitor = new LocalVariableUsagesVisitor(
					lambdaParam.getName());
			methodArg.accept(localVariableVisitor);
			List<SimpleName> usages = localVariableVisitor.getUsages();
			return usages.isEmpty();
		}

		return false;
	}

	/**
	 * creates the call to {@link Stream#flatMap(java.util.function.Function)}
	 * 
	 * @param newOuterExpression
	 *            {@link Expression} of the new {@link MethodInvocation} or
	 *            null, if there shouldn't be an {@link Expression} for the
	 *            flatMap() {@link MethodInvocation}.
	 * @param flatMapLambda
	 *            {@link LambdaExpression} for the first and only argument of
	 *            the new {@link MethodInvocation}
	 * @return The newly created flatMap {@link MethodInvocation} or null, if
	 *         the flatMapLamda parameter is null.
	 */
	private MethodInvocation createFlatMapMethodInvocation(Expression newOuterExpression,
			LambdaExpression flatMapLambda) {
		if (flatMapLambda != null) {
			SimpleName flatMapName = astRewrite.getAST()
				.newSimpleName(FLAT_MAP);
			MethodInvocation newOuter = astRewrite.getAST()
				.newMethodInvocation();
			newOuter.setExpression(newOuterExpression);
			newOuter.setName(flatMapName);
			ListRewrite newOuterListRewrite = astRewrite.getListRewrite(newOuter, MethodInvocation.ARGUMENTS_PROPERTY);
			newOuterListRewrite.insertFirst(flatMapLambda, null);

			return newOuter;
		}

		return null;
	}

	/**
	 * creates the {@link LambdaExpression}, which is used as the argument of
	 * {@link Stream#flatMap(java.util.function.Function)}. This is always a
	 * call to {@link Collection#stream()}.
	 * 
	 * @param outerLambda
	 *            the {@link LambdaExpression} of the outer
	 *            {@link Stream#forEach(java.util.function.Consumer)} method
	 *            invocation. The parameter of the new {@link LambdaExpression}
	 *            will be extracted from here.
	 * @return the newly created {@link LambdaExpression} for the flatMap method
	 *         or null if the method argument is null.
	 */
	private LambdaExpression createFlatMapLambda(LambdaExpression outerLambda, SimpleName leftMostInnerExpressionName) {
		if (outerLambda != null) {
			VariableDeclaration outerForEachlambdaParam = (VariableDeclaration) outerLambda.parameters()
				.get(0);
			if (outerForEachlambdaParam.getName()
				.getIdentifier()
				.equals(leftMostInnerExpressionName.getIdentifier())) {
				List<String> collectionTypeList = Collections.singletonList(JAVA_UTIL_COLLECTION);

				ITypeBinding outerParamTypeBinding = outerForEachlambdaParam.resolveBinding()
					.getType();
				if (ClassRelationUtil.isContentOfTypes(outerParamTypeBinding, collectionTypeList)
						|| ClassRelationUtil.isInheritingContentOfTypes(outerParamTypeBinding, collectionTypeList)) {

					VariableDeclaration flatMapLambdaParamCopy = (VariableDeclaration) astRewrite
						.createCopyTarget(outerForEachlambdaParam);
					SimpleName flatMapLambdaParamNameCopy = (SimpleName) astRewrite
						.createCopyTarget(outerForEachlambdaParam.getName());

					SimpleName methodInvocationName = astRewrite.getAST()
						.newSimpleName(STREAM);

					MethodInvocation flatMapLambdaBody = astRewrite.getAST()
						.newMethodInvocation();
					flatMapLambdaBody.setExpression(flatMapLambdaParamNameCopy);
					flatMapLambdaBody.setName(methodInvocationName);

					LambdaExpression flatMapLambda = astRewrite.getAST()
						.newLambdaExpression();
					flatMapLambda.setParentheses(false);
					flatMapLambda.setBody(flatMapLambdaBody);
					ListRewrite flatMapLambdaListRewrite = astRewrite.getListRewrite(flatMapLambda,
							LambdaExpression.PARAMETERS_PROPERTY);
					flatMapLambdaListRewrite.insertFirst(flatMapLambdaParamCopy, null);

					return flatMapLambda;
				}
			}
		}

		return null;
	}

	/**
	 * checks, if the given {@link MethodInvocation} is a {@link Collection} or
	 * {@link Stream}. If it is a {@link Collection}, a call to
	 * {@link Collection#stream()} will be added.
	 * 
	 * @param methodInvocation
	 *            {@link MethodInvocation} to check
	 * @return The given {@link MethodInvocation} with a call to
	 *         {@link Collection#stream()} added or null, if the given parameter
	 *         is null.
	 */
	private Expression addStreamMethodInvocation(MethodInvocation methodInvocation) {
		if (methodInvocation != null) {
			Expression expression = methodInvocation.getExpression();
			if (expression != null) {

				Expression newExpression = null;

				MethodInvocationType methodInvocationType = this.getMethodInvocationType(methodInvocation);
				if (MethodInvocationType.COLLECTION == methodInvocationType) {
					Expression collectionExpressionCopy = (Expression) astRewrite.createCopyTarget(expression);
					SimpleName methodInvocationName = astRewrite.getAST()
						.newSimpleName(STREAM);

					MethodInvocation streamMethodInvocation = astRewrite.getAST()
						.newMethodInvocation();
					streamMethodInvocation.setExpression(collectionExpressionCopy);
					streamMethodInvocation.setName(methodInvocationName);

					newExpression = streamMethodInvocation;
				} else if (MethodInvocationType.STREAM == methodInvocationType) {
					Expression expressionCopy = (Expression) astRewrite.createCopyTarget(expression);
					newExpression = expressionCopy;
				}

				return newExpression;
			}
		}

		return null;
	}

	/**
	 * evaluates the type of the given {@link MethodInvocation}
	 * 
	 * @param methodInvocation
	 * @return {@link MethodInvocationType#COLLECTION},
	 *         {@link MethodInvocationType#STREAM} or null, if it is neither a
	 *         {@link Collection} nor a {@link Stream}
	 */
	private MethodInvocationType getMethodInvocationType(MethodInvocation methodInvocation) {
		MethodInvocationType type = null;

		if (isCollectionForEachInvocation(methodInvocation)) {
			type = MethodInvocationType.COLLECTION;
		} else if (isStreamForEachInvocation(methodInvocation)) {
			type = MethodInvocationType.STREAM;
		}

		return type;
	}

	/**
	 * retrieves a single {@link Stream#forEach(java.util.function.Consumer)}
	 * {@link MethodInvocation} from a {@link LambdaExpression}.
	 * 
	 * @param lambdaExpression
	 * @return the only {@link MethodInvocation} present in the
	 *         {@link LambdaExpression} or null if there are more
	 *         {@link MethodInvocation}s or in case of another error.
	 */
	private MethodInvocation getSingleMethodInvocationFromLambda(LambdaExpression lambdaExpression) {
		MethodInvocation methodInvocation = null;
		ASTNode lambdaBody = lambdaExpression.getBody();

		Expression tempExpression = null;
		if (lambdaBody != null) {
			if (ASTNode.BLOCK == lambdaBody.getNodeType()) {
				Block lambdaBodyBlock = (Block) lambdaBody;
				if (lambdaBodyBlock.statements() != null && lambdaBodyBlock.statements()
					.size() == 1) {
					Statement statement = (Statement) lambdaBodyBlock.statements()
						.get(0);
					if (ASTNode.EXPRESSION_STATEMENT == statement.getNodeType()) {
						tempExpression = ((ExpressionStatement) statement).getExpression();
					}
				}
			} else { // Expression
				tempExpression = (Expression) lambdaBody;
			}

			if (tempExpression != null && ASTNode.METHOD_INVOCATION == tempExpression.getNodeType()) {
				MethodInvocation forEachMethodInvocatoin = (MethodInvocation) tempExpression;
				if (FOR_EACH.equals(forEachMethodInvocatoin.getName()
					.getIdentifier())) {
					methodInvocation = forEachMethodInvocatoin;
				}
			}
		}

		return methodInvocation;
	}

	private enum MethodInvocationType {
		COLLECTION,
		STREAM,
	}
}
