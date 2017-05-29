package at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class StreamForEachCheckValidStatementASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String CHECKED_EXCEPTION_SUPERTYPE = java.lang.Exception.class.getName();
	private static final List<String> CHECKED_EXCEPTION_TYPE_LIST = Collections
			.singletonList(CHECKED_EXCEPTION_SUPERTYPE);

	private List<SimpleName> fieldNames;
	private List<SimpleName> variableNames = new LinkedList<>();
	private List<String> currentHandledExceptionsTypes = new LinkedList<>();

	private boolean containsBreakStatement = false;
	private boolean containsContinueStatement = false;
	private boolean containsReturnStatement = false;
	private boolean containsCheckedException = false;
	private boolean containsInvalidAssignments = false;

	public StreamForEachCheckValidStatementASTVisitor(List<SimpleName> fieldNames) {
		this.fieldNames = fieldNames;
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
		variableNames.add(variableDeclarationFragmentNode.getName());
		return true;
	}

	@Override
	public boolean visit(Assignment assignmentNode) {
		if (containsInvalidAssignments)
			return false;

		Expression expression = assignmentNode.getLeftHandSide();
		if(!isExpressionValidForAssignment(expression)) {
			containsInvalidAssignments = true;
			return false;
		}
		return true;
	}

	@Override
	public boolean visit(PostfixExpression postfixExpressionNode) {
		if (containsInvalidAssignments)
			return false;

		Expression expression = postfixExpressionNode.getOperand();
		if(!isExpressionValidForAssignment(expression)) {
			containsInvalidAssignments = true;
			return false;
		}
		return true;
	}

	@Override
	public boolean visit(PrefixExpression prefixExpressionNode) {
		if (containsInvalidAssignments)
			return false;

		Expression expression = prefixExpressionNode.getOperand();
		if(!isExpressionValidForAssignment(expression)) {
			containsInvalidAssignments = true;
			return false;
		}
		return true;
	}

	private boolean isExpressionValidForAssignment(Expression expression) {

		if (expression instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) expression;
			boolean fieldNameFound = fieldNames.stream().anyMatch(fieldName -> fieldName.getIdentifier().equals(simpleName.getIdentifier()));
			boolean variableNameFound = variableNames.stream()
					.anyMatch(variableName -> variableName.getIdentifier().equals(simpleName.getIdentifier()));
			
//			IBinding binding = simpleName.resolveBinding();
//			if(binding instanceof IVariableBinding) {
//				IVariableBinding variableBinding = (IVariableBinding) binding;
//				IMethodBinding methodBindng = variableBinding.getDeclaringMethod();
//				boolean isField = variableBinding.isField();
//				boolean isEffectivelyFinal = variableBinding.isEffectivelyFinal();
//				System.out.println("asdf");
//			}
			
			return fieldNameFound || variableNameFound;
		}
		else if (expression instanceof QualifiedName) {
			QualifiedName qualifiedName = (QualifiedName) expression;
			return true;
		}
		else if (expression instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) expression;
			return true; // TODO implement properly
		}

		return false;
	}

	public boolean isStatementsValid() {
		return !containsBreakStatement && !containsContinueStatement && !containsReturnStatement
				&& !containsCheckedException && !containsInvalidAssignments;
	}
}
