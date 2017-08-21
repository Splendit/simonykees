package at.splendit.simonykees.core.visitor;

import java.util.Collection;
import java.util.LinkedList;
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

	private int depthCount = 0;
	LinkedList<MethodInvocation> methodInvocationExpressionList = new LinkedList<>();
	MethodInvocation innerMostMethodInvocation = null;

	private enum MethodInvocationType {
		COLLECTION,
		STREAM,
	}

	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {
		if (FOR_EACH_METHOD_NAME.equals(methodInvocationNode.getName().getIdentifier())
				&& methodInvocationNode.arguments() != null && methodInvocationNode.arguments().size() == 1) {

			depthCount++;

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

							boolean addFlatMap = true;
							if (depthCount <= 1) {
								Expression newOuterExpression = addStreamMethodInvocation(methodInvocationNode);
								if (newOuterExpression != null) {
									MethodInvocation newOuter = createFlatMapMethodInvocation(newOuterExpression,
											flatMapLambda);
									if (newOuter != null) {
										methodInvocationExpressionList.add(newOuter);
										addFlatMap = false;
									}
								}
							}

							MethodInvocation expression = createExpressionForInnerLoop(
									innerMethodInvocation.getExpression());

							if (expression != null) {
								if (addFlatMap) {
									MethodInvocation flatMapMethodInvocation = createFlatMapMethodInvocation(null,
											flatMapLambda);
									methodInvocationExpressionList.add(flatMapMethodInvocation);
								}
								methodInvocationExpressionList.add(expression);

							}

							innerMostMethodInvocation = innerMethodInvocation;

						}
					}
				}
			}
		}

		return true;

	}

	private MethodInvocation createExpressionForInnerLoop(Expression innerExpression) {
		if (innerExpression != null) {

			if (ASTNode.METHOD_INVOCATION == innerExpression.getNodeType()) {
				MethodInvocation methodInvocationExpression = (MethodInvocation) innerExpression;
				if (!STREAM_METHOD_NAME.equals(methodInvocationExpression.getName().getIdentifier())) {
					MethodInvocation methodInvocation = innerExpression.getAST().newMethodInvocation();
					methodInvocation.setName(innerExpression.getAST()
							.newSimpleName(methodInvocationExpression.getName().getIdentifier()));
					Expression arg = (Expression) astRewrite
							.createCopyTarget((Expression) methodInvocationExpression.arguments().get(0));
					ListRewrite args = astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);
					args.insertFirst(arg, null);
					methodInvocation
							.setExpression(createExpressionForInnerLoop(methodInvocationExpression.getExpression()));
					return methodInvocation;
				}
			}
		}

		return null;
	}

	@Override
	public void endVisit(MethodInvocation methodInvocationNode) {
		if (FOR_EACH_METHOD_NAME.equals(methodInvocationNode.getName().getIdentifier())) {
			depthCount--;

			if (depthCount == 0 && innerMostMethodInvocation != null) {
				MethodInvocation newMethodInvocation = astRewrite.getAST().newMethodInvocation();
				MethodInvocation expression = methodInvocationExpressionList.stream().reduce(null,
						this::joinMethodInvocations);
				newMethodInvocation.setExpression(expression);
				newMethodInvocation.setName(
						astRewrite.getAST().newSimpleName(innerMostMethodInvocation.getName().getIdentifier()));
				ASTNode arg = ASTNode.copySubtree(astRewrite.getAST(),
						(Expression) innerMostMethodInvocation.arguments().get(0));
				ListRewrite argsListRewrite = astRewrite.getListRewrite(newMethodInvocation,
						MethodInvocation.ARGUMENTS_PROPERTY);
				argsListRewrite.insertFirst(arg, null);

				astRewrite.replace(methodInvocationNode, newMethodInvocation, null);

				innerMostMethodInvocation = null;
				methodInvocationExpressionList.clear();
			}
		}
	}

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
	// private MethodInvocation createNewOuterMethodInvocation(Expression
	// newOuterExpression,
	// LambdaExpression flatMapLambda) {
	private MethodInvocation createFlatMapMethodInvocation(Expression newOuterExpression,
			LambdaExpression flatMapLambda) {
		// if (newOuterExpression != null && flatMapLambda != null) {
		if (flatMapLambda != null) {
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
			if (expression != null) {

				Expression newExpression = null;

				MethodInvocationType methodInvocationType = this.getMethodInvocationType(methodInvocation);
				if (MethodInvocationType.COLLECTION == methodInvocationType) {
					if (ASTNode.SIMPLE_NAME == expression.getNodeType()) {
						SimpleName collectionSimpleName = astRewrite.getAST()
								.newSimpleName(((SimpleName) expression).getIdentifier());
						SimpleName methodInvocationName = astRewrite.getAST().newSimpleName(STREAM_METHOD_NAME);

						MethodInvocation streamMethodInvocation = astRewrite.getAST().newMethodInvocation();
						streamMethodInvocation.setExpression(collectionSimpleName);
						streamMethodInvocation.setName(methodInvocationName);

						newExpression = streamMethodInvocation;
					}

					// Expression expressionCopy = (Expression)
					// astRewrite.createCopyTarget(expression);
					// SimpleName methodInvocationName =
					// astRewrite.getAST().newSimpleName(STREAM_METHOD_NAME);
					//
					// MethodInvocation streamMethodInvocation =
					// astRewrite.getAST().newMethodInvocation();
					// streamMethodInvocation.setExpression(expressionCopy);
					// streamMethodInvocation.setName(methodInvocationName);
					//
					// newExpression = streamMethodInvocation;
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
