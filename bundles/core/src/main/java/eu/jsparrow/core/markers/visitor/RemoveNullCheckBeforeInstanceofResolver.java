package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.RemoveNullCheckBeforeInstanceofRule;
import eu.jsparrow.core.visitor.impl.RemoveNullCheckBeforeInstanceofASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A visitor for resolving one issue of type
 * {@link RemoveNullCheckBeforeInstanceofASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public class RemoveNullCheckBeforeInstanceofResolver extends RemoveNullCheckBeforeInstanceofASTVisitor implements Resolver {

	public static final String ID = "RemoveNullCheckBeforeInstanceofResolver"; //$NON-NLS-1$
	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public RemoveNullCheckBeforeInstanceofResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
				.findByRuleId(RemoveNullCheckBeforeInstanceofRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(InstanceofExpression instanceOfExpression) {
		ASTNode parent = instanceOfExpression.getParent();
		if (parent.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression infixExpression = (InfixExpression) parent;
			if (positionChecker.test(infixExpression.getLeftOperand())) {
				super.visit(instanceOfExpression);
			}
		}
		return false;
	}

	@Override
	public void addMarkerEvent(Expression leftOperand, InfixExpression infixExpression, Expression expression) {
		ASTNode newNode = createRepresentingNode(infixExpression, expression);
		int credit = description.getCredit();
		int offset = leftOperand.getStartPosition();
		int length = leftOperand.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(leftOperand.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(Messages.RemoveNullCheckBeforeInstanceofResolver_name)
			.withMessage(Messages.RemoveNullCheckBeforeInstanceofResolver_message)
			.withIJavaElement(javaElement)
			.withHighlightLength(0)
			.withOffset(offset)
			.withCodePreview(newNode.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}

	private ASTNode createRepresentingNode(InfixExpression infixExpression, Expression expression) {
		AST ast = infixExpression.getAST();
		StructuralPropertyDescriptor structuralProperty = infixExpression.getLocationInParent();
		ASTNode parent = ASTNode.copySubtree(ast, infixExpression.getParent());
		parent.setStructuralProperty(structuralProperty, (Expression) ASTNode.copySubtree(ast, expression));
		return parent;
	}
}
