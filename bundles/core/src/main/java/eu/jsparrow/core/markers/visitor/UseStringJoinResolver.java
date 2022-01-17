package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.UseStringJoinRule;
import eu.jsparrow.core.visitor.impl.UseStringJoinASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type {@link UseStringJoinASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public class UseStringJoinResolver extends UseStringJoinASTVisitor implements Resolver {

	public static final String ID = "UseStringJoinResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public UseStringJoinResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(UseStringJoinRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation.getParent())) {
			super.visit(methodInvocation);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(MethodInvocation parentMethod, Expression collection, List<Expression> joinArguments) {

		int credit = description.getCredit();
		MethodInvocation newNode = createRepresentingNode(parentMethod, collection, joinArguments);
		int highlightLength = 0;
		int offset = parentMethod.getStartPosition();
		int length = parentMethod.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(parentMethod.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
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

	private MethodInvocation createRepresentingNode(MethodInvocation parentMethod, Expression collection,
			List<Expression> joinArguments) {
		AST ast = parentMethod.getAST();
		SimpleName expression = ast.newSimpleName("String"); //$NON-NLS-1$
		SimpleName name = ast.newSimpleName("join"); //$NON-NLS-1$
		MethodInvocation stringJoin = NodeBuilder.newMethodInvocation(ast, expression, name);
		Expression delimiter = joinArguments.isEmpty() ? ast.newStringLiteral()
				: (Expression) ASTNode.copySubtree(ast, joinArguments.get(0));
		Expression ietrable = (Expression) ASTNode.copySubtree(ast, collection);
		@SuppressWarnings("unchecked")
		List<Expression> stringJoinArguments = stringJoin.arguments();
		stringJoinArguments.add(delimiter);
		stringJoinArguments.add(ietrable);
		return stringJoin;
	}
}
