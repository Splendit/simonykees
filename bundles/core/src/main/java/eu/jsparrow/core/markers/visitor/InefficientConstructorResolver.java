package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.constants.ReservedNames;
import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.InefficientConstructorRule;
import eu.jsparrow.core.visitor.impl.InefficientConstructorASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * A visitor for resolving one issue of type
 * {@link InefficientConstructorASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public class InefficientConstructorResolver extends InefficientConstructorASTVisitor implements Resolver {

	public static final String ID = "InefficientConstructorResolver"; //$NON-NLS-1$
	private RuleDescription description;

	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;

	public InefficientConstructorResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(InefficientConstructorRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(node.arguments(), Expression.class);
		if (arguments.isEmpty()) {
			return false;
		}
		Expression argument = arguments.get(0);
		if (positionChecker.test(argument)) {
			return super.visit(node);
		}
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (positionChecker.test(node)) {
			return super.visit(node);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(Expression refactorCandidateParameter, MethodInvocation node,
			Expression replaceParameter) {
		MethodInvocation newNode = createRepresentingNode(node, replaceParameter);
		int credit = description.getCredit();
		int offset = node.getStartPosition();
		int length = node.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(node.getStartPosition());
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
				.withResolver(ID)
				.withName(Messages.InefficientConstructorResolver_name)
				.withMessage(Messages.InefficientConstructorResolver_message)
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

	@Override
	public void addMarkerEvent(ClassInstanceCreation node, SimpleName refactorPrimitiveType,
			Expression refactorCandidateParameter) {
		MethodInvocation newNode = createRepresentingNode(refactorPrimitiveType, refactorCandidateParameter);
		int highlightLength = newNode.toString()
			.length();
		int credit = description.getCredit();
		int offset = node.getStartPosition();
		int length = node.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(node.getStartPosition());
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(Messages.InefficientConstructorResolver_name)
			.withMessage(Messages.InefficientConstructorResolver_message)
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

	private MethodInvocation createRepresentingNode(MethodInvocation node, Expression replaceParameter) {
		AST ast = node.getAST();
		MethodInvocation methodInvocation = (MethodInvocation) ASTNode.copySubtree(ast, node);
		@SuppressWarnings("unchecked")
		List<Expression> arguments = methodInvocation.arguments();
		arguments.clear();
		arguments.add((Expression) ASTNode.copySubtree(ast, replaceParameter));
		return methodInvocation;
	}

	private MethodInvocation createRepresentingNode(SimpleName typeName, Expression replaceParameter) {
		AST ast = replaceParameter.getAST();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName(ReservedNames.MI_VALUE_OF));
		@SuppressWarnings("unchecked")
		List<Expression> arguments = methodInvocation.arguments();
		arguments.add((Expression) ASTNode.copySubtree(ast, replaceParameter));
		SimpleName typeNameCopy = (SimpleName) ASTNode.copySubtree(ast, typeName);
		methodInvocation.setExpression(typeNameCopy);
		return methodInvocation;
	}
}
