package eu.jsparrow.core.markers.visitor.loop.stream;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamTakeWhileRule;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamTakeWhileASTVisitor;
import eu.jsparrow.core.visitor.stream.tolist.ReplaceStreamCollectByToListASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link ReplaceStreamCollectByToListASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public class EnhancedForLoopToStreamTakeWhileResolver extends EnhancedForLoopToStreamTakeWhileASTVisitor
		implements Resolver {

	public static final String ID = "EnhancedForLoopToStreamTakeWhileResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public EnhancedForLoopToStreamTakeWhileResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(EnhancedForLoopToStreamTakeWhileRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(EnhancedForStatement forStatement) {
		if (positionChecker.test(forStatement)) {
			super.visit(forStatement);
		}
		return false;
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
