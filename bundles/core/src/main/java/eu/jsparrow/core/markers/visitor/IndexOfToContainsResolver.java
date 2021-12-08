package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.IndexOfToContainsRule;
import eu.jsparrow.core.visitor.impl.IndexOfToContainsASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

public class IndexOfToContainsResolver extends IndexOfToContainsASTVisitor {
	
	public static final String ID = IndexOfToContainsResolver.class.getName();

	private Predicate<ASTNode> positionChecker;
	private IJavaElement javaElement;
	private RuleDescription description;

	public IndexOfToContainsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(IndexOfToContainsRule.RULE_ID);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation.getParent())) {
			super.visit(methodInvocation);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(InfixExpression node, Expression methodExpression, Expression methodArgument) {
		int credit = description.getCredit();
		MethodInvocation newNode = createContainsInvocation(methodExpression, methodArgument);
		int highlightLength = newNode.toString().length();
		RefactoringMarkerEvent event = new RefactoringEventImpl(ID,
				description.getName(),
				description.getDescription(),
				javaElement,
				highlightLength,
				node, newNode, credit);
		addMarkerEvent(event);
	}
	
	@Override
	public void addMarkerEvent(InfixExpression node, Expression methodExpression, Expression methodArgument, PrefixExpression.Operator not) {
		int credit = description.getCredit();
		MethodInvocation contains = createContainsInvocation(methodExpression, methodArgument);
		AST ast = node.getAST();
		PrefixExpression newNode = ast.newPrefixExpression();
		newNode.setOperator(not);
		newNode.setOperand(contains);
		int highlightLength = newNode.toString().length();
		RefactoringMarkerEvent event = new RefactoringEventImpl(ID,
				description.getName(),
				description.getDescription(),
				javaElement,
				highlightLength,
				node, newNode, credit);
		addMarkerEvent(event);
	}
	
	@SuppressWarnings("unchecked")
	private MethodInvocation createContainsInvocation(Expression methodExpression, Expression methodArgument) {
		AST ast = methodExpression.getAST();
		MethodInvocation contains = ast.newMethodInvocation();
		contains.setName(ast.newSimpleName("contains")); //$NON-NLS-1$
		contains.setExpression((Expression)ASTNode.copySubtree(ast, methodExpression));
		contains.arguments().add((Expression)ASTNode.copySubtree(ast, methodArgument));
		return contains;
	}

}
