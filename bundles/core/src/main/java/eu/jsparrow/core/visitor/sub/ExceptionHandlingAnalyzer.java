package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.UnionType;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class ExceptionHandlingAnalyzer {

	private static final String CLOSE = "close"; //$NON-NLS-1$
	private static final List<String> TOLERATED_EXCEPTIONS = Collections.unmodifiableList(Arrays.asList(
			java.lang.RuntimeException.class.getName(), java.lang.Error.class.getName()));

	static boolean checkResourcesForAutoCloseException(ASTNode excludedAncestor, TryStatement tryStatementNode) {
		List<Expression> resources = ASTNodeUtil.convertToTypedList(tryStatementNode.resources(), Expression.class);
		for (Expression resource : resources) {
			if (!checkResourceForAutoCloseException(excludedAncestor, resource)) {
				return false;
			}
		}
		return true;
	}

	private static boolean checkResourceForAutoCloseException(ASTNode excludedAncestor, Expression resource) {
		ITypeBinding typeBinding = resource.resolveTypeBinding();
		IMethodBinding declaredCloseMethod = findDeclaredCloseMethod(typeBinding).orElse(null);
		if (declaredCloseMethod != null) {
			return analyzeExceptionHandling(excludedAncestor, resource, declaredCloseMethod);
		}
		List<ITypeBinding> ancestors = ClassRelationUtil.findAncestors(typeBinding);
		for (ITypeBinding ancestor : ancestors) {
			IMethodBinding closeMethodOfAncestor = findDeclaredCloseMethod(ancestor).orElse(null);
			if (closeMethodOfAncestor != null
					&& analyzeExceptionHandling(excludedAncestor, resource, closeMethodOfAncestor)) {
				return true;
			}
		}
		return false;
	}

	static Optional<IMethodBinding> findDeclaredCloseMethod(ITypeBinding typeBinding) {
		return Arrays.stream(typeBinding.getDeclaredMethods())
			.filter(methodBinding -> methodBinding.getName()
				.equals(CLOSE))
			.filter(methodBinding -> methodBinding.getParameterTypes().length == 0)
			.findFirst();
	}

	static boolean checkThrowStatement(ASTNode excludedAncestor, ThrowStatement throwStatement) {
		ITypeBinding exceptionTypeBinding = throwStatement.getExpression()
			.resolveTypeBinding();
		if (exceptionTypeBinding == null) {
			return false;
		}
		if (isToleratedException(exceptionTypeBinding)) {
			return true;
		}
		return analyzeExceptionHandlingRecursively(excludedAncestor, throwStatement, exceptionTypeBinding);
	}

	static boolean checkMethodInvocation(ASTNode excludedAncestor, MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		return analyzeExceptionHandling(excludedAncestor, methodInvocation, methodBinding);
	}

	static boolean checkClassInstanceCreation(ASTNode excludedAncestor,
			ClassInstanceCreation classInstanceCreation) {
		IMethodBinding methodBinding = classInstanceCreation.resolveConstructorBinding();
		if (methodBinding == null) {
			return false;
		}
		return analyzeExceptionHandling(excludedAncestor, classInstanceCreation, methodBinding);
	}

	private static boolean analyzeExceptionHandling(ASTNode excludedAncestor, ASTNode node,
			IMethodBinding methodBinding) {
		ITypeBinding[] exceptions = methodBinding.getExceptionTypes();
		for (ITypeBinding exception : exceptions) {
			if (exception == null) {
				return false;
			}
			if (!isToleratedException(exception)
					&& !analyzeExceptionHandlingRecursively(excludedAncestor, node, exception)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isToleratedException(ITypeBinding exceptionTypeBinding) {
		return ClassRelationUtil.isContentOfTypes(exceptionTypeBinding, TOLERATED_EXCEPTIONS) ||
				ClassRelationUtil.isInheritingContentOfTypes(exceptionTypeBinding, TOLERATED_EXCEPTIONS);

	}

	private static boolean analyzeExceptionHandlingRecursively(ASTNode excludedAncestor, ASTNode node,
			ITypeBinding exceptionTypeBinding) {
		TryStatement tryStatement = findNextTryStatementCatchingExceptions(excludedAncestor, node).orElse(null);
		if (tryStatement == null) {
			return false;
		}
		List<String> currentHandledExceptionsTypes = collectHandledExceptionTypes(tryStatement);

		if (ClassRelationUtil.isContentOfTypes(exceptionTypeBinding, currentHandledExceptionsTypes) ||
				ClassRelationUtil.isInheritingContentOfTypes(exceptionTypeBinding, currentHandledExceptionsTypes)) {
			return true;
		}
		return analyzeExceptionHandlingRecursively(excludedAncestor, tryStatement, exceptionTypeBinding);
	}

	private static Optional<TryStatement> findNextTryStatementCatchingExceptions(ASTNode excludedAncestor,
			final ASTNode nodeInsideExcludedAncestor) {
		ASTNode childNode = nodeInsideExcludedAncestor;
		while (childNode != null) {
			ASTNode parent = childNode.getParent();
			if (parent == excludedAncestor) {
				return Optional.empty();
			}
			if (childNode.getLocationInParent() == TryStatement.BODY_PROPERTY
					|| childNode.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY) {
				return Optional.of((TryStatement) parent);
			}
			childNode = parent;
		}
		return Optional.empty();
	}

	private static List<String> collectHandledExceptionTypes(TryStatement tryStatementNode) {
		List<Type> exceptionTypes = new ArrayList<>();
		ASTNodeUtil.convertToTypedList(tryStatementNode.catchClauses(), CatchClause.class)
			.stream()
			.map(CatchClause::getException)
			.map(SingleVariableDeclaration::getType)
			.forEach(exceptionType -> {
				if (exceptionType.getNodeType() == ASTNode.UNION_TYPE) {
					UnionType unionType = (UnionType) exceptionType;
					exceptionTypes.addAll(ASTNodeUtil.convertToTypedList(unionType.types(), Type.class));
				} else {
					exceptionTypes.add(exceptionType);
				}
			});

		return exceptionTypes.stream()
			.map(Type::resolveBinding)
			.filter(Objects::nonNull)
			.map(ITypeBinding::getQualifiedName)
			.collect(Collectors.toList());
	}

	private ExceptionHandlingAnalyzer() {
		/*
		 * private default constructor to hide implicit public one
		 */
	}

}
