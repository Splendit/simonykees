package eu.jsparrow.rules.java10.factorymethods;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class AnonymousClassArgumentAnalyser extends ArgumentAnalyser<ClassInstanceCreation> {

	@Override
	public void analyzeArgument(ClassInstanceCreation classInstanceCreation) {
		Type type = classInstanceCreation.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		if (!ClassRelationUtil.isInheritingContentOfTypes(typeBinding, collectionTypes)) {
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

		String insertMethodName = findInsertMethodName(typeBinding);
		int arity = PUT.equals(insertMethodName) ? 2 : 1;
		collectInsertedElements(insertMethodName, arity, bodyStatements);
	}

	private boolean collectInsertedElements(String name, int arity, List<Statement> bodyStatements) {
		List<Expression> collectedElements = new ArrayList<>();
		for (Statement statement : bodyStatements) {
			List<Expression> methodArgments = isInsertStatement(name, arity, statement);
			if (methodArgments.isEmpty()) {
				return false;
			}
			collectedElements.addAll(methodArgments);
		}
		elements = collectedElements;
		return true;
	}

	private List<Expression> isInsertStatement(String name, int arity, Statement statement) {
		if (statement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return Collections.emptyList();
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		Expression expression = expressionStatement.getExpression();
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Collections.emptyList();
		}
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		SimpleName methodName = methodInvocation.getName();
		Expression methodExpression = methodInvocation.getExpression();
		List<Expression> methodArgments = convertToTypedList(methodInvocation.arguments(), Expression.class);
		int methodArity = methodArgments.size();
		if (methodExpression != null || !name.equals(methodName.getIdentifier()) || arity != methodArity) {
			return Collections.emptyList();
		}
		return methodArgments;
	}

}
