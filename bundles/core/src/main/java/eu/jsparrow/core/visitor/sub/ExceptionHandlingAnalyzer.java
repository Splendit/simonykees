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
	private static final String CHECKED_EXCEPTION_SUPERTYPE = java.lang.Exception.class.getName();
	private static final List<String> CHECKED_EXCEPTION_SUPERTYPE_LIST = Collections
		.singletonList(CHECKED_EXCEPTION_SUPERTYPE);

	private static final String RUNTIME_EXCEPTION = java.lang.RuntimeException.class.getName();
	private static final List<String> RUNTIME_EXCEPTION_LIST = Collections.singletonList(RUNTIME_EXCEPTION);

	public static boolean checkResourcesForAutoCloseException(ASTNode excludedAncestor, TryStatement tryStatementNode) {
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

	private static Optional<IMethodBinding> findDeclaredCloseMethod(ITypeBinding typeBinding) {
		return Arrays.stream(typeBinding.getDeclaredMethods())
			.filter(methodBinding -> methodBinding.getName()
				.equals(CLOSE))
			.filter(methodBinding -> methodBinding.getParameterTypes().length == 0)
			.findFirst();
	}

	public static boolean checkThrowStatement(ASTNode excludedAncestor, ThrowStatement throwStatement) {
		ITypeBinding exceptionTypeBinding = throwStatement.getExpression()
			.resolveTypeBinding();
		if (exceptionTypeBinding == null) {
			return false;
		}
		if (!isCheckedExceptionToBeHandled(exceptionTypeBinding)) {
			return true;
		}
		return analyzeExceptionHandlingRecursively(excludedAncestor, throwStatement, exceptionTypeBinding);
	}

	public static boolean checkMethodInvocation(ASTNode excludedAncestor, MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		return analyzeExceptionHandling(excludedAncestor, methodInvocation, methodBinding);
	}

	public static boolean checkClassInstanceCreation(ASTNode excludedAncestor,
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
			if (isCheckedExceptionToBeHandled(exception)
					&& !analyzeExceptionHandlingRecursively(excludedAncestor, node, exception)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isCheckedExceptionToBeHandled(ITypeBinding exceptionTypeBinding) {

		if (ClassRelationUtil.isContentOfType(exceptionTypeBinding, RUNTIME_EXCEPTION) ||
				ClassRelationUtil.isInheritingContentOfTypes(exceptionTypeBinding, RUNTIME_EXCEPTION_LIST)) {
			return false;
		}

		return ClassRelationUtil.isContentOfType(exceptionTypeBinding, CHECKED_EXCEPTION_SUPERTYPE) ||
				ClassRelationUtil.isInheritingContentOfTypes(exceptionTypeBinding, CHECKED_EXCEPTION_SUPERTYPE_LIST);

	}

	private static boolean analyzeExceptionHandlingRecursively(ASTNode excludedAncestor, ASTNode node,
			ITypeBinding exceptionTypeBinding) {
		TryStatement tryStatement = findNextTryStatrementCatchingExceptions(excludedAncestor, node).orElse(null);
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

	private static Optional<TryStatement> findNextTryStatrementCatchingExceptions(ASTNode excludedAncestor,
			ASTNode node) {
		ASTNode parent = node;
		while (parent != null && parent != excludedAncestor) {
			if (parent.getLocationInParent() == TryStatement.BODY_PROPERTY
					|| parent.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY) {
				return Optional.of((TryStatement) parent.getParent());
			}
			parent = parent.getParent();
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
