package eu.jsparrow.core.markers.visitor.loop;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.WhileToForEachRule;
import eu.jsparrow.core.visitor.loop.whiletoforeach.WhileToForEachASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link WhileToForEachASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public class WhileToForEachResolver extends WhileToForEachASTVisitor implements Resolver {

	public static final String ID = "WhileToForEachResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public WhileToForEachResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(WhileToForEachRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(WhileStatement whileStatement) {
		if (positionChecker.test(whileStatement)) {
			super.visit(whileStatement);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(WhileStatement loop, SimpleName iterableNode,
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
