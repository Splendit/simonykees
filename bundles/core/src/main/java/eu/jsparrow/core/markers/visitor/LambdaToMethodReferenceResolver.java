package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeMethodReference;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.LambdaToMethodReferenceRule;
import eu.jsparrow.core.visitor.impl.LambdaToMethodReferenceASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * A visitor for resolving one issue of type
 * {@link LambdaToMethodReferenceASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public class LambdaToMethodReferenceResolver extends LambdaToMethodReferenceASTVisitor {

	public static final String ID = LambdaToMethodReferenceResolver.class.getName();

	private IJavaElement javaElement;
	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public LambdaToMethodReferenceResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
				.findByRuleId(LambdaToMethodReferenceRule.RULE_ID);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(LambdaExpression lambdaExpressionNode) {
		if (positionChecker.test(lambdaExpressionNode)) {
			return super.visit(lambdaExpressionNode);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(LambdaExpression lambdaExpressionNode, Expression refExpression, SimpleName name) {
		ExpressionMethodReference newNode = createNodeRepresentation(refExpression, name);
		int highlightLenght = newNode.toString()
			.length();
		int credit = description.getCredit();
		RefactoringEventImpl event = new RefactoringEventImpl(ID, Messages.LambdaToMethodReferenceResolver_name,
				Messages.LambdaToMethodReferenceResolver_message,
				javaElement, highlightLenght, lambdaExpressionNode, newNode, credit);
		addMarkerEvent(event);
	}

	@Override
	public void addMarkerEvent(LambdaExpression lambdaExpressionNode, Type classInstanceCreationType) {
		CreationReference newNode = createNodeRepresentation(classInstanceCreationType);
		int highlightLenght = newNode.toString()
			.length();
		int credit = description.getCredit();
		RefactoringEventImpl event = new RefactoringEventImpl(ID, Messages.LambdaToMethodReferenceResolver_name,
				Messages.LambdaToMethodReferenceResolver_message, javaElement, highlightLenght,
				lambdaExpressionNode, newNode, credit);
		addMarkerEvent(event);
	}

	@Override
	public void addMarkerEvent(LambdaExpression lambdaExpressionNode, Type representingType, SimpleName methodName) {
		TypeMethodReference newNode = createRepresentingNode(representingType, methodName);
		int highlightLenght = newNode.toString()
			.length();
		int credit = description.getCredit();
		RefactoringEventImpl event = new RefactoringEventImpl(ID, Messages.LambdaToMethodReferenceResolver_name,
				Messages.LambdaToMethodReferenceResolver_message, javaElement,
				highlightLenght, lambdaExpressionNode, newNode, credit);
		addMarkerEvent(event);

	}

	private ExpressionMethodReference createNodeRepresentation(Expression expression, SimpleName name) {
		AST ast = name.getAST();
		ExpressionMethodReference ref = ast.newExpressionMethodReference();
		Expression expressionCopy = (Expression) ASTNode.copySubtree(ast, expression);
		SimpleName nameCopey = (SimpleName) ASTNode.copySubtree(ast, name);
		ref.setExpression(expressionCopy);
		ref.setName(nameCopey);
		return ref;
	}

	private CreationReference createNodeRepresentation(Type classInstanceCreationType) {
		AST ast = classInstanceCreationType.getAST();
		CreationReference creationReference = ast.newCreationReference();
		creationReference.setType((Type) ASTNode.copySubtree(ast, classInstanceCreationType));
		return creationReference;
	}

	private TypeMethodReference createRepresentingNode(Type representingType, SimpleName methodName) {
		AST ast = methodName.getAST();
		TypeMethodReference typeMethodReference = ast.newTypeMethodReference();
		Type newType = (Type) ASTNode.copySubtree(ast, representingType);
		SimpleName newName = (SimpleName) ASTNode.copySubtree(ast, methodName);
		typeMethodReference.setType(newType);
		typeMethodReference.setName(newName);
		return typeMethodReference;
	}
}
