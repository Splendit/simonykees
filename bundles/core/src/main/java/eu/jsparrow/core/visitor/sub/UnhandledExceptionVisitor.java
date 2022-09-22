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
public class UnhandledExceptionVisitor extends ASTVisitor {
	private final ASTNode excludedAncestor;
	private boolean containsCheckedException = false;

	/**
	 * 
	 * @param nodeToBeVisited
	 *            {@link ASTNode} visited by UnhandledExceptionVisitor to find
	 *            out whether it contains any Exception which are not handled
	 *            properly.
	 * @param excludedAncestor
	 *            excludedAncestor is the {@link ASTNode} inside which it is
	 *            analyzed whether a certain exception can be handled or not.
	 * 
	 * @return true if all possible exceptions can be handled inside the node
	 *         specified by the argument for excludedAncestor, otherwise false.
	 */
	public static boolean analyzeExceptionHandling(ASTNode nodeToBeVisited, ASTNode excludedAncestor) {
		UnhandledExceptionVisitor unhandledExceptionVisitor = new UnhandledExceptionVisitor(excludedAncestor);
		nodeToBeVisited.accept(unhandledExceptionVisitor);
		return !unhandledExceptionVisitor.containsUnhandledException();
	}

	public UnhandledExceptionVisitor(ASTNode excludedAncestor) {
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
			containsCheckedException = true;
			return false;
		}
		return true;
	}

	public boolean containsUnhandledException() {
		return containsCheckedException;
	}
}
