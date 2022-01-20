package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.UseIsEmptyOnCollectionsRule;
import eu.jsparrow.core.visitor.impl.UseIsEmptyOnCollectionsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link UseIsEmptyOnCollectionsASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public class UseIsEmptyOnCollectionsResolver extends UseIsEmptyOnCollectionsASTVisitor implements Resolver {

	public static final String ID = "UseIsEmptyOnCollectionsResolver"; //$NON-NLS-1$
	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public UseIsEmptyOnCollectionsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
				.findByRuleId(UseIsEmptyOnCollectionsRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation.getParent())) {
			return super.visit(methodInvocation);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(InfixExpression parent, Expression varExpression) {
		ASTNode newNode = createRepresentationNode(parent, varExpression);
		int credit = description.getCredit();
		int offset = parent.getStartPosition();
		int length = parent.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(parent.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(Messages.UseIsEmptyOnCollectionsResolver_name)
			.withMessage(Messages.UseIsEmptyOnCollectionsResolver_message)
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

	private ASTNode createRepresentationNode(InfixExpression infixExpression, Expression varExpression) {
		AST ast = infixExpression.getAST();
		SimpleName isEmptyMethod = ast.newSimpleName("isEmpty"); //$NON-NLS-1$
		MethodInvocation replaceNode = NodeBuilder.newMethodInvocation(ast,
				(Expression) ASTNode.copySubtree(ast, varExpression), isEmptyMethod);
		StructuralPropertyDescriptor locationInParent = infixExpression.getLocationInParent();
		ASTNode parent = ASTNode.copySubtree(ast, infixExpression.getParent());
		parent.setStructuralProperty(locationInParent, replaceNode);
		return parent;
	}
}
