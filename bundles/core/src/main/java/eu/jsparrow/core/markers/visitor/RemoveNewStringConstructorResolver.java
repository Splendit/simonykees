package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.RemoveNewStringConstructorRule;
import eu.jsparrow.core.visitor.impl.RemoveNewStringConstructorASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A visitor for resolving one issue of type
 * {@link RemoveNewStringConstructorASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public class RemoveNewStringConstructorResolver extends RemoveNewStringConstructorASTVisitor implements Resolver {

	public static final String ID = "RemoveNewStringConstructorResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private IJavaElement javaElement;
	private RuleDescription description;

	public RemoveNewStringConstructorResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(RemoveNewStringConstructorRule.RULE_ID);
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
	public boolean visit(ClassInstanceCreation classInstanceCreation) {
		if (positionChecker.test(classInstanceCreation)) {
			super.visit(classInstanceCreation);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(ClassInstanceCreation node, Expression replacement) {
		int credit = description.getCredit();
		int highlightLength = replacement.getLength();
		int offset = node.getStartPosition();
		int length = node.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(node.getStartPosition());
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(replacement.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}
