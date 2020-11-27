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

	public List<ASTNode> getNodesThrowingExpectedException() {
		return nodesThrowingExpectedException;
	}

	public ExpressionsThrowingExceptionVisitor(ITypeBinding exceptionType) {
		this.exceptionType = exceptionType;
	}
	
	public boolean visit(ClassInstanceCreation instanceCreation) {
		IMethodBinding constructorBinding = instanceCreation.resolveConstructorBinding();
		// TODO: check if the constructor binding throws the expected exception 
		return true; 
	}
	
	public boolean visit(ThrowStatement throwStatement) {
		Expression expression = throwStatement.getExpression();
		ITypeBinding thrownType = expression.resolveTypeBinding();
		//TODO: check if the thrown type is a subtype or matches the expectedException type.
		
		return true;
	}
 
	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding[] exceptionTypes = methodBinding.getExceptionTypes();

		String qualifiedExceptionName = exceptionType.getQualifiedName();

		boolean throwsExpectedException = Arrays.stream(exceptionTypes)
			.anyMatch(exception -> ClassRelationUtil.isContentOfType(exception, qualifiedExceptionName)
					|| ClassRelationUtil.isInheritingContentOfTypes(exception,
							Collections.singletonList(qualifiedExceptionName)));
		if(throwsExpectedException) {
			nodesThrowingExpectedException.add(methodInvocation);
		}
		
		return true;
		
	}
}
