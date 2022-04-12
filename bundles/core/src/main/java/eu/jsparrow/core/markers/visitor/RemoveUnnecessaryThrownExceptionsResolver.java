package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.RemoveUnnecessaryThrownExceptionsRule;
import eu.jsparrow.core.visitor.impl.RemoveUnnecessaryThrownExceptionsASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * A visitor for resolving one issue of type
 * {@link RemoveUnnecessaryThrownExceptionsASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public class RemoveUnnecessaryThrownExceptionsResolver extends RemoveUnnecessaryThrownExceptionsASTVisitor
		implements Resolver {
	public static final String ID = "RemoveUnnecessaryThrownExceptionsResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public RemoveUnnecessaryThrownExceptionsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(RemoveUnnecessaryThrownExceptionsRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		List<Type> thrownTypes = ASTNodeUtil.convertToTypedList(methodDeclaration.thrownExceptionTypes(), Type.class);
		for (Type type : thrownTypes) {
			if (positionChecker.test(type)) {
				super.visit(methodDeclaration);
				break;
			}
		}

		return true;
	}

	@Override
	public void addMarkerEvent(Type node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}
}
