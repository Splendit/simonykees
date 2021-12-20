package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.UseCollectionsSingletonListRule;
import eu.jsparrow.core.visitor.impl.UseCollectionsSingletonListASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A visitor for resolving one issue of type
 * {@link UseCollectionsSingletonListASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public class UseCollectionsSingletonListResolver extends UseCollectionsSingletonListASTVisitor implements Resolver {

	public static final String ID = "UseCollectionsSingletonListResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private IJavaElement javaElement;
	private RuleDescription description;

	public UseCollectionsSingletonListResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(UseCollectionsSingletonListRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation)) {
			super.visit(methodInvocation);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(SimpleName methodName, SimpleName newNode) {
		int credit = description.getCredit();
		int highlightLength = newNode.getLength();
		ASTNode original = methodName.getParent();
		int offset = original.getStartPosition();
		int length = original.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(original.getStartPosition());
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(newNode.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}
