package at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class EnhancedForLoopToStreamFindFirstASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {
	
	private static final String FIND_FIRST = "findFirst"; //$NON-NLS-1$
	private static final String OR_ELSE = "orElse"; //$NON-NLS-1$
	
	@Override
	public boolean visit(EnhancedForStatement forLoop) {
		
		Expression loopExpression = forLoop.getExpression();
		SingleVariableDeclaration loopParameter = forLoop.getParameter();
		
		IfStatement ifStatement = isConvertableInterruptedLoop(forLoop, loopExpression, loopParameter);	
		if(ifStatement == null) {
			return true;
		}
		
		Expression ifCondition = ifStatement.getExpression();
		Statement thenStatement = ifStatement.getThenStatement();
		VariableDeclarationFragment varDeclFragment;
		ReturnStatement returnStatement;
		
		if ((varDeclFragment = isAssignmentAndBreakBlock(thenStatement, forLoop, loopParameter.getName())) != null) {
			/*
			 * replace initialization of the boolean variable with
			 * Stream::anyMatch
			 */
			Type type = ((VariableDeclarationStatement)varDeclFragment.getParent()).getType();
			MethodInvocation methodInvocation = createStreamFindFirstInitalizer(loopExpression, ifCondition,
					loopParameter, varDeclFragment.getInitializer());
			astRewrite.replace(varDeclFragment.getInitializer(), methodInvocation, null);
			replaceLoopWithFragment(forLoop, varDeclFragment, (Type)astRewrite.createCopyTarget(type));

		} else if ((returnStatement = isReturnBlock(thenStatement, forLoop, loopParameter.getName())) != null) {
			// replace the return statement with a Stream::AnyMatch
			MethodInvocation methodInvocation = createStreamFindFirstInitalizer(loopExpression, ifCondition,
					loopParameter, returnStatement.getExpression());
			astRewrite.replace(returnStatement.getExpression(), methodInvocation, null);
			astRewrite.remove(forLoop, null);
		}
		
		return true;
	}

	private VariableDeclarationFragment isAssignmentAndBreakBlock(Statement thenStatement,
			EnhancedForStatement forLoop, SimpleName loopParameterName) {
		Assignment assignmentAfterBreak = super.findAssignmentAfterBreakExpression(thenStatement);
		if(assignmentAfterBreak != null) {
			Expression rhs = assignmentAfterBreak.getRightHandSide();
			if(ASTNode.SIMPLE_NAME == rhs.getNodeType() && ((SimpleName)rhs).getIdentifier().equals(loopParameterName.getIdentifier())) {
				Expression lhs = assignmentAfterBreak.getLeftHandSide();
				if(ASTNode.SIMPLE_NAME == lhs.getNodeType()) {
					return findDeclarationFragment(forLoop, (SimpleName)lhs);
				}
			}
		}
		return null;
	}

	private VariableDeclarationFragment findDeclarationFragment(EnhancedForStatement forLoop, SimpleName varName) {
		ASTNode loopParent = forLoop.getParent();
		if (ASTNode.BLOCK == loopParent.getNodeType()) {
			Block parentBlock = (Block) loopParent;
			LoopWithBreakStatementVisitor analyzer = new LoopWithBreakStatementVisitor(parentBlock,
					forLoop, varName);
			parentBlock.accept(analyzer);
			VariableDeclarationFragment declFragment = analyzer.getDeclarationBoolFragment();
			if(declFragment != null && declFragment.getInitializer() != null) {
				return declFragment;
			}
		}
		return null;
	}

	private ReturnStatement isReturnBlock(Statement thenStatement, EnhancedForStatement forLoop, SimpleName parameter) {
		ReturnStatement returnStatement = isReturnBlock(thenStatement);
		if(returnStatement != null) {
			Expression returnedExpression = returnStatement.getExpression();
			if(returnedExpression != null && ASTNode.SIMPLE_NAME == returnedExpression.getNodeType() 
					&& ((SimpleName)returnedExpression).getIdentifier().equals(parameter.getIdentifier())) {
				ReturnStatement followingReturnStatement = findFollowingReturnStatement(forLoop);
				if(followingReturnStatement != null && followingReturnStatement.getExpression() != null) {
					return followingReturnStatement;
				}
			}
		}
		return null;
	}

	private MethodInvocation createStreamFindFirstInitalizer(Expression loopExpression, Expression ifCondition,
			SingleVariableDeclaration loopParameter, Expression orElseExpression) {
		AST ast = astRewrite.getAST();
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName(STREAM));
		stream.setExpression(createExpressionForStreamMethodInvocation(loopExpression));

		MethodInvocation filter = ast.newMethodInvocation();
		filter.setName(ast.newSimpleName(FILTER));
		filter.setExpression(stream);

		LambdaExpression findFirstCondition = ast.newLambdaExpression();
		findFirstCondition.setBody(astRewrite.createMoveTarget(ifCondition));
		ListRewrite lambdaRewrite = astRewrite.getListRewrite(findFirstCondition, LambdaExpression.PARAMETERS_PROPERTY);
		findFirstCondition.setParentheses(false);
		VariableDeclarationFragment newDeclFragment = ast.newVariableDeclarationFragment();
		newDeclFragment.setName((SimpleName) astRewrite.createCopyTarget(loopParameter.getName()));
		lambdaRewrite.insertFirst(newDeclFragment, null);

		ListRewrite anyMatchParamRewrite = astRewrite.getListRewrite(filter, MethodInvocation.ARGUMENTS_PROPERTY);
		anyMatchParamRewrite.insertFirst(findFirstCondition, null);
		
		MethodInvocation findFirst = ast.newMethodInvocation();
		findFirst.setName(ast.newSimpleName(FIND_FIRST));
		findFirst.setExpression(filter);
		
		MethodInvocation orElse = ast.newMethodInvocation();
		orElse.setName(ast.newSimpleName(OR_ELSE));
		orElse.setExpression(findFirst);
		
		ListRewrite orElseParamRewrite = astRewrite.getListRewrite(orElse, MethodInvocation.ARGUMENTS_PROPERTY);
		orElseParamRewrite.insertFirst(orElseExpression, null);
		
		return orElse;
	}
}
