package eu.jsparrow.core.markers.visitor.junit;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.ReplaceJUnitExpectedAnnotationPropertyRule;
import eu.jsparrow.core.visitor.junit.ReplaceJUnitExpectedAnnotationPropertyASTVisitor;
import eu.jsparrow.core.visitor.junit.TestMethodUtil;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link ReplaceJUnitExpectedAnnotationPropertyASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public class ReplaceJUnitExpectedAnnotationPropertyResolver extends ReplaceJUnitExpectedAnnotationPropertyASTVisitor
		implements Resolver {

	public static final String ID = "ReplaceJUnitExpectedAnnotationPropertyResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public ReplaceJUnitExpectedAnnotationPropertyResolver(Predicate<ASTNode> positionChecker) {
		super(""); //$NON-NLS-1$
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(ReplaceJUnitExpectedAnnotationPropertyRule.RULE_ID);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		IJavaProject project = compilationUnit.getJavaElement()
			.getJavaProject();
		ReplaceJUnitExpectedAnnotationPropertyRule rule = new ReplaceJUnitExpectedAnnotationPropertyRule();
		rule.calculateEnabledForProject(project);
		String assertThrowsQualifiedName = rule.getAssertThrowsQualifiedName();
		super.updateAssertThrowsQualifiedName(assertThrowsQualifiedName);
		return super.visit(compilationUnit);

	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
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
