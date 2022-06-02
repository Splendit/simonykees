package eu.jsparrow.core.visitor.sub;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;

/**
 * This visitor checks whether an unhandled checked exception is thrown.
 * 
 * @author Matthias Webhofer, Ardit Ymeri
 * @since 2.1.1
 *
 */
public class UnhandledExceptionVisitorNEW extends ASTVisitor {
	private final ASTNode excludedAncestor;
	protected boolean containsCheckedException = false;
	protected boolean containsThrowStatement = false;

	public UnhandledExceptionVisitorNEW(ASTNode excludedAncestor) {
		this.excludedAncestor = excludedAncestor;
	}

	@Override
	public boolean visit(TryStatement tryStatementNode) {
		if (!ExceptionHandlingAnalyzer.checkResourcesForAutoCloseException(excludedAncestor, tryStatementNode)) {
			containsCheckedException = true;
			return false;
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {
		if (!ExceptionHandlingAnalyzer.checkMethodInvocation(excludedAncestor, methodInvocationNode)) {
			containsCheckedException = true;
			return false;
		}
		return true;

	}

	@Override
	public boolean visit(ClassInstanceCreation classInstanceCreationNode) {
		if (!ExceptionHandlingAnalyzer.checkClassInstanceCreation(excludedAncestor, classInstanceCreationNode)) {
			containsCheckedException = true;
			return false;
		}
		return true;
	}

	@Override
	public boolean visit(ThrowStatement throwStatementNode) {
		if (!ExceptionHandlingAnalyzer.checkThrowStatement(excludedAncestor, throwStatementNode)) {
			containsThrowStatement = true;
			return false;
		}
		return true;
	}

	public boolean throwsException() {
		return containsCheckedException;
	}
}
