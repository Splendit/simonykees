package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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

/**
 * Replaces stream {@link Collectors} that are used for concatenating the values
 * of a collection with {@link StringJoiner}s. For example, the following code:
 * 
 * <pre>
 *  {@code collection.stream().collect(Collectors.joining(","))}
 * </pre>
 * 
 * will be transformed to:
 * 
 * <pre>
 * {@code String.join(",", collection)}
 * </pre>
 * 
 * @since 3.15.0
 *
 */
public class UseStringJoinASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		if (!isCollectionJoining(methodInvocation)) {
			return true;
		}

		MethodInvocation parentMethod = findParentCollectInvocation(methodInvocation).orElse(null);
		if (parentMethod == null) {
			return true;
		}
		MethodInvocation stream = findStreamInvocation(parentMethod).orElse(null);
		if (stream == null) {
			return true;
		}

		Expression streamExpression = stream.getExpression();
		if (streamExpression == null) {
			return true;
		}

		if (!analyzeStreamExpression(streamExpression)) {
			return true;
		}

		refactor(parentMethod, streamExpression,
				ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class));
		return true;
	}

	private boolean analyzeStreamExpression(Expression streamExpression) {
		ITypeBinding streamExpressionTypeBinding = streamExpression.resolveTypeBinding();
		ITypeBinding collectionErasure = streamExpressionTypeBinding.getErasure();

		if (!ClassRelationUtil.isContentOfType(collectionErasure, java.util.Collection.class.getName())
				&& !ClassRelationUtil.isInheritingContentOfTypes(collectionErasure,
						Collections.singletonList(java.util.Collection.class.getName()))) {
			return false;
		}

		ITypeBinding[] typeParameters = streamExpressionTypeBinding.getTypeArguments();
		if (typeParameters.length != 1) {
			return false;
		}

		ITypeBinding typeParameter = typeParameters[0];
		return ClassRelationUtil.isContentOfType(typeParameter, java.lang.String.class.getName());
	}

	private Optional<MethodInvocation> findStreamInvocation(MethodInvocation parentMethod) {
		Expression collectionExpression = parentMethod.getExpression();
		if (collectionExpression == null) {
			return Optional.empty();
		}

		if (collectionExpression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}

		MethodInvocation stream = (MethodInvocation) collectionExpression;
		List<Expression> streamArguments = ASTNodeUtil.convertToTypedList(stream.arguments(), Expression.class);
		if (!streamArguments.isEmpty()) {
			return Optional.empty();
		}

		IMethodBinding streamMethodBinding = stream.resolveMethodBinding();
		ITypeBinding streamDeclaringClass = streamMethodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(streamDeclaringClass, java.util.Collection.class.getName())) {
			return Optional.empty();
		}
		return Optional.of(stream);
	}

	private Optional<MethodInvocation> findParentCollectInvocation(MethodInvocation methodInvocation) {
		StructuralPropertyDescriptor structuralProperty = methodInvocation.getLocationInParent();
		if (structuralProperty != MethodInvocation.ARGUMENTS_PROPERTY) {
			return Optional.empty();
		}
		MethodInvocation parentMethod = (MethodInvocation) methodInvocation.getParent();

		if (!"collect".equals(parentMethod.getName() //$NON-NLS-1$
			.getIdentifier())) {
			return Optional.empty();
		}

		IMethodBinding parentMethodBinding = parentMethod.resolveMethodBinding();
		ITypeBinding parentMethodDeclaringClass = parentMethodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(parentMethodDeclaringClass, java.util.stream.Stream.class.getName())) {
			return Optional.empty();
		}
		List<Expression> collectionArguments = ASTNodeUtil.convertToTypedList(parentMethod.arguments(),
				Expression.class);
		if (collectionArguments.size() > 1) {
			return Optional.empty();
		}
		return Optional.of(parentMethod);
	}

	private boolean isCollectionJoining(MethodInvocation methodInvocation) {
		SimpleName name = methodInvocation.getName();
		if (!"joining".equals(name.getIdentifier())) { //$NON-NLS-1$
			return false;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(declaringClass, java.util.stream.Collectors.class.getName())) {
			return false;
		}
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		return arguments.size() <= 1;
	}

	private void refactor(MethodInvocation parentMethod, Expression collection, List<Expression> joinArguments) {
		AST ast = parentMethod.getAST();
		SimpleName expression = ast.newSimpleName("String"); //$NON-NLS-1$
		SimpleName name = ast.newSimpleName("join"); //$NON-NLS-1$
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
