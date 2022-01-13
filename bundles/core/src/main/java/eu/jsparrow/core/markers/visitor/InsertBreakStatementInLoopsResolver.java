package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.InsertBreakStatementInLoopsRule;
import eu.jsparrow.core.visitor.impl.InsertBreakStatementInLoopsASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link InsertBreakStatementInLoopsASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public class InsertBreakStatementInLoopsResolver extends InsertBreakStatementInLoopsASTVisitor implements Resolver {

	public static final String ID = "InsertBreakStatementInLoopsResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public InsertBreakStatementInLoopsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(InsertBreakStatementInLoopsRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {
		if (positionChecker.test(enhancedForStatement)) {
			super.visit(enhancedForStatement);
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addMarkerEvent(EnhancedForStatement forStatement, IfStatement ifStatement, Block ifBodyBlock) {
		AST ast = forStatement.getAST();
		EnhancedForStatement newForStatement = (EnhancedForStatement) ASTNode.copySubtree(ast, forStatement);
		IfStatement newIfStatement = (IfStatement) ASTNode.copySubtree(ast, ifStatement);
		Block newIfBodyBlock = (Block) ASTNode.copySubtree(ast, ifBodyBlock);
		BreakStatement breakStatement = ast.newBreakStatement();
		newIfBodyBlock.statements()
			.add(breakStatement);
		newIfStatement.setThenStatement(newIfBodyBlock);
		newForStatement.setBody(newIfStatement);
		int credit = description.getCredit();
		int highlightLength = newForStatement.getLength() + breakStatement.toString()
			.length();
		int offset = forStatement.getStartPosition();
		int length = forStatement.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(forStatement.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(newForStatement.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addMarkerEvent(EnhancedForStatement forStatement, IfStatement ifStatement,
			ExpressionStatement thenStatement) {
		AST ast = forStatement.getAST();
		EnhancedForStatement newForStatement = (EnhancedForStatement) ASTNode.copySubtree(ast, forStatement);
		IfStatement newIfStatement = (IfStatement) ASTNode.copySubtree(ast, ifStatement);
		Block newIfBodyBlock = ast.newBlock();
		ExpressionStatement newThenStatement = (ExpressionStatement) ASTNode.copySubtree(ast, thenStatement);
		BreakStatement breakStatement = ast.newBreakStatement();
		newIfBodyBlock.statements()
			.add(newThenStatement);
		newIfBodyBlock.statements()
			.add(breakStatement);
		newIfStatement.setThenStatement(newIfBodyBlock);
		newForStatement.setBody(newIfStatement);
		int credit = description.getCredit();
		int highlightLength = newForStatement.getLength() + breakStatement.toString()
			.length();
		int offset = forStatement.getStartPosition();
		int length = forStatement.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(forStatement.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(newForStatement.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}
