package eu.jsparrow.core.markers.visitor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.PutIfAbsentRule;
import eu.jsparrow.core.visitor.impl.PutIfAbsentASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A visitor for resolving one issue of type {@link PutIfAbsentASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public class PutIfAbsentResolver extends PutIfAbsentASTVisitor implements Resolver {

	public static final String ID = "PutIfAbsentResolver"; //$NON-NLS-1$
	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public PutIfAbsentResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
				.findByRuleId(PutIfAbsentRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation)) {
			return super.visit(methodInvocation);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(MethodInvocation methodInvocation) {
		ExpressionStatement newNode = createRepresentingNode(methodInvocation);
		int credit = description.getCredit();
		int offset = methodInvocation.getStartPosition();
		int length = methodInvocation.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(methodInvocation.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(Messages.PutIfAbsentResolver_name)
			.withMessage(Messages.PutIfAbsentResolver_message)
			.withIJavaElement(javaElement)
			.withHighlightLength(0)
			.withOffset(offset)
			.withCodePreview(newNode.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}

	private ExpressionStatement createRepresentingNode(MethodInvocation methodInvocation) {
		AST ast = methodInvocation.getAST();
		SimpleName putIfAbsentName = ast.newSimpleName(PUT_IF_ABSENT);
		@SuppressWarnings("unchecked")
		List<Expression> arguments = methodInvocation.arguments();
		Expression firstArgument = (Expression) ASTNode.copySubtree(ast, arguments.get(0));
		Expression secondArgument = (Expression) ASTNode.copySubtree(ast, arguments.get(1));
		MethodInvocation putIfAbsent = NodeBuilder.newMethodInvocation(methodInvocation.getAST(),
				(Expression) ASTNode.copySubtree(ast, methodInvocation.getExpression()), putIfAbsentName,
				Arrays.asList(firstArgument, secondArgument));

		return NodeBuilder.newExpressionStatement(methodInvocation.getAST(), putIfAbsent);
	}
}
