package eu.jsparrow.core.visitor.junit;

import java.util.Arrays;
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
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class ReplaceExpectedExceptionByAssertThrowsASTVisitor extends AbstractAddImportASTVisitor {

	private static final String ORG_JUNIT_JUPITER_API_TEST = "org.junit.jupiter.api.Test"; //$NON-NLS-1$
	private static final String ASSERT_THROWS = "assertThrows"; //$NON-NLS-1$
	private static final String ORG_JUNIT_ASSERT_ASSERT_THROWS = "org.junit.Assert.assertThrows"; //$NON-NLS-1$
	private static final String ORG_JUNIT_TEST = "org.junit.Test"; //$NON-NLS-1$
	private static final String EXCEPTION_TYPE_NAME = java.lang.Exception.class.getName();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyStaticMethodImport(compilationUnit, ORG_JUNIT_ASSERT_ASSERT_THROWS);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {

		List<MarkerAnnotation> annotations = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(),
				MarkerAnnotation.class);
		if (annotations.size() != 1) {
			return false;
		}
		MarkerAnnotation annotation = annotations.get(0);
		Name typeName = annotation.getTypeName();
		ITypeBinding annotationTypeBinding = typeName.resolveTypeBinding();
		ClassRelationUtil.isContentOfTypes(annotationTypeBinding,
				Arrays.asList(ORG_JUNIT_JUPITER_API_TEST, ORG_JUNIT_TEST));
		if (!ClassRelationUtil.isContentOfType(annotationTypeBinding, ORG_JUNIT_TEST)) {
			return true;
		}

		Block body = methodDeclaration.getBody();
		ExpectedExceptionVisitor visitor = new ExpectedExceptionVisitor();
		body.accept(visitor);
		
		if(visitor.hasUnsupportedMethods() || visitor.hasUnresolvedInvocations()) {
			return true;
		}
		
		if(!visitor.hasUniqueExpectedExceptionRule()) {
			return true;
		}

		List<MethodInvocation> expectExceptionsInvocations = visitor.getExpectExceptionInvocations();
		if (expectExceptionsInvocations.size() != 1) {
			return true;
		}
		MethodInvocation expectExceptionInvocation = expectExceptionsInvocations.get(0);
		if (expectExceptionInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}

		List<Expression> expectedExceptions = visitor.getExpectedExceptionsTypes();
		if (expectedExceptions.size() != 1) {
			return true;
		}
		Expression expectedException = expectedExceptions.get(0);
		ITypeBinding exceptionType = findExceptionTypeArgument(expectedException).orElse(null);
		if (exceptionType == null) {
			return true;
		}

		ExpressionsThrowingExceptionVisitor throwingExceptionsVisitor = new ExpressionsThrowingExceptionVisitor(
				exceptionType);
		body.accept(throwingExceptionsVisitor);
		List<ASTNode> nodesThrowingException = throwingExceptionsVisitor.getNodesThrowingExpectedException();
		if (nodesThrowingException.size() != 1) {
			return true;
		}
		ASTNode nodeThrowingException = nodesThrowingException.get(0);
		if (nodeThrowingException.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY
				&& nodeThrowingException.getLocationInParent() != ThrowStatement.EXPRESSION_PROPERTY) {
			return true;
		}

		boolean isLastStatement = verifyPosition(methodDeclaration, nodeThrowingException);
		if (!isLastStatement) {
			return true;
		}

		refactor(methodDeclaration, expectExceptionInvocation, expectedException, nodeThrowingException);

		/*
		 * TODO: 0. Make sure there is no other expctExcetion.___
		 * invocation/usage. 1. all the expressions of
		 * expect/expectMessage/expectCause match with each other. 2. replace
		 * all expectMessage by expect assertions. 3. replace all expectCause by
		 * assertions 4. make more unit tests. 5. run the rule in opensource
		 * projects.
		 */

		return false;
	}

	private boolean verifyPosition(MethodDeclaration methodDeclaration, ASTNode nodeThrowingException) {
		Block testBody = methodDeclaration.getBody();
		List<Statement> statements = ASTNodeUtil.convertToTypedList(testBody.statements(), Statement.class);
		Statement lastStatement = statements.get(statements.size() - 1);
		return lastStatement == nodeThrowingException.getParent();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void refactor(MethodDeclaration methodDeclaration, MethodInvocation expectExceptionInvocation,
			Expression expectedException, ASTNode nodeThrowingException) {
		AST ast = methodDeclaration.getAST();
		Optional<Name> qualifiedPrefix = addImportForStaticMethod(ORG_JUNIT_ASSERT_ASSERT_THROWS, methodDeclaration);
		MethodInvocation assertThrows = ast.newMethodInvocation();
		assertThrows.setName(ast.newSimpleName(ASSERT_THROWS));
		qualifiedPrefix.ifPresent(assertThrows::setExpression);
		Expression firstArg = (Expression) astRewrite.createCopyTarget(expectedException);
		LambdaExpression lambdaExpression = ast.newLambdaExpression();
		ASTNode lambdaBody = createThrowRunnable(nodeThrowingException);
		lambdaExpression.setBody(lambdaBody);
		List assertionArguments = assertThrows.arguments();
		assertionArguments.add(firstArg);
		assertionArguments.add(lambdaExpression);
		ExpressionStatement assertionStatement = ast.newExpressionStatement(assertThrows);

		astRewrite.replace(nodeThrowingException.getParent(), assertionStatement, null);
		astRewrite.remove(expectExceptionInvocation.getParent(), null);
		onRewrite();
	}

	@SuppressWarnings("unchecked")
	private ASTNode createThrowRunnable(ASTNode nodeThrowingException) {
		if (nodeThrowingException.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
			return astRewrite.createCopyTarget(nodeThrowingException);
		} else {
			AST ast = nodeThrowingException.getAST();
			Block body = ast.newBlock();
			@SuppressWarnings("rawtypes")
			List statements = body.statements();
			statements.add(astRewrite.createCopyTarget(nodeThrowingException.getParent()));
			return body;
		}

	}

	private Optional<ITypeBinding> findExceptionTypeArgument(Expression excpetionClass) {
		ITypeBinding argumentType = excpetionClass.resolveTypeBinding();
		if (argumentType.isParameterizedType()) {
			ITypeBinding[] typeArguments = argumentType.getTypeArguments();
			if (typeArguments.length == 1) {
				ITypeBinding typeArgument = typeArguments[0];
				boolean isException = ClassRelationUtil.isContentOfType(typeArgument, EXCEPTION_TYPE_NAME)
						|| ClassRelationUtil.isInheritingContentOfTypes(typeArgument,
								Collections.singletonList(EXCEPTION_TYPE_NAME));
				if (isException) {
					return Optional.of(typeArgument);
				}
			}
		}

		return Optional.empty();
	}

}
