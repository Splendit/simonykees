package at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * visits blocks and checks their validity
 * 
 * @see {@link EnhancedForLoopToStreamForEachASTVisitor}
 * @author Matthias Webhofer
 * @since 1.2
 */
public class StreamForEachCheckValidStatementASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String CHECKED_EXCEPTION_SUPERTYPE = java.lang.Exception.class.getName();
	private static final List<String> CHECKED_EXCEPTION_TYPE_LIST = Collections
			.singletonList(CHECKED_EXCEPTION_SUPERTYPE);

	/*
	 * helper fields
	 */
	private List<SimpleName> variableNames = new LinkedList<>();
	private List<String> currentHandledExceptionsTypes = new LinkedList<>();

	/*
	 * variables for checking validity
	 */
	private boolean containsBreakStatement = false;
	private boolean containsContinueStatement = false;
	private boolean containsReturnStatement = false;
	private boolean containsCheckedException = false;
	private boolean containsThrowStatement = false;
	private List<IVariableBinding> invalidVariables = new LinkedList<>();

	public StreamForEachCheckValidStatementASTVisitor(SimpleName parameter) {
		this.variableNames.add(parameter);
	}

	@Override
	public boolean visit(BreakStatement breakStatementNode) {
		containsBreakStatement = true;
		return false;
	}

	@Override
	public boolean visit(ContinueStatement continueStatementNode) {
		containsContinueStatement = true;
		return false;
	}

	@Override
	public boolean visit(ReturnStatement returnStatementNode) {
		containsReturnStatement = true;
		return false;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {

		/*
		 * check each method invocation for possible exceptions and if they are
		 * caught with a try catch. uncaught exceptions are not allowed within
		 * the consumer lambda.
		 */
		IMethodBinding methodBinding = methodInvocationNode.resolveMethodBinding();
		if (methodBinding != null) {
			ITypeBinding[] exceptions = methodBinding.getExceptionTypes();
			for (ITypeBinding exception : exceptions) {
				if (ClassRelationUtil.isInheritingContentOfTypes(exception, CHECKED_EXCEPTION_TYPE_LIST)
						|| ClassRelationUtil.isContentOfTypes(exception, CHECKED_EXCEPTION_TYPE_LIST)) {
					if (!currentHandledExceptionsTypes.contains(exception.getQualifiedName())) {
						containsCheckedException = true;
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean visit(TryStatement tryStatementNode) {
		ASTNodeUtil.convertToTypedList(tryStatementNode.catchClauses(), CatchClause.class).stream()
				.forEach(catchClause -> {
					IVariableBinding exceptionVariableBinding = catchClause.getException().resolveBinding();
					if (exceptionVariableBinding != null) {
						currentHandledExceptionsTypes.add(exceptionVariableBinding.getType().getQualifiedName());
					}
				});
		return true;
	}

	@Override
	public void endVisit(TryStatement tryStatementNode) {
		ASTNodeUtil.convertToTypedList(tryStatementNode.catchClauses(), CatchClause.class).stream()
				.forEach(catchClause -> {
					IVariableBinding exceptionVariableBinding = catchClause.getException().resolveBinding();
					if (exceptionVariableBinding != null) {
						currentHandledExceptionsTypes.remove(exceptionVariableBinding.getType().getQualifiedName());
					}
				});
	}

	@Override
	public boolean visit(VariableDeclarationFragment variableDeclarationFragmentNode) {
		/*
		 * search local variables
		 */
		variableNames.add(variableDeclarationFragmentNode.getName());
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleNameNode) {
		if (!(simpleNameNode.getParent() instanceof VariableDeclaration)) {

			/*
			 * only local, final or effectively final variables or fields are
			 * allowed.
			 */
			IBinding binding = simpleNameNode.resolveBinding();
			if (binding instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) binding;
				boolean isField = variableBinding.isField();
				boolean isFinal = Modifier.isFinal(variableBinding.getModifiers());
				boolean isEffectivelyFinal = variableBinding.isEffectivelyFinal();
				boolean isLocalVariable = variableNames.stream()
						.anyMatch(var -> var.getIdentifier().equals(simpleNameNode.getIdentifier()));

				if (!(isField || isFinal || isEffectivelyFinal || isLocalVariable))
					invalidVariables.add(variableBinding);
			}
		}
		return false;
	}

	@Override
	public boolean visit(FieldAccess node) {
		return false;
	}

	@Override
	public boolean visit(ThrowStatement throwStatementNode) {
		containsThrowStatement = true;
		return false;
	}

	public boolean isStatementsValid() {
		return !containsBreakStatement && !containsContinueStatement && !containsReturnStatement
				&& !containsCheckedException && !containsThrowStatement && invalidVariables.isEmpty();
	}
}
