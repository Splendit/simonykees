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
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class ReplaceExpectedExceptionByAssertThrowsASTVisitor extends AbstractAddImportASTVisitor {

	private static final String GET_MESSAGE = "getMessage"; //$NON-NLS-1$
	private static final String ASSERT_TRUE = "assertTrue"; //$NON-NLS-1$
	private static final String ORG_HAMCREST_MATCHER = "org.hamcrest.Matcher"; //$NON-NLS-1$
	private static final String ORG_JUNIT_ASSERT_ASSERT_TRUE = "org.junit.Assert.assertTrue"; //$NON-NLS-1$
	private static final String ORG_HAMCREST_MATCHER_ASSERT_ASSERT_THAT = "org.hamcrest.MatcherAssert.assertThat"; //$NON-NLS-1$
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
			verifyStaticMethodImport(compilationUnit, ORG_JUNIT_ASSERT_ASSERT_TRUE);
			verifyStaticMethodImport(compilationUnit, ORG_HAMCREST_MATCHER_ASSERT_ASSERT_THAT);
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

		if (visitor.hasUnsupportedMethods() || visitor.hasUnresolvedInvocations()) {
			return true;
		}

		if (!visitor.hasUniqueExpectedExceptionRule()) {
			return true;
		}

		List<MethodInvocation> expectExceptionsInvocations = visitor.getExpectExceptionInvocations();
		if (expectExceptionsInvocations.size() != 1) {
			return true;
		}
		
		if(!visitor.verifyExpectCauseMatchers()) {
			return true;
		}
		
		MethodInvocation expectExceptionInvocation = expectExceptionsInvocations.get(0);
		if (expectExceptionInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}

		List<Expression> expectedExceptions = visitor.getExpectedExceptionsExpression();
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

		refactor(methodDeclaration, expectExceptionInvocation, expectedException, nodeThrowingException, visitor);
		/*
		 * TODO: 0. Make sure there is no other expctExcetion invocation/usage.
		 * 1. all the expressions of expect/expectMessage/expectCause match with
		 * each other. 2. replace all expectMessage by expect assertions. 3.
		 * replace all expectCause by assertions 4. make more unit tests. 5. run
		 * the rule in opensource projects.
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
			Expression expectedException, ASTNode nodeThrowingException, ExpectedExceptionVisitor visitor) {

		List<Expression> expectedMessages = visitor
			.getExpectedMessages(mi -> visitor.hasSingleParameterOfType(mi, java.lang.String.class.getName()));
		List<Expression> expectedMessageMatchers = visitor
			.getExpectedMessages(mi -> visitor.hasSingleParameterOfType(mi, ORG_HAMCREST_MATCHER));
		List<Expression> expectedCauseMatchers = visitor.getExpectedCauses();

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

		if (expectedMessages.isEmpty() && expectedMessageMatchers.isEmpty() && expectedCauseMatchers.isEmpty()) {
			ExpressionStatement assertionStatement = ast.newExpressionStatement(assertThrows);
			astRewrite.replace(nodeThrowingException.getParent(), assertionStatement, null);
			astRewrite.remove(expectExceptionInvocation.getParent(), null);
			onRewrite();
		} else {
			VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
			fragment.setInitializer(assertThrows);
			//TODO: make sure exception is a valid name
			fragment.setName(ast.newSimpleName("exception"));
			VariableDeclarationStatement exceptionDeclaration = ast.newVariableDeclarationStatement(fragment);
			// TODO:get the exception type as a parameter or resolve it here, and use the a specific exception type. 
			Type type = ast.newSimpleType(ast.newSimpleName("Exception"));
			exceptionDeclaration.setType(type);
			
			astRewrite.replace(nodeThrowingException.getParent(), exceptionDeclaration, null);
			astRewrite.remove(expectExceptionInvocation.getParent(), null);
			ListRewrite rewriter = astRewrite.getListRewrite(methodDeclaration.getBody(), Block.STATEMENTS_PROPERTY);
			for(Expression expectedMmessage : expectedMessages) {
				MethodInvocation assertTrue = ast.newMethodInvocation();
				Optional<Name> qualifier = addImportForStaticMethod(ORG_JUNIT_ASSERT_ASSERT_TRUE, methodDeclaration);
				qualifier.ifPresent(assertTrue::setExpression);
				assertTrue.setName(ast.newSimpleName(ASSERT_TRUE));
				MethodInvocation getMessage = ast.newMethodInvocation();
				getMessage.setName(ast.newSimpleName(GET_MESSAGE));
				//FIXME use the one from above
				getMessage.setExpression(ast.newSimpleName("exception"));
				MethodInvocation contains = ast.newMethodInvocation();
				contains.setName(ast.newSimpleName("contains"));
				contains.setExpression(getMessage);
				contains.arguments().add(astRewrite.createCopyTarget(expectedMmessage));
				assertTrue.arguments().add(contains);
				ExpressionStatement expressionStatement = ast.newExpressionStatement(assertTrue);
				rewriter.insertLast(expressionStatement, null);
				astRewrite.remove(ASTNodeUtil.getSpecificAncestor(expectedMmessage, Statement.class), null);
			}
			
			for(Expression expectedMmessage : expectedMessageMatchers) {
				MethodInvocation assertTrue = ast.newMethodInvocation();
				Optional<Name> qualifier = addImportForStaticMethod(ORG_HAMCREST_MATCHER_ASSERT_ASSERT_THAT, methodDeclaration);
				qualifier.ifPresent(assertTrue::setExpression);
				assertTrue.setName(ast.newSimpleName("assertThat"));
				MethodInvocation getMessage = ast.newMethodInvocation();
				getMessage.setName(ast.newSimpleName(GET_MESSAGE));
				//FIXME use the one from above
				getMessage.setExpression(ast.newSimpleName("exception"));
				assertTrue.arguments().add(getMessage);
				assertTrue.arguments().add(astRewrite.createCopyTarget(expectedMmessage));
				ExpressionStatement expressionStatement = ast.newExpressionStatement(assertTrue);
				rewriter.insertLast(expressionStatement, null);
				astRewrite.remove(ASTNodeUtil.getSpecificAncestor(expectedMmessage, Statement.class), null);
			}
			
			for(Expression expectedMmessage : expectedCauseMatchers) {
				MethodInvocation assertTrue = ast.newMethodInvocation();
				Optional<Name> qualifier = addImportForStaticMethod(ORG_HAMCREST_MATCHER_ASSERT_ASSERT_THAT, methodDeclaration);
				qualifier.ifPresent(assertTrue::setExpression);
				assertTrue.setName(ast.newSimpleName("assertThat"));
				MethodInvocation getMessage = ast.newMethodInvocation();
				getMessage.setName(ast.newSimpleName("getCause"));
				//FIXME use the one from above
				getMessage.setExpression(ast.newSimpleName("exception"));
				assertTrue.arguments().add(getMessage);
				assertTrue.arguments().add(astRewrite.createCopyTarget(expectedMmessage));
				ExpressionStatement expressionStatement = ast.newExpressionStatement(assertTrue);
				rewriter.insertLast(expressionStatement, null);
				astRewrite.remove(ASTNodeUtil.getSpecificAncestor(expectedMmessage, Statement.class), null);
			}

			onRewrite();
		}

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
