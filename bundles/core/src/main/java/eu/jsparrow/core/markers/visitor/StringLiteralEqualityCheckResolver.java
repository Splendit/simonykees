package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.StringLiteralEqualityCheckRule;
import eu.jsparrow.core.visitor.impl.StringLiteralEqualityCheckASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A visitor for resolving one issue of type
 * {@link StringLiteralEqualityCheckASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public class StringLiteralEqualityCheckResolver extends StringLiteralEqualityCheckASTVisitor implements Resolver {

	public static final String ID = "StringLiteralEqualityCheckResolver"; //$NON-NLS-1$
	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public StringLiteralEqualityCheckResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(StringLiteralEqualityCheckRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(StringLiteral stringLiteral) {
		if (positionChecker.test(stringLiteral)) {
			return super.visit(stringLiteral);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(StringLiteral stringLiteral, Expression expression) {
		MethodInvocation newNode = createRepresentingNode(expression, stringLiteral);
		int credit = description.getCredit();
		int offset = stringLiteral.getStartPosition();
		int length = stringLiteral.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(stringLiteral.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(Messages.StringLiteralEqualityCheckResolver_name)
			.withMessage(Messages.StringLiteralEqualityCheckResolver_message)
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

	private MethodInvocation createRepresentingNode(Expression expression, StringLiteral stringLiteral) {
		AST ast = expression.getAST();
		MethodInvocation equals = ast.newMethodInvocation();
		equals.setName(ast.newSimpleName(EQUALS));
		equals.setExpression((Expression) ASTNode.copySubtree(ast, stringLiteral));
		@SuppressWarnings("unchecked")
		List<Expression> arguments = equals.arguments();
		arguments.add((Expression) ASTNode.copySubtree(ast, expression));
		return equals;
	}
}
