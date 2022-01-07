package eu.jsparrow.core.markers.visitor.loop;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.ForToForEachRule;
import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type {@link ForToForEachASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public class ForToForEachResolver extends ForToForEachASTVisitor implements Resolver {

	public static final String ID = "ForToForEachResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public ForToForEachResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(ForToForEachRule.FOR_TO_FOR_EACH_RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(ForStatement forStatement) {
		if (positionChecker.test(forStatement)) {
			super.visit(forStatement);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(ForStatement loop, SimpleName iterableNode,
			SingleVariableDeclaration iteratorDecl) {
		int credit = description.getCredit();
		String codeRepresentation = String.format("for (%s : %s) {%n \t...%n}", iteratorDecl.toString(), //$NON-NLS-1$
				iterableNode.toString());
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
			.withCodePreview(codeRepresentation)
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}
