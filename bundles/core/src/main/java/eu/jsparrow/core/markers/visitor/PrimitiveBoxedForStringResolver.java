package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.PrimitiveBoxedForStringRule;
import eu.jsparrow.core.visitor.impl.PrimitiveBoxedForStringASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A visitor for resolving one issue of type
 * {@link PrimitiveBoxedForStringASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public class PrimitiveBoxedForStringResolver extends PrimitiveBoxedForStringASTVisitor implements Resolver {

	public static final String ID = "PrimitiveBoxedForStringResolver"; //$NON-NLS-1$
	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public PrimitiveBoxedForStringResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(PrimitiveBoxedForStringRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (positionChecker.test(node)) {
			return super.visit(node);
		}
		return false;
	}

	@Override
	public boolean visit(StringLiteral node) {
		if (positionChecker.test(node)) {
			return super.visit(node);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(ASTNode node, Expression refactorCandidateExpression, SimpleName name,
			SimpleName refactorPrimitiveType) {
		MethodInvocation newNode = createRepresentingNode(refactorCandidateExpression, name, refactorPrimitiveType);
		int highlightLength = 0;
		if (node.getNodeType() == ASTNode.METHOD_INVOCATION) {
			highlightLength = newNode.toString()
				.length();
		}
		int credit = description.getCredit();
		int offset = node.getStartPosition();
		int length = node.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(node.getStartPosition());
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(Messages.PrimitiveBoxedForStringResolver_name)
			.withMessage(Messages.PrimitiveBoxedForStringResolver_message)
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

	@SuppressWarnings("unchecked")
	private MethodInvocation createRepresentingNode(Expression refactorCandidateExpression,
			SimpleName methodName,
			SimpleName primitiveType) {
		AST ast = refactorCandidateExpression.getAST();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		Expression moveTargetArgument = (Expression) ASTNode.copySubtree(ast, refactorCandidateExpression);
		methodInvocation.arguments()
			.add(moveTargetArgument);

		SimpleName staticClassType = astRewrite.getAST()
			.newSimpleName(primitiveType.getIdentifier());
		methodInvocation.setExpression(staticClassType);
		methodInvocation.setName(ast.newSimpleName(methodName.getIdentifier()));
		return methodInvocation;
	}
}
