package at.splendit.simonykees.core.visitor;

import java.util.Collection;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.visitor.lambdaForEach.AbstractLambdaForEachASTVisitor;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.0.4
 */
public class FlatMapInsteadOfNestedLoopsASTVisitor extends AbstractLambdaForEachASTVisitor {

	private static final String FOR_EACH_METHOD_NAME = "forEach"; //$NON-NLS-1$
	private static final String STREAM_METHOD_NAME = "stream"; //$NON-NLS-1$
	private static final String FLAT_MAP_NAME = "flatMap"; //$NON-NLS-1$

	private enum MethodInvocationType {
		COLLECTION,
		STREAM,
	}

	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {
		if (FOR_EACH_METHOD_NAME.equals(methodInvocationNode.getName().getIdentifier())
				&& methodInvocationNode.arguments() != null && methodInvocationNode.arguments().size() == 1) {

			Expression methodArgumentExpression = (Expression) methodInvocationNode.arguments().get(0);
			if (methodArgumentExpression != null
					&& ASTNode.LAMBDA_EXPRESSION == methodArgumentExpression.getNodeType()) {
				LambdaExpression methodArgumentLambda = (LambdaExpression) methodArgumentExpression;

				MethodInvocation innerMethodInvocation = getSingleMethodInvocationFromLambda(methodArgumentLambda);

				if (innerMethodInvocation != null) {
					if (methodArgumentLambda != null && methodArgumentLambda.parameters() != null
							&& methodArgumentLambda.parameters().size() == 1) {
						LambdaExpression flatMapLambda = createFlatMapLambda(methodArgumentLambda);
						if (flatMapLambda != null) {
							Expression newOuterExpression = addStreamMethodInvocation(methodInvocationNode);
							if (newOuterExpression != null) {
								MethodInvocation newOuter = createNewOuterMethodInvocation(newOuterExpression,
										flatMapLambda);

								if (newOuter != null) {
									replaceMethodInvocation(methodInvocationNode, innerMethodInvocation,
											innerMethodInvocation.getExpression(), newOuter);
								}
							}
						}
					}
				}
			}
		}

		return true;
	}

	/**
	 * this method replaces the nested loop with a call to
	 * {@link Stream#flatMap}. it is executed recursively, in order to keep all
	 * existing stream operations. it replaces the {@link SimpleName}
	 * representing a {@link Collection} or a call to {@link# Collection#stream}
	 * from the inner loop with the whole {@link Stream} operations of the
	 * converted outer loop.
	 * 
	 * @param outer
	 *            The old outer loop, which has to be replaced
	 * @param inner
	 *            The inner loop already converted to a stream
	 * @param innerExpression
	 *            The expression of the inner loop
	 *            {@link Stream#forEach(java.util.function.Consumer)} method
	 *            invocation. This is the recursive parameter. If it is null,
	 *            the recursion will be aborted. If it is a call to
	 *            {@link Collection#stream()}, or a {@link SimpleName} the
	 *            changes will be written to the AST. In any other case, the
	 *            recursion takes place.
	 * @param newOuter
	 *            The newly created
	 *            {@link Stream#flatMap(java.util.function.Function)}
	 *            invocation, which will be used as the {@link Expression} of
	 *            the inner {@link Stream#forEach(java.util.function.Consumer)}
	 *            method invocation.
	 */
	private void replaceMethodInvocation(MethodInvocation outer, MethodInvocation inner, Expression innerExpression,
			MethodInvocation newOuter) {
		boolean transform = false;
		if (innerExpression != null) {
			if (ASTNode.METHOD_INVOCATION == innerExpression.getNodeType()) {
				MethodInvocation methodInvocationExpression = (MethodInvocation) innerExpression;
				if (STREAM_METHOD_NAME.equals(methodInvocationExpression.getName().getIdentifier())) {
					transform = true;
				} else {
					replaceMethodInvocation(outer, inner, methodInvocationExpression.getExpression(), newOuter);
				}
			} else if (ASTNode.SIMPLE_NAME == innerExpression.getNodeType()) {
				transform = true;
			}
		}

		if (transform) {
			astRewrite.replace(innerExpression, newOuter, null);
			MethodInvocation innerCopy = (MethodInvocation) astRewrite.createCopyTarget(inner);
			astRewrite.replace(outer, innerCopy, null);
		}
	}

	/**
	 * creates the call to {@link Stream#flatMap(java.util.function.Function)}
	 * 
	 * @param newOuterExpression
	 *            {@link Expression} of the new {@link MethodInvocation}
	 * @param flatMapLambda
	 *            {@link LambdaExpression} for the first and only argument of
	 *            the new {@link MethodInvocation}
	 * @return The newly created flatMap {@link MethodInvocation} or null, if
	 *         one of the parameters is empty.
	 */
	private MethodInvocation createNewOuterMethodInvocation(Expression newOuterExpression,
			LambdaExpression flatMapLambda) {
		if (newOuterExpression != null && flatMapLambda != null) {
			SimpleName flatMapName = astRewrite.getAST().newSimpleName(FLAT_MAP_NAME);
			MethodInvocation newOuter = astRewrite.getAST().newMethodInvocation();
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
	 * {@link Stream#flatMap(java.util.function.Function)}
	 * 
	 * @param outerLambda
	 *            the {@link LambdaExpression} of the outer
	 *            {@link Stream#forEach(java.util.function.Consumer)} method
	 *            invocation. The parameter of the new {@link LambdaExpression}
	 *            will be extracted from here.
	 * @return the newly created {@link LambdaExpression} for the flatMap method
	 *         or null if the method argument is null.
	 */
	private LambdaExpression createFlatMapLambda(LambdaExpression outerLambda) {
		if (outerLambda != null) {
			VariableDeclaration outerForEachlambdaParam = (VariableDeclaration) outerLambda.parameters().get(0);
			VariableDeclaration flatMapLambdaParamCopy = (VariableDeclaration) astRewrite
					.createCopyTarget(outerForEachlambdaParam);
			SimpleName flatMapLambdaParamNameCopy = (SimpleName) astRewrite
					.createCopyTarget(outerForEachlambdaParam.getName());

			SimpleName methodInvocationName = astRewrite.getAST().newSimpleName(STREAM_METHOD_NAME);

			MethodInvocation flatMapLambdaBody = astRewrite.getAST().newMethodInvocation();
			flatMapLambdaBody.setExpression(flatMapLambdaParamNameCopy);
			flatMapLambdaBody.setName(methodInvocationName);

			LambdaExpression flatMapLambda = astRewrite.getAST().newLambdaExpression();
			flatMapLambda.setBody(flatMapLambdaBody);
			ListRewrite flatMapLambdaListRewrite = astRewrite.getListRewrite(flatMapLambda,
					LambdaExpression.PARAMETERS_PROPERTY);
			flatMapLambdaListRewrite.insertFirst(flatMapLambdaParamCopy, null);

			return flatMapLambda;
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
			Expression expressionCopy = (Expression) astRewrite.createCopyTarget(expression);
			Expression newExpression = null;

			MethodInvocationType methodInvocationType = this.getMethodInvocationType(methodInvocation);
			if (MethodInvocationType.COLLECTION == methodInvocationType) {
				SimpleName methodInvocationName = astRewrite.getAST().newSimpleName(STREAM_METHOD_NAME);

				MethodInvocation streamMethodInvocation = astRewrite.getAST().newMethodInvocation();
				streamMethodInvocation.setExpression(expressionCopy);
				streamMethodInvocation.setName(methodInvocationName);

				newExpression = streamMethodInvocation;
			} else if (MethodInvocationType.STREAM == methodInvocationType) {
				newExpression = expressionCopy;
			}

			return newExpression;
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
	 * retrieves a single {@link MethodInvocation} from a
	 * {@link LambdaExpression}.
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
				if (lambdaBodyBlock.statements() != null && lambdaBodyBlock.statements().size() == 1) {
					Statement statement = (Statement) lambdaBodyBlock.statements().get(0);
					if (ASTNode.EXPRESSION_STATEMENT == statement.getNodeType()) {
						tempExpression = ((ExpressionStatement) statement).getExpression();
					}
				}
			} else { // Expression
				tempExpression = (Expression) lambdaBody;
			}

			if (tempExpression != null && ASTNode.METHOD_INVOCATION == tempExpression.getNodeType()) {
				MethodInvocation forEachMethodInvocatoin = (MethodInvocation) tempExpression;
				if (FOR_EACH_METHOD_NAME.equals(forEachMethodInvocatoin.getName().getIdentifier())) {
					methodInvocation = forEachMethodInvocatoin;
				}
			}
		}

		return methodInvocation;
	}

}
