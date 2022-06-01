package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
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

import eu.jsparrow.core.visitor.loop.stream.StreamForEachCheckValidStatementASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * This visitor checks whether an unhandled checked exception is thrown.
 * 
 * @author Matthias Webhofer, Ardit Ymeri
 * @since 2.1.1
 *
 */
public class UnhandledExceptionVisitorNEW extends ASTVisitor {

	private static final String CLOSE = "close"; //$NON-NLS-1$
	private static final String CHECKED_EXCEPTION_SUPERTYPE = java.lang.Exception.class.getName();
	private static final List<String> CHECKED_EXCEPTION_TYPE_LIST = Collections
		.singletonList(CHECKED_EXCEPTION_SUPERTYPE);

	private final ASTNode excludedAncestor;
	protected boolean containsCheckedException = false;
	protected boolean containsThrowStatement = false;
	protected List<String> currentHandledExceptionsTypes = new LinkedList<>();

	public UnhandledExceptionVisitorNEW(ASTNode excludedAncestor) {
		this.excludedAncestor = excludedAncestor;
	}

	@Override
	public boolean visit(TryStatement tryStatementNode) {
		collectHandledExceptionTypes(tryStatementNode)
			.stream()
			.map(Type::resolveBinding)
			.filter(Objects::nonNull)
			.forEach(exceptionVariableBinding -> currentHandledExceptionsTypes.add(exceptionVariableBinding
				.getQualifiedName()));
		return checkResourcesForAutoCloseException(tryStatementNode);
	}

	private static List<Type> collectHandledExceptionTypes(TryStatement tryStatementNode) {
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

		return exceptionTypes;
	}

	@Override
	public void endVisit(TryStatement tryStatementNode) {
		ASTNodeUtil.convertToTypedList(tryStatementNode.catchClauses(), CatchClause.class)
			.stream()
			.map(catchClause -> catchClause.getException()
				.resolveBinding())
			.filter(Objects::nonNull)
			.forEach(exceptionVariableBinding -> currentHandledExceptionsTypes.remove(exceptionVariableBinding.getType()
				.getQualifiedName()));
	}

	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {
		IMethodBinding methodBinding = methodInvocationNode.resolveMethodBinding();
		return checkForExceptions(methodBinding);

	}

	@Override
	public boolean visit(ClassInstanceCreation classInstanceCreationNode) {
		IMethodBinding methodBinding = classInstanceCreationNode.resolveConstructorBinding();
		return checkForExceptions(methodBinding);
	}

	/**
	 * checks the given method binding for declared exceptions and looks if
	 * those exceptions are handled. if there is an unhandled exception the
	 * {@link StreamForEachCheckValidStatementASTVisitor#containsCheckedException}
	 * property is set, which prevents the enhanced for loop from transforming.
	 * 
	 * @param methodBinding
	 *            the methodBinding for the method invocation to check
	 * @return true, if the visitor should continue with this statement, false
	 *         otherwise.
	 */
	protected boolean checkForExceptions(IMethodBinding methodBinding) {
		if (methodBinding != null) {
			ITypeBinding[] exceptions = methodBinding.getExceptionTypes();
			for (ITypeBinding exception : exceptions) {
				if ((ClassRelationUtil.isInheritingContentOfTypes(exception, CHECKED_EXCEPTION_TYPE_LIST)
						|| ClassRelationUtil.isContentOfTypes(exception, CHECKED_EXCEPTION_TYPE_LIST))
						&& !checkForException(exception)) {
					containsCheckedException = true;
					return false;
				}
			}
		}

		return true;
	}

	private boolean checkForException(ITypeBinding exception) {
		return ClassRelationUtil.isContentOfTypes(exception, currentHandledExceptionsTypes) ||
				ClassRelationUtil.isInheritingContentOfTypes(exception, currentHandledExceptionsTypes);
	}

	protected boolean checkResourcesForAutoCloseException(TryStatement tryStatementNode) {
		List<Expression> resources = ASTNodeUtil.convertToTypedList(tryStatementNode.resources(), Expression.class);
		for (Expression resource : resources) {
			if (!checkResourceForAutoCloseException(resource)) {
				return false;
			}
		}
		return true;
	}

	private boolean checkResourceForAutoCloseException(Expression resource) {
		ITypeBinding typeBinding = resource.resolveTypeBinding();
		IMethodBinding declaredCloseMethod = findDeclaredCloseMethod(typeBinding).orElse(null);
		if (declaredCloseMethod != null) {
			return checkForExceptions(declaredCloseMethod);
		}
		List<ITypeBinding> ancestors = ClassRelationUtil.findAncestors(typeBinding);
		for (ITypeBinding ancestor : ancestors) {
			IMethodBinding closeMethodOfAncestor = findDeclaredCloseMethod(ancestor).orElse(null);
			if (closeMethodOfAncestor != null && checkForExceptions(closeMethodOfAncestor)) {
				return true;
			}
		}
		return false;
	}

	private Optional<IMethodBinding> findDeclaredCloseMethod(ITypeBinding typeBinding) {
		return Arrays.stream(typeBinding.getDeclaredMethods())
			.filter(methodBinding -> methodBinding.getName()
				.equals(CLOSE))
			.filter(methodBinding -> methodBinding.getParameterTypes().length == 0)
			.findFirst();
	}

	@Override
	public boolean visit(ThrowStatement throwStatementNode) {
		if (!ExceptionHandlingAnalyzer.checkThrowStatement(excludedAncestor, throwStatementNode)) {
			containsThrowStatement = true;
		}
		return false;
	}

	public boolean throwsException() {
		return containsCheckedException;
	}
}
