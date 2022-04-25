package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.MakeFieldsAndVariablesFinalRule;
import eu.jsparrow.core.visitor.make_final.MakeFieldsAndVariablesFinalASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link MakeFieldsAndVariablesFinalASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public class MakeFieldsAndVariablesFinalResolver extends MakeFieldsAndVariablesFinalASTVisitor implements Resolver {

	public static final String ID = "MakeFieldsAndVariablesFinalResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public MakeFieldsAndVariablesFinalResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(MakeFieldsAndVariablesFinalRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		if (positionChecker.test(fieldDeclaration)) {
			super.visit(fieldDeclaration);
		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement variableDeclarationStatement) {
		if (positionChecker.test(variableDeclarationStatement)) {
			super.visit(variableDeclarationStatement);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(VariableDeclarationStatement node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}

	@Override
	public void addMarkerEvent(FieldDeclaration node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}

}
