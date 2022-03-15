package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.ReImplementingInterfaceRule;
import eu.jsparrow.core.visitor.impl.ReImplementingInterfaceASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class ReImplementingInterfaceResolver extends ReImplementingInterfaceASTVisitor implements Resolver {

	public static final String ID = "ReImplementingInterfaceResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public ReImplementingInterfaceResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(ReImplementingInterfaceRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}
	
	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		List<Type> interfaces = ASTNodeUtil.convertToTypedList(typeDeclaration.superInterfaceTypes(),
				Type.class);
		for(Type type : interfaces) {
			if (positionChecker.test(type)) {
				super.visit(typeDeclaration);
				break;
			}
		}

		return true;
	}

	@Override
	public void addMarkerEvent(Type node) {
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
}
