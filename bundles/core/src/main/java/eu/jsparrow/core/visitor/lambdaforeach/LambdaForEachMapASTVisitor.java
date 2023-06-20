package eu.jsparrow.core.visitor.lambdaforeach;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.LambdaForEachMapEvent;
import eu.jsparrow.core.visitor.sub.LambdaExpressionBodyAnalyzer;
import eu.jsparrow.core.visitor.utils.LambdaNodeUtil;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Extracts, if possible, a part of the body of the lambda expression occurring
 * as a consumer of a {@link Stream#forEach(Consumer)} and handles it by using a
 * {@link Stream#map(Function)} instead. For example, the following code:
 * 
 * <pre>
 * <code>
 * 		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
 *			int i = 10;
 *			String subString = s.substring(1) + i;
 * 			String lower = subString.toLowerCase();
 * 			sb.append(lower);
 * 		});
 * </code>
 * </pre>
 * 
 * will be transformed into
 * 
 * <pre>
 * <code> 
 * 		list.stream().filter(s -> !s.isEmpty()).map((s) -> {
 * 			int i = 10;
 * 			return s.substring(1) + i;
 * 		}).
 * 		forEach(subString -> {
 *			String lower = subString.toLowerCase();
 *			sb.append(lower);
 *		});
 * </code>
 * </pre>
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaForEachMapASTVisitor extends AbstractLambdaForEachASTVisitor implements LambdaForEachMapEvent {

	private List<Statement> replacedStatements = new ArrayList<>();

	@Override
	public void endVisit(CompilationUnit cu) {
		replacedStatements.clear();
		super.endVisit(cu);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		boolean toStreamNeeded = false;

		if (isCollectionForEachInvocation(methodInvocation)) {
			toStreamNeeded = true;
		} else if (!isStreamForEachInvocation(methodInvocation)) {
			return true;
		}

		if (hasRawExpression(methodInvocation)) {
			return true;
		}

		LambdaExpression lambdaExpressionAsOnlyArgument = ASTNodeUtil
			.findSingletonListElement(methodInvocation.arguments(), LambdaExpression.class)
			.orElse(null);
		if (lambdaExpressionAsOnlyArgument == null) {
			return true;
		}
		
		SimpleName parameter = extractSingleParameter(lambdaExpressionAsOnlyArgument);
		Block body = extractLambdaExpressionBlockBody(lambdaExpressionAsOnlyArgument);

		if (body == null) {
			return false;
		}

		/*
		 * use the analyzer for checking for extractable part in the forEach
		 */
		LambdaExpressionBodyAnalyzer analyzer = new LambdaExpressionBodyAnalyzer(parameter, body, astRewrite);

		if (!analyzer.foundExtractableMapStatement()) {
			return true;
		}

		if (isGeneratedNode(analyzer.getNewForEachParameterType())) {
			return true;
		}

		// get the extractable information from analyzer
		ASTNode extractableBlock = analyzer.getExtractableBlock();
		ASTNode remainingBlock = analyzer.getRemainingBlock();
		SimpleName newForEachParamName = analyzer.getNewForEachParameterName();

		this.replacedStatements.add(analyzer.getReplacedRemainingStatement());

		// introduce a Stream::map
		Expression streamExpression = methodInvocation.getExpression();
		AST ast = methodInvocation.getAST();
		MethodInvocation mapInvocation = ast.newMethodInvocation();
		mapInvocation.setName(ast.newSimpleName(analyzer.getMappingMethodName()));
		if (toStreamNeeded) {
			MethodInvocation streamInvocation = ast.newMethodInvocation();
			streamInvocation.setName(ast.newSimpleName(STREAM));
			streamInvocation.setExpression((Expression) astRewrite.createCopyTarget(streamExpression));
			mapInvocation.setExpression(streamInvocation);
		} else {
			mapInvocation.setExpression((Expression) astRewrite.createCopyTarget(streamExpression));
		}

		ListRewrite argumentsPropertyRewriter = astRewrite.getListRewrite(mapInvocation,
				MethodInvocation.ARGUMENTS_PROPERTY);
		LambdaExpression mapExpression = generateLambdaExpression(ast, extractableBlock, lambdaExpressionAsOnlyArgument);
		argumentsPropertyRewriter.insertFirst(mapExpression, null);

		/*
		 * replace the existing stream expression with the new one having the
		 * introduced map method in the tail
		 */
		astRewrite.replace(streamExpression, mapInvocation, null);

		// replace the body of the forEach with the new body
		astRewrite.replace(body, remainingBlock, null);

		/*
		 * replace the parameter of the forEach lambda expression
		 */
		astRewrite.replace(parameter, newForEachParamName, null);
		LambdaNodeUtil.saveComments(getCommentRewriter(), analyzer, findParentStatement(methodInvocation));
		addMarkerEvent(methodInvocation);
		onRewrite();

		/*
		 * Replace the type of the parameter if any
		 */
		Type type = LambdaNodeUtil.extractSingleParameterType(lambdaExpressionAsOnlyArgument);
		if (type != null) {
			Type newType = analyzer.getNewForEachParameterType();
			if (newType.isPrimitiveType()) {
				/*
				 * implicit boxing! primitives are not allowed in forEach
				 */
				astRewrite.replace((ASTNode) lambdaExpressionAsOnlyArgument.parameters()
					.get(0), newForEachParamName, null);
			} else {
				astRewrite.replace(type, newType, null);
				Modifier modifier = analyzer.getNewForEachParameterModifier();
				LambdaNodeUtil.insertModifier(lambdaExpressionAsOnlyArgument, modifier, astRewrite);
			}
		}

		return true;
	}

	private boolean hasRawExpression(MethodInvocation methodInvocation) {
		Expression expression = methodInvocation.getExpression();
		if (expression == null) {
			return false;
		}
		ITypeBinding expressionType = expression.resolveTypeBinding();

		return expressionType != null && expressionType.isRawType();
	}

	private Statement findParentStatement(MethodInvocation methodInvocation) {
		ExpressionStatement parent = ASTNodeUtil.getSpecificAncestor(methodInvocation, ExpressionStatement.class);
		while (parent != null && this.replacedStatements.contains(parent)) {
			parent = ASTNodeUtil.getSpecificAncestor(parent, ExpressionStatement.class);
		}
		return parent;
	}

	/**
	 * Creates a new lambda expression with the given parameter name and the
	 * body.
	 * 
	 * @param ast
	 *            the ast where the new lambda expression belongs to
	 * @param body
	 *            the body of the new lambda expression
	 * @return the generated {@link LambdaExpression}.
	 */
	private LambdaExpression generateLambdaExpression(AST ast, ASTNode body, LambdaExpression original) {
		/*
		 * A workaround for keeping the formatting of the original lambda
		 * expression.
		 */
		LambdaExpression lambdaExpression = (LambdaExpression) ASTNode.copySubtree(ast, original);
		lambdaExpression.setBody(body);
		return lambdaExpression;
	}

	/**
	 * Checks if the body of the lambda expression is a block, and extracts it.
	 * 
	 * @param lambdaExpression
	 *            lambda expression to check for.
	 * 
	 * @return the {@link Block} representing the body of the lambda expression,
	 *         or {@code null} if its is not a block.
	 */
	private Block extractLambdaExpressionBlockBody(LambdaExpression lambdaExpression) {
		ASTNode body = lambdaExpression.getBody();
		if (ASTNode.BLOCK == body.getNodeType()) {
			return (Block) body;
		}
		return null;
	}
}