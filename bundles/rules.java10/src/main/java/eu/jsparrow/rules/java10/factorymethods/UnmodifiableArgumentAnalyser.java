package eu.jsparrow.rules.java10.factorymethods;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class UnmodifiableArgumentAnalyser {
	private List<Expression> elements;

	public UnmodifiableArgumentAnalyser(Expression argument) {

		if (argument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation anonymousClass = (ClassInstanceCreation) argument;
			/*
			 * Check for: Any implementation of List/Set/Map having a static
			 * initializer consisting only of add() or put methods.
			 * 
			 * Extract a list of entries for the factory method
			 */
			analyzeAnonymousClassArgument(anonymousClass);
		} else if (argument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation argumentMethod = (MethodInvocation) argument;
			analyzeMethodInvocationArgument(argumentMethod);

		} else if (argument.getNodeType() == ASTNode.SIMPLE_NAME) {
			/*
			 * Check for: A fresh declaration of a collection followed by
			 * inserting elements. Keep track of all statements followed
			 * immediately by an assignment statement where this method
			 * invocation is the RHS
			 * 
			 * Extract the list of the inserted elements
			 */
		}
	}

	private void analyzeAnonymousClassArgument(ClassInstanceCreation classInstanceCreation) {
		Type type = classInstanceCreation.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		List<String> setsAndLists = Arrays.asList(java.util.List.class.getName(), java.util.Set.class.getName(),
				java.util.Map.class.getName());
		if (!ClassRelationUtil.isInheritingContentOfTypes(typeBinding, setsAndLists)) {
			return;
		}
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		List<BodyDeclaration> bodyDeclarations = convertToTypedList(anonymousClassDeclaration.bodyDeclarations(),
				BodyDeclaration.class);
		if (bodyDeclarations.size() != 1) {
			return;
		}
		BodyDeclaration declaration = bodyDeclarations.get(0);
		if (declaration.getNodeType() != ASTNode.INITIALIZER) {
			return;
		}

		Initializer initializer = (Initializer) declaration;
		Block body = initializer.getBody();
		List<Statement> bodyStatements = convertToTypedList(body.statements(), Statement.class);

		if (!collectInsertedElements("add", 1, bodyStatements)) { //$NON-NLS-1$
			collectInsertedElements("put", 2, bodyStatements); //$NON-NLS-1$
		}
	}

	private boolean collectInsertedElements(String name, int arity, List<Statement> bodyStatements) {
		List<Expression> collectedElements = new ArrayList<>();
		for (Statement statement : bodyStatements) {
			if (statement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
				return false;
			}
			ExpressionStatement expressionStatement = (ExpressionStatement) statement;
			Expression expression = expressionStatement.getExpression();
			if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
				return false;
			}
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			SimpleName methodName = methodInvocation.getName();
			Expression methodExpression = methodInvocation.getExpression();
			List<Expression> methodArgments = convertToTypedList(methodInvocation.arguments(), Expression.class);
			int methodArity = methodArgments.size();
			if (methodExpression != null || !name.equals(methodName.getIdentifier()) || arity != methodArity) {
				return false;
			}
			collectedElements.addAll(methodArgments);

		}
		this.elements = collectedElements;
		return true;
	}

	private void analyzeMethodInvocationArgument(MethodInvocation argumentMethod) {
		/*
		 * Check for: Arrays.asList(...)
		 * 
		 * Extract a list of entries for the factory method
		 */
		IMethodBinding methodBinding = argumentMethod.resolveMethodBinding();

		if ("asList".equals(methodBinding.getName()) && ClassRelationUtil //$NON-NLS-1$
			.isContentOfType(methodBinding.getDeclaringClass(), java.util.Arrays.class.getName())) {
			elements = convertToTypedList(argumentMethod.arguments(), Expression.class);
		}
	}

	public List<Expression> getElements() {
		return elements;
	}

}
