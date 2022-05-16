package eu.jsparrow.core.markers.visitor.junit;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;

import eu.jsparrow.core.markers.RefactoringMarkerEventFactory;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.ReplaceJUnitExpectedAnnotationPropertyRule;
import eu.jsparrow.core.rule.impl.ReplaceJUnitExpectedExceptionRule;
import eu.jsparrow.core.visitor.junit.ReplaceJUnitExpectedExceptionASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class ReplaceJUnitExpectedExceptionResolver extends ReplaceJUnitExpectedExceptionASTVisitor implements Resolver  {
	public static final String ID = "ReplaceJUnitExpectedExceptionResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public ReplaceJUnitExpectedExceptionResolver(Predicate<ASTNode> positionChecker) {
		super(""); //$NON-NLS-1$
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(ReplaceJUnitExpectedExceptionRule.RULE_ID);
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
	public boolean visit(MethodDeclaration methodDeclaration) {
		HelperVisitor visitor = new HelperVisitor();
		methodDeclaration.accept(visitor);
		if(visitor.hasMatch()) {
			return super.visit(methodDeclaration);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(ASTNode node) {
		RefactoringMarkerEvent event = RefactoringMarkerEventFactory.createEventForNode(getCompilationUnit(), node, ID,
				description);
		addMarkerEvent(event);
	}
	
	
	class HelperVisitor extends ASTVisitor {
		private boolean match = false;
		
		@Override
		public boolean preVisit2(ASTNode node) {
			return !match;
		}
		
		@Override
		public boolean visit(MethodInvocation methodInvocation) {
			if(positionChecker.test(methodInvocation)) {
				this.match = true;
			}
			return true;
		}
		
		@Override
		public boolean visit(ClassInstanceCreation classInstanceCreation) {
			if(positionChecker.test(classInstanceCreation)) {
				this.match = true;
			}
			return true;
		}
		
		@Override
		public boolean visit(ThrowStatement throwStatement) {
			if(positionChecker.test(throwStatement)) {
				this.match = true;
			}
			return true;
		}
		
		public boolean hasMatch() {
			return this.match;
		}

	}
}
