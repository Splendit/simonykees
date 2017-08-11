package at.splendit.simonykees.core.visitor;

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

				this.transform(methodInvocationNode, methodArgumentLambda, innerMethodInvocation);
			}
		}

		return true;
	}

	private void transform(MethodInvocation outerMethodInvocation, LambdaExpression outerLambda,
			MethodInvocation innerMethodInvocation) {
		if (outerMethodInvocation == null || innerMethodInvocation == null) {
			return;
		}
		if (outerLambda == null || outerLambda.parameters() == null && outerLambda.parameters().size() == 1) {
			return;
		}

		Expression outerExpression = outerMethodInvocation.getExpression();
		Expression outerExpressionCopy = (Expression) astRewrite.createMoveTarget(outerExpression);
		Expression newOuterExpression = null;

		MethodInvocationType outerMethodInvocationType = this.getMethodInvocationType(outerMethodInvocation);
		if (MethodInvocationType.COLLECTION == outerMethodInvocationType) {
			SimpleName methodInvocationName = astRewrite.getAST().newSimpleName(STREAM_METHOD_NAME);

			MethodInvocation streamMethodInvocation = astRewrite.getAST().newMethodInvocation();
			streamMethodInvocation.setExpression(outerExpressionCopy);
			streamMethodInvocation.setName(methodInvocationName);

			newOuterExpression = streamMethodInvocation;
		} else if (MethodInvocationType.STREAM == outerMethodInvocationType) {
			newOuterExpression = outerExpressionCopy;
		} else {
			return;
		}

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
		//flatMapLambda.parameters().add(flatMapLambdaParamCopy);

		SimpleName flatMapName = astRewrite.getAST().newSimpleName(FLAT_MAP_NAME);

		MethodInvocation newOuter = astRewrite.getAST().newMethodInvocation();
		newOuter.setExpression(newOuterExpression);
		newOuter.setName(flatMapName);
		ListRewrite newOuterListRewrite = astRewrite.getListRewrite(newOuter, MethodInvocation.ARGUMENTS_PROPERTY);
		newOuterListRewrite.insertFirst(flatMapLambda, null);
//		newOuter.arguments().add(flatMapLambda);

		MethodInvocation newInner = (MethodInvocation) astRewrite.createCopyTarget(innerMethodInvocation);
//		MethodInvocation newInner = (MethodInvocation) ASTNode.copySubtree(astRewrite.getAST(), innerMethodInvocation);
		newInner.setExpression(newOuter);

		astRewrite.replace(outerMethodInvocation, flatMapLambda, null);
	}

	private MethodInvocationType getMethodInvocationType(MethodInvocation methodInvocation) {
		MethodInvocationType type = null;

		if (isCollectionForEachInvocation(methodInvocation)) {
			type = MethodInvocationType.COLLECTION;
		} else if (isStreamForEachInvocation(methodInvocation)) {
			type = MethodInvocationType.STREAM;
		}

		return type;
	}

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
