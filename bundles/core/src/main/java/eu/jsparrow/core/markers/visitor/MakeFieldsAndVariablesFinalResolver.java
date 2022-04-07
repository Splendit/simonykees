package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.MakeFieldsAndVariablesFinalRule;
import eu.jsparrow.core.visitor.make_final.MakeFieldsAndVariablesFinalASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class MakeFieldsAndVariablesFinalResolver extends MakeFieldsAndVariablesFinalASTVisitor implements Resolver {

	public static final String ID = "MakeFieldsAndVariablesFinalResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public MakeFieldsAndVariablesFinalResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(MakeFieldsAndVariablesFinalRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		if (positionChecker.test(fieldDeclaration)) {
			super.visit(fieldDeclaration);
		}
		return true;
	}
	
	@Override
	public boolean visit(VariableDeclarationStatement variableDeclarationStatement) {
		if (positionChecker.test(variableDeclarationStatement)) {
			super.visit(variableDeclarationStatement);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(VariableDeclarationStatement node) {
		addMarker(node);
	}

	@Override
	public void addMarkerEvent(FieldDeclaration node) {
		addMarker(node);
	}

	private void addMarker(ASTNode node) {
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
