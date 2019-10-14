package eu.jsparrow.core.visitor.optional;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * 
 * 
 * @since 3.10.0
 *
 */
public class OptionalIfPresentOrElseASTVisitor extends AbstractOptionalASTVisitor {

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName name = methodInvocation.getName();
		if (!IS_PRESENT.equals(name.getIdentifier())) {
			return true;
		}
		if (methodInvocation.getLocationInParent() != IfStatement.EXPRESSION_PROPERTY) {
			return true;
		}

		Expression expression = methodInvocation.getExpression();
		if (expression == null) {
			return true;
		}

		ITypeBinding expressionType = expression.resolveTypeBinding();
		boolean isOptional = ClassRelationUtil.isContentOfType(expressionType, OPTIONAL_FULLY_QUALIFIED_NAME);
		if (!isOptional) {
			return true;
		}

		IfStatement ifStatement = (IfStatement) methodInvocation.getParent();
		// analyze thenStatement
		Statement thenStatement = ifStatement.getThenStatement();

		boolean hasReturnStatement = containsFlowControlStatement(thenStatement);
		if (hasReturnStatement) {
			return true;
		}

		boolean hasUnhandledException = containsUnhandeledException(thenStatement);
		if (hasUnhandledException) {
			return true;
		}

		if (containsNonEffectivelyFinal(thenStatement)) {
			return true;
		}

		OptionalGetVisitor visitor = new OptionalGetVisitor(methodInvocation.getExpression());
		thenStatement.accept(visitor);
		List<MethodInvocation> getInvocations = visitor.getInvocations();
		List<MethodInvocation> nonDiscardedGetExpressions = getInvocations.stream()
			.filter(get -> ExpressionStatement.EXPRESSION_PROPERTY != get.getLocationInParent())
			.collect(Collectors.toList());
		if (nonDiscardedGetExpressions.isEmpty()) {
			return true;
		}

		// analyze else statement
		Statement elseStatement = ifStatement.getElseStatement();
		if (elseStatement.getNodeType() == ASTNode.IF_STATEMENT) {
			return true;
		}

		if (containsNonEffectivelyFinal(elseStatement)) {
			return true;
		}

		if (containsUnhandeledException(elseStatement)) {
			return true;
		}

		if (containsFlowControlStatement(elseStatement)) {
			return true;
		}

		String identifier = findParameterName(thenStatement, nonDiscardedGetExpressions);
		if (identifier.isEmpty()) {
			return true;
		}

		ExpressionStatement ifPresentOrElse = constructIfPresentOrElse(methodInvocation, thenStatement,
				nonDiscardedGetExpressions, elseStatement, identifier);
		astRewrite.replace(ifStatement, ifPresentOrElse, null);
		removedNodes.clear();
		return true;
	}

	@SuppressWarnings("unchecked")
	private ExpressionStatement constructIfPresentOrElse(MethodInvocation methodInvocation, Statement thenStatement,
			List<MethodInvocation> nonDiscardedGetExpressions, Statement elseStatement, String identifier) {
		IfPresentBodyFactoryVisitor ifPresentBodyFactoryVisitor = new IfPresentBodyFactoryVisitor(
				nonDiscardedGetExpressions, identifier, astRewrite);
		thenStatement.accept(ifPresentBodyFactoryVisitor);

		LambdaExpression lambda = NodeBuilder.newLambdaExpression(methodInvocation.getAST(),
				astRewrite.createCopyTarget(thenStatement), identifier);
		AST ast = methodInvocation.getAST();
		LambdaExpression orElseLambda = ast.newLambdaExpression();
		orElseLambda.setBody(astRewrite.createCopyTarget(elseStatement));

		MethodInvocation ifPresentOrElse = ast.newMethodInvocation();
		ifPresentOrElse.setExpression((Expression) astRewrite.createCopyTarget(methodInvocation.getExpression()));
		ifPresentOrElse.setName(ast.newSimpleName("ifPresentOrElse")); //$NON-NLS-1$
		ifPresentOrElse.arguments()
			.add(lambda);
		ifPresentOrElse.arguments()
			.add(orElseLambda);
		return ast.newExpressionStatement(ifPresentOrElse);
	}
}
