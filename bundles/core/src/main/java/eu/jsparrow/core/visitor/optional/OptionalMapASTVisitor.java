package eu.jsparrow.core.visitor.optional;

import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.markers.common.OptionalMapEvent;
import eu.jsparrow.core.visitor.sub.LambdaExpressionBodyAnalyzer;
import eu.jsparrow.core.visitor.utils.LambdaNodeUtil;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * 
 * Extracts an {@code Optional::map} from the body of the consumer used in
 * {@code Optional::ifPresent}. This makes complicated code blocks easier to
 * read and reuse. Example:
 * 
 * <pre>
 * optional.ifPresent(value -> {
 * 	String test = value.replace("t", "o");
 * 	System.out.print(test);
 * });
 * </pre>
 * 
 * is transformed to:
 * 
 * <pre>
 * optional.map(value -> value.replace("t", "o"))
 * 	.ifPresent(test -> System.out.print(test));
 * </pre>
 * 
 * 
 * @since 3.13.0
 *
 */
public class OptionalMapASTVisitor extends AbstractOptionalASTVisitor implements OptionalMapEvent {

	@Override
	public boolean visit(LambdaExpression lambdaExpression) {

		/*
		 * Body should be a block with more than one statement.
		 */
		ASTNode lambdaBody = lambdaExpression.getBody();
		if (lambdaBody.getNodeType() != ASTNode.BLOCK) {
			return true;
		}
		Block block = (Block) lambdaBody;
		if (block.statements()
			.size() < 2) {
			return false;
		}

		/*
		 * Parent should be the method invocation ifPresent.
		 */
		if (MethodInvocation.ARGUMENTS_PROPERTY != lambdaExpression.getLocationInParent()) {
			return true;
		}

		MethodInvocation methodInvocation = (MethodInvocation) lambdaExpression.getParent();
		if (methodInvocation.getExpression() == null) {
			return false;
		}

		boolean isOptionalIfPresent = hasRightTypeAndName(methodInvocation, java.util.Optional.class.getName(),
				IF_PRESENT);
		if (!isOptionalIfPresent) {
			return true;
		}

		/*
		 * Check the requirements for extracting a variable.
		 */

		SimpleName parameter = findParameterName(lambdaExpression).orElse(null);
		if (parameter == null) {
			return false;
		}

		LambdaExpressionBodyAnalyzer analyzer = new LambdaExpressionBodyAnalyzer(parameter, block, astRewrite);
		if (!analyzer.foundExtractableMapStatement()) {
			return true;
		}

		if (isGeneratedNode(analyzer.getNewForEachParameterType())) {
			return true;
		}

		// Introduce optional map
		replace(lambdaExpression, methodInvocation, parameter, analyzer);

		return true;
	}

	@SuppressWarnings("unchecked")
	private void replace(LambdaExpression lambdaExpression, MethodInvocation methodInvocation, SimpleName parameter,
			LambdaExpressionBodyAnalyzer analyzer) {
		ASTNode lambdaBody = lambdaExpression.getBody();
		ASTNode extractableBlock = analyzer.getExtractableBlock();
		ASTNode remainingBlock = analyzer.getRemainingBlock();
		SimpleName newParameterName = analyzer.getNewForEachParameterName();
		Expression optionalExpression = methodInvocation.getExpression();
		AST ast = methodInvocation.getAST();
		MethodInvocation mapInvocation = ast.newMethodInvocation();
		mapInvocation.setName(ast.newSimpleName("map")); //$NON-NLS-1$
		mapInvocation.setExpression((Expression) astRewrite.createCopyTarget(optionalExpression));

		LambdaExpression mapExpression = (LambdaExpression) ASTNode.copySubtree(ast, lambdaExpression);
		mapExpression.setBody(extractableBlock);
		mapInvocation.arguments()
			.add(mapExpression);
		astRewrite.replace(optionalExpression, mapInvocation, null);
		astRewrite.replace(lambdaBody, remainingBlock, null);
		astRewrite.replace(parameter, newParameterName, null);
		onRewrite();
		addMarkerEvent(lambdaExpression);

		LambdaNodeUtil.saveComments(getCommentRewriter(), analyzer,
				ASTNodeUtil.getSpecificAncestor(lambdaExpression, Statement.class));

		Type parameterType = LambdaNodeUtil.extractSingleParameterType(lambdaExpression)
			.orElse(null);
		if (parameterType == null) {
			return;
		}

		Type newType = analyzer.getNewForEachParameterType();
		if (newType.isPrimitiveType()) {
			astRewrite.replace((ASTNode) lambdaExpression.parameters()
				.get(0), newParameterName, null);
		} else {
			astRewrite.replace(parameterType, newType, null);
			Modifier modifier = analyzer.getNewForEachParameterModifier();
			LambdaNodeUtil.insertModifier(lambdaExpression, modifier, astRewrite);
		}
	}

	public static Optional<SimpleName> findParameterName(LambdaExpression lambdaExpression) {
		Optional<VariableDeclarationFragment> fragmentAsOnlyParameter = ASTNodeUtil
			.findSingletonListElement(lambdaExpression.parameters(), VariableDeclarationFragment.class);
		if (fragmentAsOnlyParameter.isPresent()) {
			return fragmentAsOnlyParameter.map(VariableDeclarationFragment::getName);
		}

		return ASTNodeUtil
			.findSingletonListElement(lambdaExpression.parameters(), SingleVariableDeclaration.class)
			.map(SingleVariableDeclaration::getName);
	}
}
