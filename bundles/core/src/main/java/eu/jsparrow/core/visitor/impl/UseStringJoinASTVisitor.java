package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UseStringJoinASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation methodInvocation) {


		//1
		SimpleName name = methodInvocation.getName();
		if (!"joining".equals(name.getIdentifier())) {
			return true;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(declaringClass, java.util.stream.Collectors.class.getName())) {
			return true;
		}
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() > 1) {
			return false;
		}

		//2
		StructuralPropertyDescriptor structuralProperty = methodInvocation.getLocationInParent();
		if (structuralProperty != MethodInvocation.ARGUMENTS_PROPERTY) {
			return true;
		}
		MethodInvocation parentMethod = (MethodInvocation) methodInvocation.getParent();

		if (!"collect".equals(parentMethod.getName() //$NON-NLS-1$
			.getIdentifier())) {
			return false;
		}

		IMethodBinding parentMethodBinding = parentMethod.resolveMethodBinding();
		ITypeBinding parentMethodDeclaringClass = parentMethodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(parentMethodDeclaringClass, java.util.stream.Stream.class.getName())) {
			return false;
		}
		List<Expression> collectionArguments = ASTNodeUtil.convertToTypedList(parentMethod.arguments(),
				Expression.class);
		if (collectionArguments.size() > 1) {
			return false;
		}
		Expression collectionExpression = parentMethod.getExpression();
		if (collectionExpression == null) {
			return false;
		}

		if (collectionExpression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return true;
		}

		MethodInvocation stream = (MethodInvocation) collectionExpression;
		/*
		 * Stream arguments needs to be 0 stream expression needs to be a
		 * collection
		 */

		List<Expression> streamArguments = ASTNodeUtil.convertToTypedList(stream.arguments(), Expression.class);
		if (!streamArguments.isEmpty()) {
			return false;
		}

		IMethodBinding streamMethodBinding = stream.resolveMethodBinding();
		ITypeBinding streamDeclaringClass = streamMethodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(streamDeclaringClass, java.util.Collection.class.getName())) {
			return false;
		}

		Expression streamExpression = stream.getExpression();
		if (streamExpression == null) {
			return false;
		}

		ITypeBinding streamExpressionTypeBinding = streamExpression.resolveTypeBinding();
		ITypeBinding collectionErasure = streamExpressionTypeBinding.getErasure();

		if (!(ClassRelationUtil.isContentOfType(collectionErasure, java.util.Collection.class.getName())
				|| ClassRelationUtil.isInheritingContentOfTypes(collectionErasure,
						Collections.singletonList(java.util.Collection.class.getName())))) {
			return false;
		}

		ITypeBinding[] typeParameters = streamExpressionTypeBinding.getTypeArguments();
		/*
		 * The collection erasure needs to be a java.util.Collection The
		 * typeParameters needs to contain a single parameter of type String
		 * 
		 */
		if (typeParameters.length != 1) {
			return true;
		}

		ITypeBinding typeParameter = typeParameters[0];
		if (!ClassRelationUtil.isContentOfType(typeParameter, java.lang.String.class.getName())) {
			return false;
		}

		refactor(parentMethod, streamExpression, arguments);

		return true;
	}

	private void refactor(MethodInvocation parentMethod, Expression collection, List<Expression> joinArguments) {
		AST ast = parentMethod.getAST();
		SimpleName expression = ast.newSimpleName("String");
		SimpleName name = ast.newSimpleName("join");
		MethodInvocation stringJoin = NodeBuilder.newMethodInvocation(ast, expression, name);
		Expression delimiter = joinArguments.isEmpty() ? ast.newStringLiteral()
				: (Expression) astRewrite.createCopyTarget(joinArguments.get(0));
		Expression ietrable = (Expression) astRewrite.createCopyTarget(collection);
		@SuppressWarnings("unchecked")
		List<Expression> stringJoinArguments = stringJoin.arguments();
		stringJoinArguments.add(delimiter);
		stringJoinArguments.add(ietrable);
		astRewrite.replace(parentMethod, stringJoin, null);
		onRewrite();
	}

}
