package eu.jsparrow.rules.java16.patternmatching;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.PatternInstanceofExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link UsePatternMatchingForInstanceofASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public class UsePatternMatchingForInstanceofResolver extends UsePatternMatchingForInstanceofASTVisitor
		implements Resolver {

	public static final String ID = "UsePatternMatchingForInstanceofResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public UsePatternMatchingForInstanceofResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = new UsePatternMatchingForInstanceofRule().getRuleDescription();
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(InstanceofExpression instanceOfExpression) {
		if (positionChecker.test(instanceOfExpression)) {
			super.visit(instanceOfExpression);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(InstanceofExpression instanceOf, SimpleName name) {
		int credit = description.getCredit();
		int offset = instanceOf.getStartPosition();
		int length = instanceOf.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(instanceOf.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();

		AST ast = instanceOf.getAST();
		PatternInstanceofExpression newPatternInstanceOf = ast.newPatternInstanceofExpression();
		newPatternInstanceOf.setLeftOperand((Expression) ASTNode.copySubtree(ast, instanceOf.getLeftOperand()));
		SingleVariableDeclaration singleVarDecl = ast.newSingleVariableDeclaration();
		singleVarDecl.setType((Type) ASTNode.copySubtree(ast, instanceOf.getRightOperand()));
		singleVarDecl.setName((SimpleName) ASTNode.copySubtree(ast, name));
		newPatternInstanceOf.setRightOperand(singleVarDecl);
		int highlightLength = newPatternInstanceOf.getLength();

		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(newPatternInstanceOf.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}
