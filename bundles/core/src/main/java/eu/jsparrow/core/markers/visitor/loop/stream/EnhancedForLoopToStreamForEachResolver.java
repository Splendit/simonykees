package eu.jsparrow.core.markers.visitor.loop.stream;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamForEachRule;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamForEachASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type {@link EnhancedForLoopToStreamForEachASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public class EnhancedForLoopToStreamForEachResolver extends EnhancedForLoopToStreamForEachASTVisitor
		implements Resolver {

	public static final String ID = "EnhancedForLoopToStreamForEachResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public EnhancedForLoopToStreamForEachResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(EnhancedForLoopToStreamForEachRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public void endVisit(EnhancedForStatement forStatement) {
		if (positionChecker.test(forStatement)) {
			super.endVisit(forStatement);
		}
	}

	@Override
	public void addMarkerEvent(EnhancedForStatement loop) {
		int credit = description.getCredit();
		int highlightLength = 0;
		int offset = loop.getStartPosition();
		int length = loop.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(loop.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(description.getDescription())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}
