package eu.jsparrow.core.markers.visitor.factory.methods;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.CollectionsFactoryMethodsRule;
import eu.jsparrow.core.visitor.factory.methods.CollectionsFactoryMethodsASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

public class CollectionsFactoryMethodsResolver extends CollectionsFactoryMethodsASTVisitor implements Resolver {


	public static final String ID = "CollectionsFactoryMethodsResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public CollectionsFactoryMethodsResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(CollectionsFactoryMethodsRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation)) {
			super.visit(methodInvocation);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(MethodInvocation methodInvocation, String expressionTypeName, String factoryMethodName,
			List<Expression> elements) {
		int credit = description.getCredit();
		int highlightLength = 0;
		int offset = methodInvocation.getStartPosition();
		int length = methodInvocation.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(methodInvocation.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		ASTNode newNode = createRepresentingNode(methodInvocation, expressionTypeName, factoryMethodName, elements);
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(newNode.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}

	private ASTNode createRepresentingNode(MethodInvocation methodInvocation, String expressionTypeName,
			String factoryMethodName, List<Expression> elements) {
		AST ast = methodInvocation.getAST();
		MethodInvocation newMethodInvocation = ast.newMethodInvocation();
		newMethodInvocation.setName(ast.newSimpleName(factoryMethodName));
		newMethodInvocation.setExpression(ast.newSimpleName(expressionTypeName));
		@SuppressWarnings("unchecked")
		List<Expression> arguments = newMethodInvocation.arguments();
		for(Expression element : elements) {
			Expression argument = (Expression) ASTNode.copySubtree(ast, element);
			arguments.add(argument);
		}
		return newMethodInvocation;
	}
}
