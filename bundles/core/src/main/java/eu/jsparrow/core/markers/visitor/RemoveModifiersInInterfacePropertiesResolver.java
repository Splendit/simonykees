package eu.jsparrow.core.markers.visitor;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.RemoveModifiersInInterfacePropertiesRule;
import eu.jsparrow.core.visitor.impl.RemoveModifiersInInterfacePropertiesASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class RemoveModifiersInInterfacePropertiesResolver extends RemoveModifiersInInterfacePropertiesASTVisitor
		implements Resolver {

	public static final String ID = "RemoveModifiersInInterfacePropertiesResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public RemoveModifiersInInterfacePropertiesResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(RemoveModifiersInInterfacePropertiesRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {

		List<Modifier> fieldModifiers = Arrays.stream(typeDeclaration.getFields())
			.flatMap(field -> convertToTypedList(field.modifiers(), Modifier.class).stream())
			.filter(modifier -> modifier.isPublic() || modifier.isStatic() || modifier.isFinal())
			.collect(Collectors.toList());

		boolean matchedFieldModifier = fieldModifiers.stream()
			.anyMatch(positionChecker);

		if (matchedFieldModifier) {
			super.visit(typeDeclaration);
			return true;
		}

		List<Modifier> methodModifiers = Arrays.stream(typeDeclaration.getMethods())
			.flatMap(method -> convertToTypedList(method.modifiers(), Modifier.class).stream())
			.filter(Modifier::isPublic)
			.collect(Collectors.toList());

		boolean matchedMethodModifier = methodModifiers.stream()
			.anyMatch(positionChecker);

		if (matchedMethodModifier) {
			super.visit(typeDeclaration);
		}

		return true;
	}

	@Override
	public void addMarkerEvent(Modifier node) {
		int credit = description.getCredit();
		int highlightLength = 0;
		int offset = node.getStartPosition();
		int length = node.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(node.getStartPosition());
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
	
	@Override
	protected void removeModifier(Modifier modifier) {
		if(positionChecker.test(modifier)) {
			super.removeModifier(modifier);
		}
	}
}
