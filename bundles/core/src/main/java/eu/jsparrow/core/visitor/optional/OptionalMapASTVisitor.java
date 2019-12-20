package eu.jsparrow.core.visitor.optional;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.lambdaforeach.ForEachBodyAnalyzer;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class OptionalMapASTVisitor extends AbstractOptionalASTVisitor {

	@SuppressWarnings("unchecked")
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
			return true;
		}

		/*
		 * Parent should be the method invocation ifPresent.
		 */
		if (MethodInvocation.ARGUMENTS_PROPERTY != lambdaExpression.getLocationInParent()) {
			return true;
		}

		MethodInvocation methodInvocation = (MethodInvocation) lambdaExpression.getParent();
		boolean isOptionalIfPresent = hasRightTypeAndName(methodInvocation, java.util.Optional.class.getName(),
				IF_PRESENT);
		if (!isOptionalIfPresent) {
			return true;
		}

		/*
		 * 3. Check the requirements for extracting a variable. See Use
		 * Stream::map.
		 */

		SimpleName parameter = findParameterName(lambdaExpression);
		if(parameter == null) {
			return false;
		}
		
		ForEachBodyAnalyzer analyzer = new ForEachBodyAnalyzer(parameter, block, astRewrite);
		if (!analyzer.foundExtractableMapStatement()) {
			return true;
		}

		if (isGeneratedNode(analyzer.getNewForEachParameterType())) {
			return true;
		}

		ASTNode extractableBlock = analyzer.getExtractableBlock();
		ASTNode remainingBlock = analyzer.getRemainingBlock();
		SimpleName newParameterName = analyzer.getNewForEachParameterName();

		// Introduce optional map
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

		Type parameterType = extractSingleParameterType(lambdaExpression);
		if (parameterType == null) {
			return true;
		}

		Type newType = analyzer.getNewForEachParameterType();
		if (newType.isPrimitiveType()) {
			/*
			 * implicit boxing! primitives are not allowed in forEach
			 */
			astRewrite.replace((ASTNode) lambdaExpression.parameters()
				.get(0), newParameterName, null);
		} else {
			astRewrite.replace(parameterType, newType, null);
			Modifier modifier = analyzer.getNewForEachParameterModifier();
			insertModifier(lambdaExpression, modifier);
		}

		return true;
	}

	private SimpleName findParameterName(LambdaExpression lambdaExpression) {
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(lambdaExpression.parameters(),
				VariableDeclarationFragment.class);
		if (fragments.size() == 1) {
			return fragments.get(0)
				.getName();
		}

		List<SingleVariableDeclaration> declarations = ASTNodeUtil.returnTypedList(lambdaExpression.parameters(),
				SingleVariableDeclaration.class);

		if (declarations.size() == 1) {
			return declarations.get(0).getName();
		}
		
		return null;
	}

	/**
	 * Extracts the {@link Type} of the parameter of the lambda expression, if
	 * any.
	 * 
	 * @param lambdaExpression
	 *            lambda expression to be checked
	 * 
	 * @return the {@link Type} of the parameter if the lambda expression has
	 *         only one parameter expressed as a
	 *         {@link SingleVariableDeclaration}, or {@code null} otherwise.
	 */
	private Type extractSingleParameterType(LambdaExpression lambdaExpression) {
		Type parameter = null;

		List<SingleVariableDeclaration> declarations = ASTNodeUtil.returnTypedList(lambdaExpression.parameters(),
				SingleVariableDeclaration.class);
		if (declarations.size() == 1) {
			SingleVariableDeclaration declaration = declarations.get(0);
			parameter = declaration.getType();
		}

		return parameter;
	}

	/**
	 * Inserts the modifier to the parameter of the lambda expression if it has
	 * only one parameter represented with a {@link SingleVariableDeclaration}.
	 * 
	 * @param lambdaExpression
	 *            a node representing a lambda expression
	 * @param modifier
	 *            the modifier to be inserted
	 */
	private void insertModifier(LambdaExpression lambdaExpression, Modifier modifier) {
		if (modifier != null) {
			List<SingleVariableDeclaration> params = ASTNodeUtil.convertToTypedList(lambdaExpression.parameters(),
					SingleVariableDeclaration.class);
			if (params.size() == 1) {
				SingleVariableDeclaration param = params.get(0);
				ListRewrite paramRewriter = astRewrite.getListRewrite(param,
						SingleVariableDeclaration.MODIFIERS2_PROPERTY);
				paramRewriter.insertFirst(astRewrite.createCopyTarget(modifier), null);
			}
		}
	}

}
