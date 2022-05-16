package eu.jsparrow.core.markers.visitor.junit;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.ReplaceJUnitTimeoutAnnotationPropertyRule;
import eu.jsparrow.core.visitor.junit.ReplaceJUnitTimeoutAnnotationPropertyASTVisitor;
import eu.jsparrow.core.visitor.junit.TestMethodUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class ReplaceJUnitTimeoutAnnotationPropertyResolver extends ReplaceJUnitTimeoutAnnotationPropertyASTVisitor implements Resolver {

	public static final String ID = "ReplaceJUnitTimeoutAnnotationPropertyResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public ReplaceJUnitTimeoutAnnotationPropertyResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(ReplaceJUnitTimeoutAnnotationPropertyRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		if (positionChecker.test(node)) {
			super.visit(node);
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		NormalAnnotation annotation = TestMethodUtil.findTestAnnotatedMethod(node)
			.orElse(null);
		if (annotation == null) {
			return false;
		}

		if (positionChecker.test(annotation)) {
			super.visit(node);
		}
		return true;
	}
	
	@Override
	public void addMarkerEvent(NormalAnnotation node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}
}
