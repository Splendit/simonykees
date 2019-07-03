package eu.jsparrow.core.visitor.loop.stream;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.builder.NodeBuilder;
import eu.jsparrow.core.visitor.sub.FlowBreakersVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.util.OperatorUtil;

/**
 * 
 * @since 3.7.0
 *
 */
public class EnhancedForLoopToStreamTakeWhileASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {
		if (isConditionalExpression(enhancedForStatement.getExpression())) {
			return true;
		}

		SingleVariableDeclaration loopParameter = enhancedForStatement.getParameter();
		Type parameterType = loopParameter.getType();
		Expression loopExpression = enhancedForStatement.getExpression();
		ITypeBinding parameterTypeBinding = parameterType.resolveBinding();
		ITypeBinding expressionBinding = loopExpression.resolveTypeBinding();

		if (parameterTypeBinding == null || expressionBinding == null) {
			return true;
		}
		
		if(!ClassRelationUtil.isContentOfType(expressionBinding, java.util.Collection.class.getName()) &&
				!ClassRelationUtil.isInheritingContentOfTypes(expressionBinding, Collections.singletonList(java.util.Collection.class.getName()))) {
			return true;
		}

		if (!isTypeSafe(parameterTypeBinding) || !isTypeSafe(expressionBinding)) {
			return true;
		}

		Statement bodyStatement = enhancedForStatement.getBody();
		if (bodyStatement.getNodeType() != ASTNode.BLOCK) {
			return true;
		}

		Block body = (Block) bodyStatement;
		
		if(containsNonEffectivelyFinalVariable(body)) {
			return true;
		}
		
		List<Statement> bodyStatements = ASTNodeUtil.convertToTypedList(body.statements(), Statement.class);
		if (bodyStatements.size() <= 1) {
			return true;
		}

		Statement firstStatement = bodyStatements.get(0);
		if (!isIfStatementWithBreakBody(firstStatement)) {
			return true;
		}
		
		if(containsFlowBreakerStatements(bodyStatements.subList(1, bodyStatements.size()))) {
			return true;
		}
		
		IfStatement ifStatement = (IfStatement)firstStatement;
		ExpressionStatement streamStatement = createStreamStatement(loopParameter.getName(), loopExpression, ifStatement, body);
		astRewrite.replace(enhancedForStatement, streamStatement, null);
		//TODO: take care of comments.
		onRewrite();

		return true;
	}

	private boolean containsFlowBreakerStatements(List<Statement> statements) {
		for(Statement statement : statements) {
			FlowBreakersVisitor visitor = new FlowBreakersVisitor();
			statement.accept(visitor);
			if(visitor.hasFlowBreakerStatement()) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private ExpressionStatement createStreamStatement(SimpleName parameter, Expression loopExpression,
			IfStatement ifStatement, Block loopBody) {
		AST ast = astRewrite.getAST();
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName("stream")); //$NON-NLS-1$
		stream.setExpression((Expression) astRewrite.createCopyTarget(loopExpression));
		MethodInvocation takeWhile = ast.newMethodInvocation();
		takeWhile.setName(ast.newSimpleName("takeWhile")); //$NON-NLS-1$
		takeWhile.setExpression(stream);

		Expression negatedCondition = OperatorUtil.createNegatedExpression(ifStatement.getExpression(), astRewrite);
		LambdaExpression takeWhilePredicate = NodeBuilder.newLambdaExpression(ast, negatedCondition,
				parameter.getIdentifier());
		takeWhile.arguments()
			.add(takeWhilePredicate);

		MethodInvocation forEach = ast.newMethodInvocation();
		forEach.setExpression(takeWhile);
		forEach.setName(ast.newSimpleName("forEach")); //$NON-NLS-1$
		astRewrite.remove(ifStatement, null);
		Block newNode = (Block)ASTNode.copySubtree(ast, loopBody);
		newNode.statements().remove(0);
		LambdaExpression forEachBody = NodeBuilder.newLambdaExpression(ast, newNode, parameter.getIdentifier());
		forEach.arguments()
			.add(forEachBody);
		return ast.newExpressionStatement(forEach);
	}

	private boolean isIfStatementWithBreakBody(Statement statement) {
		if (statement.getNodeType() != ASTNode.IF_STATEMENT) {
			return false;
		}

		IfStatement ifStatement = (IfStatement) statement;
		Statement elseStatement = ifStatement.getElseStatement();
		if (elseStatement != null) {
			return false;
		}

		Statement thenStatement = ifStatement.getThenStatement();
		if (thenStatement.getNodeType() == ASTNode.BLOCK) {
			Block ifBody = (Block) thenStatement;
			List<Statement> ifBodyStatements = ASTNodeUtil.convertToTypedList(ifBody.statements(), Statement.class);
			if (ifBodyStatements.size() != 1) {
				return false;
			}
			Statement singleBodyStatement = ifBodyStatements.get(0);
			return singleBodyStatement.getNodeType() == ASTNode.BREAK_STATEMENT;
		}
		return thenStatement.getNodeType() == ASTNode.BREAK_STATEMENT;
	}

}
