package eu.jsparrow.core.markers.visitor.lambdaforeach;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.LambdaForEachIfWrapperToFilterRule;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachIfWrapperToFilterASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * A visitor for resolving one issue of type
 * {@link LambdaForEachIfWrapperToFilterASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public class LambdaForEachIfWrapperToFilterResolver extends LambdaForEachIfWrapperToFilterASTVisitor
		implements Resolver {

	public static final String ID = "LambdaForEachIfWrapperToFilterResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public LambdaForEachIfWrapperToFilterResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(LambdaForEachIfWrapperToFilterRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.isEmpty()) {
			return false;
		}
		Expression firstArgument = arguments.get(0);
		if (positionChecker.test(firstArgument)) {
			super.visit(methodInvocation);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addMarkerEvent(MethodInvocation methodInvocation, Expression ifExpression,
			VariableDeclaration parameterDeclaration) {
		int credit = description.getCredit();
		int highlightLength = 0;
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		Expression forEachLambda = arguments.get(0);
		int offset = forEachLambda.getStartPosition();
		int length = forEachLambda.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(forEachLambda.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		AST ast = methodInvocation.getAST();
		MethodInvocation filter = ast.newMethodInvocation();
		filter.setName(ast.newSimpleName("filter")); //$NON-NLS-1$
		LambdaExpression lambda = ast.newLambdaExpression();
		lambda.setBody(ASTNode.copySubtree(ast, ifExpression));
		lambda.parameters()
			.add((VariableDeclaration) ASTNode.copySubtree(ast, parameterDeclaration));
		filter.arguments()
			.add(lambda);
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(filter.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}

}
