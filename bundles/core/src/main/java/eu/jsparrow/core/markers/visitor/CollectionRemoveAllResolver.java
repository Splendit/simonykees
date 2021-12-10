package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.CollectionRemoveAllRule;
import eu.jsparrow.core.visitor.impl.CollectionRemoveAllASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A visitor for resolving one issue of type
 * {@link CollectionRemoveAllASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public class CollectionRemoveAllResolver extends CollectionRemoveAllASTVisitor {

	public static final String ID = CollectionRemoveAllResolver.class.getName();

	private Predicate<ASTNode> positionChecker;
	private IJavaElement javaElement;
	private RuleDescription description;

	public CollectionRemoveAllResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(CollectionRemoveAllRule.RULE_ID);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation)) {
			super.visit(methodInvocation);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(MethodInvocation node) {
		int credit = description.getCredit();
		AST ast = node.getAST();
		MethodInvocation newNode = ast.newMethodInvocation();
		newNode.setName(ast.newSimpleName("clear")); //$NON-NLS-1$
		newNode.setExpression((Expression) ASTNode.copySubtree(ast, node.getExpression()));
		int highlightLength = newNode.getLength();
		RefactoringMarkerEvent event = new RefactoringEventImpl(ID,
				description.getName(),
				description.getDescription(),
				javaElement,
				highlightLength,
				node, newNode, credit);
		addMarkerEvent(event);
	}
}
