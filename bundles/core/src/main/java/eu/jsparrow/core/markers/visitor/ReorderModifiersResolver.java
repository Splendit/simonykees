package eu.jsparrow.core.markers.visitor;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.ReorderModifiersRule;
import eu.jsparrow.core.visitor.impl.ReorderModifiersASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class ReorderModifiersResolver extends ReorderModifiersASTVisitor implements Resolver {

	public static final String ID = "ReorderModifiersResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public ReorderModifiersResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(ReorderModifiersRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(fieldDeclaration.modifiers(), Modifier.class);
		for (Modifier modifier : modifiers) {
			if (positionChecker.test(modifier)) {
				super.visit(fieldDeclaration);
				break;
			}
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(), Modifier.class);
		for (Modifier modifier : modifiers) {
			if (positionChecker.test(modifier)) {
				super.visit(methodDeclaration);
				break;
			}
		}
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(typeDeclaration.modifiers(), Modifier.class);
		for (Modifier modifier : modifiers) {
			if (positionChecker.test(modifier)) {
				super.visit(typeDeclaration);
				break;
			}
		}
		return true;
	}

	@Override
	public void addMarkerEvent(List<Modifier> modifiers) {
		int credit = description.getCredit();
		int highlightLength = 0;
		Modifier firstModifier = modifiers.get(0);
		int offset = modifiers.stream()
			.mapToInt(Modifier::getStartPosition)
			.min()
			.orElse(firstModifier.getStartPosition());
		int length = modifiers.stream()
			.max(Comparator.comparing(Modifier::getStartPosition))
			.map(modifier -> (modifier.getStartPosition() + modifier.getLength()) - offset)
			.orElse(firstModifier.getLength());
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(firstModifier.getStartPosition());
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
