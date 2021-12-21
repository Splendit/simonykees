package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.RemoveUnusedParameterRule;
import eu.jsparrow.core.visitor.impl.RemoveUnusedParameterASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A visitor for resolving one issue of type
 * {@link RemoveUnusedParameterASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public class RemoveUnusedParameterResolver extends RemoveUnusedParameterASTVisitor implements Resolver {

	public static final String ID = "RemoveUnusedParameterResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public RemoveUnusedParameterResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(RemoveUnusedParameterRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		if (positionChecker.test(methodDeclaration)) {
			super.visit(methodDeclaration);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(SingleVariableDeclaration parameter, MethodDeclaration methodDeclaration,
			int parameterIndex) {

		int credit = description.getCredit();
		int highlightLength = 0;
		AST ast = methodDeclaration.getAST();
		MethodDeclaration newMethodDeclaration = (MethodDeclaration) ASTNode.copySubtree(ast, methodDeclaration);
		newMethodDeclaration.setBody(ast.newBlock());
		newMethodDeclaration.parameters()
			.remove(parameterIndex);
		Javadoc javaDoc = newMethodDeclaration.getJavadoc();
		if (javaDoc != null) {
			javaDoc.delete();
		}

		int offset = parameter.getStartPosition();
		int length = parameter.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(parameter.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(newMethodDeclaration.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}
