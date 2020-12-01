package eu.jsparrow.core.visitor.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class ExpressionsThrowingExceptionVisitor extends ASTVisitor {

	private ITypeBinding exceptionType;
	private List<ASTNode> nodesThrowingExpectedException = new ArrayList<>();

	public ExpressionsThrowingExceptionVisitor(ITypeBinding exceptionType) {
		this.exceptionType = exceptionType;
	}

	@Override
	public boolean visit(ClassInstanceCreation instanceCreation) {
		IMethodBinding constructorBinding = instanceCreation.resolveConstructorBinding();
		boolean throwsException = verifyThrownExceptions(constructorBinding);
		if (throwsException) {
			nodesThrowingExpectedException.add(instanceCreation);
		}
		return true;
	}

	@Override
	public boolean visit(ThrowStatement throwStatement) {
		Expression expression = throwStatement.getExpression();
		String qualifiedExceptionName = exceptionType.getQualifiedName();
		ITypeBinding thrownType = expression.resolveTypeBinding();
		boolean throwsException = ClassRelationUtil.isContentOfType(thrownType, qualifiedExceptionName)
				|| ClassRelationUtil.isInheritingContentOfTypes(thrownType,
						Collections.singletonList(qualifiedExceptionName));
		if (throwsException) {
			nodesThrowingExpectedException.add(throwStatement.getExpression());
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		boolean throwsExpectedException = verifyThrownExceptions(methodBinding);
		if (throwsExpectedException) {
			nodesThrowingExpectedException.add(methodInvocation);
		}
		return true;
	}

	private boolean verifyThrownExceptions(IMethodBinding methodBinding) {
		ITypeBinding[] exceptionTypes = methodBinding.getExceptionTypes();
		String qualifiedExceptionName = exceptionType.getQualifiedName();
		return Arrays.stream(exceptionTypes)
			.anyMatch(exception -> ClassRelationUtil.isContentOfType(exception, qualifiedExceptionName)
					|| ClassRelationUtil.isInheritingContentOfTypes(exception,
							Collections.singletonList(qualifiedExceptionName)));
	}

	public List<ASTNode> getNodesThrowingExpectedException() {
		return nodesThrowingExpectedException;
	}
}
