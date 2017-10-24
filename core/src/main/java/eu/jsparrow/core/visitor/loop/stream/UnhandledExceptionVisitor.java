package eu.jsparrow.core.visitor.loop.stream;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;

import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.util.ClassRelationUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor checks whether an unhandled checked exception is thrown.
 * 
 * @author Matthias Webhofer, Ardit Ymeri
 * @since 2.1.1
 *
 */
public class UnhandledExceptionVisitor extends AbstractASTRewriteASTVisitor {

	private static final String CHECKED_EXCEPTION_SUPERTYPE = java.lang.Exception.class.getName();
	private static final List<String> CHECKED_EXCEPTION_TYPE_LIST = Collections
		.singletonList(CHECKED_EXCEPTION_SUPERTYPE);

	protected boolean containsCheckedException = false;
	protected boolean containsThrowStatement = false;
	protected List<String> currentHandledExceptionsTypes = new LinkedList<>();

	@Override
	public boolean visit(TryStatement tryStatementNode) {
		ASTNodeUtil.convertToTypedList(tryStatementNode.catchClauses(), CatchClause.class)
			.stream()
			.map(catchClause -> catchClause.getException()
				.resolveBinding())
			.forEach(exceptionVariableBinding -> {
				if (exceptionVariableBinding != null) {
					currentHandledExceptionsTypes.add(exceptionVariableBinding.getType()
						.getQualifiedName());
				}
			});
		return true;
	}

	@Override
	public void endVisit(TryStatement tryStatementNode) {
		ASTNodeUtil.convertToTypedList(tryStatementNode.catchClauses(), CatchClause.class)
			.stream()
			.map(catchClause -> catchClause.getException()
				.resolveBinding())
			.forEach(exceptionVariableBinding -> {
				if (exceptionVariableBinding != null) {
					currentHandledExceptionsTypes.remove(exceptionVariableBinding.getType()
						.getQualifiedName());
				}
			});
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
						&& !currentHandledExceptionsTypes.contains(exception.getQualifiedName())) {
					containsCheckedException = true;
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean visit(ThrowStatement throwStatementNode) {
		containsThrowStatement = true;
		return false;
	}

	public boolean throwsException() {
		return containsCheckedException;
	}
}
