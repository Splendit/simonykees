package eu.jsparrow.core.visitor.junit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class ReplaceExpectedExceptionByAssertThrowsASTVisitor extends AbstractAddImportASTVisitor {
	
	private static final String ORG_JUNIT_TEST = "org.junit.Test";
	private static final String EXCEPTION_TYPE_NAME = java.lang.Exception.class.getName();
	
	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if(continueVisiting) {
			verifyStaticMethodImport(compilationUnit, "org.junit.Assert.assertThrows");
		}
		return continueVisiting;
	}
	
	
	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		// check for @Test
		List<MarkerAnnotation> annotations = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(), MarkerAnnotation.class);
		if(annotations.size() != 1) {
			return false;
		}
		MarkerAnnotation annotation = annotations.get(0);
		Name typeName = annotation.getTypeName();
		ITypeBinding annotationTypeBinding = typeName.resolveTypeBinding();
		if(!ClassRelationUtil.isContentOfType(annotationTypeBinding, ORG_JUNIT_TEST)) {
			return true;
		}
		
		Block body = methodDeclaration.getBody();
		ExpectedExceptionVisitor visitor = new ExpectedExceptionVisitor();
		body.accept(visitor);
		
		List<MethodInvocation> expectExceptionsInvocations = visitor.getExpectExceptionInvocations();
		if(expectExceptionsInvocations.size() !=1) {
			return true;
		}
		MethodInvocation expectExceptionInvocation = expectExceptionsInvocations.get(0);
		if(expectExceptionInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}
		
		List<Expression> expectedExceptions = visitor.getExpectedExceptionsTypes();
		if(expectedExceptions.size() != 1) {
			return true;
		}
		Expression expectedException = expectedExceptions.get(0);
		ITypeBinding exceptionType = findExceptionTypeArgument(expectedException).orElse(null);
		if(exceptionType == null) {
			return true;
		}
		 
		ExpressionsThrowingExceptionVisitor expressionsThrowingExceptionVisitor = new ExpressionsThrowingExceptionVisitor(exceptionType);
		body.accept(expressionsThrowingExceptionVisitor);
		List<ASTNode> nodesThrowingException = expressionsThrowingExceptionVisitor.getNodesThrowingExpectedException();
		if(nodesThrowingException.size() != 1) {
			return true;
		}
		ASTNode nodeThrowingException = nodesThrowingException.get(0);
		if(nodeThrowingException.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}
		Expression expressionThrowingException = (Expression)nodeThrowingException;
		AST ast = methodDeclaration.getAST();
		//TODO: use a fully qualified name as an expression if the static import cannot be added. 
		Name name = addImportForStaticMethod("org.junit.Assert.assertThrows", methodDeclaration)
				.orElse(ast.newName("org.junit.Assert.assertThrows"));
		MethodInvocation assertThrows = ast.newMethodInvocation();
		assertThrows.setName(ast.newSimpleName("assertThrows"));
		Expression firstArg = (Expression) astRewrite.createCopyTarget(expectedException);
		LambdaExpression lambdaExpression = ast.newLambdaExpression();
		lambdaExpression.setBody(astRewrite.createCopyTarget(nodeThrowingException));
		List assertionArguments = assertThrows.arguments();
		assertionArguments.add(firstArg);
		assertionArguments.add(lambdaExpression);
		ExpressionStatement assertionStatement = ast.newExpressionStatement(assertThrows);
		
		astRewrite.replace(nodeThrowingException, assertionStatement, null);
		astRewrite.remove(expectExceptionInvocation.getParent(), null);
		onRewrite();
		
		
		
		// Make a helper visitor to find expectedException.expect(class)
		// make sure the last statement throws that exception 
		// 

		// check for expectedException.expect()
		// make sure the transformation is feasible: no duplicated expect, no statement after expect, etc..
		// generate transformation 
		
		// verify the positioning. 
		
		return false;
	}
	
	private Optional<ITypeBinding> findExceptionTypeArgument(Expression excpetionClass) {
		ITypeBinding argumentType = excpetionClass.resolveTypeBinding();
		if (argumentType.isParameterizedType()) {
			ITypeBinding[] typeArguments = argumentType.getTypeArguments();
			if(typeArguments.length == 1) {
				ITypeBinding typeArgument = typeArguments[0];
				boolean isException = ClassRelationUtil.isContentOfType(typeArgument, EXCEPTION_TYPE_NAME)
						|| ClassRelationUtil.isInheritingContentOfTypes(typeArgument, Collections.singletonList(EXCEPTION_TYPE_NAME));
				if(isException) {
					return Optional.of(typeArgument);
				}
			}
		}
		
		return Optional.empty();
	}

}
