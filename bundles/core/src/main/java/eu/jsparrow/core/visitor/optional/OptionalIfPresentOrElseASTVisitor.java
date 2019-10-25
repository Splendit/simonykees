package eu.jsparrow.core.visitor.optional;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
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

		Expression expression = methodInvocation.getExpression();
		if (expression == null) {
			return true;
		}

		ITypeBinding expressionType = expression.resolveTypeBinding();
		boolean isOptional = ClassRelationUtil.isContentOfType(expressionType, OPTIONAL_FULLY_QUALIFIED_NAME);
		if (!isOptional) {
			return true;
		}

		if (methodInvocation.getLocationInParent() != IfStatement.EXPRESSION_PROPERTY) {
			return true;
		}
		IfStatement ifStatement = (IfStatement) methodInvocation.getParent();

		// analyze thenStatement
		Statement thenStatement = ifStatement.getThenStatement();
		if (!isConvertibleToLambdaBody(thenStatement)) {
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
		if (elseStatement.getNodeType() != ASTNode.BLOCK
				&& elseStatement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return true;
		}

		if (!isConvertibleToLambdaBody(elseStatement)) {
			return true;
		}

		String identifier = findParameterName(thenStatement, nonDiscardedGetExpressions);
		if (identifier.isEmpty()) {
			return true;
		}

		ASTNode elseBody = unwrapOrElseBody(elseStatement);
		ExpressionStatement ifPresentOrElse = constructIfPresentOrElse(methodInvocation, thenStatement,
				nonDiscardedGetExpressions, elseBody, identifier);
		astRewrite.replace(ifStatement, ifPresentOrElse, null);
		removedNodes.clear();
		return true;
	}

	private ASTNode unwrapOrElseBody(Statement elseStatement) {
		if (elseStatement.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block) elseStatement;
			List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
			if (statements.size() == 1) {
				Statement statement = statements.get(0);
				if (statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
					ExpressionStatement expressionStatement = (ExpressionStatement) statement;
					return expressionStatement.getExpression();
				}
			}
		} else if (elseStatement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
			ExpressionStatement expressionStatement = (ExpressionStatement) elseStatement;
			return expressionStatement.getExpression();
		}
		return elseStatement;
	}

	private boolean isConvertibleToLambdaBody(Statement thenStatement) {
		boolean hasReturnStatement = containsFlowControlStatement(thenStatement);
		if (hasReturnStatement) {
			return false;
		}

		boolean hasUnhandledException = containsUnhandledException(thenStatement);
		if (hasUnhandledException) {
			return false;
		}

		return !containsNonEffectivelyFinal(thenStatement);
	}

	@SuppressWarnings("unchecked")
	private ExpressionStatement constructIfPresentOrElse(MethodInvocation methodInvocation, Statement thenStatement,
			List<MethodInvocation> nonDiscardedGetExpressions, ASTNode elseStatement, String identifier) {
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
