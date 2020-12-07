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
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LiveVariableScope;

/**
 * Replaces {@code ExpectedException.expect()} with {@code assertThrows}.
 * Inserts further assertion to replace {@code ExpectedException.expectMessage}
 * and {@code ExpectedException.expectCause}. For example:
 * 
 * <pre>
 * <code>
 * &#64;Rule
 * public ExpectedException expectedException = ExpectedException.none();
 * 
 * &#64;Test
 * public void testingExceptionCause() throws IOException {
 *    Matcher<Throwable> isNotFileNotFoundException = not(is(new FileNotFoundException()));
 *    expectedException.expect(IOException.class);
 *    expectedException.expectCause(isNotFileNotFoundException);
 *    assertThat(exception.getMessage(), containsString("IO"));
 *    throwIOException();
 * }
 * </code>
 * </pre>
 * 
 * becomes:
 * 
 * <pre>
 * <code>
 * &#64;Test
 * public void testingExceptionCause() {
 *    Matcher<Throwable> isNotFileNotFoundException = not(is(new FileNotFoundException()));
 *    IOException exception = assertThrows(IOException.class, () -> throwIOException());
 *    assertThat(exception.getCause(), isNotFileNotFoundException);
 *    assertThat(exception.getMessage(), containsString("IO"));
 * }
 * </code>
 * </pre>
 * 
 * @since 3.24.0
 *
 */
public class ReplaceExpectedExceptionASTVisitor extends AbstractAddImportASTVisitor {

	private static final String GET_MESSAGE = "getMessage"; //$NON-NLS-1$
	private static final String ASSERT_TRUE = "assertTrue"; //$NON-NLS-1$
	private static final String ORG_HAMCREST_MATCHER = "org.hamcrest.Matcher"; //$NON-NLS-1$
	private static final String ORG_JUNIT_ASSERT_ASSERT_TRUE = "org.junit.Assert.assertTrue"; //$NON-NLS-1$
	private static final String ORG_HAMCREST_MATCHER_ASSERT_ASSERT_THAT = "org.hamcrest.MatcherAssert.assertThat"; //$NON-NLS-1$
	private static final String ORG_JUNIT_JUPITER_API_TEST = "org.junit.jupiter.api.Test"; //$NON-NLS-1$
	private static final String ASSERT_THROWS = "assertThrows"; //$NON-NLS-1$
	private static final String ORG_JUNIT_TEST = "org.junit.Test"; //$NON-NLS-1$
	private static final String EXCEPTION_TYPE_NAME = java.lang.Exception.class.getName();
	private static final String EXCEPTION_NAME = "exception"; //$NON-NLS-1$

	private String assertThrowsQualifiedName;

	private LiveVariableScope aliveVariableScope = new LiveVariableScope();

	public ReplaceExpectedExceptionASTVisitor(String assertThrowsQualifiedName) {
		this.assertThrowsQualifiedName = assertThrowsQualifiedName;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyStaticMethodImport(compilationUnit, assertThrowsQualifiedName);
			verifyStaticMethodImport(compilationUnit, ORG_JUNIT_ASSERT_ASSERT_TRUE);
			verifyStaticMethodImport(compilationUnit, ORG_HAMCREST_MATCHER_ASSERT_ASSERT_THAT);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {

		// Check if the method declaration is a test case
		if (!isTestAnnotatedMethod(methodDeclaration)) {
			return false;
		}

		// Find ExpectedException invocations
		ExpectedExceptionVisitor expectedExceptionVisitor = new ExpectedExceptionVisitor();
		if (!analyzeUsagesOfExpectedException(methodDeclaration, expectedExceptionVisitor)) {
			return false;
		}

		Expression expectedException = expectedExceptionVisitor.getExpectedExceptionsExpression()
			.get(0);
		ITypeBinding exceptionType = findExceptionTypeArgument(expectedException).orElse(null);
		if (exceptionType == null) {
			return true;
		}

		// Search for statements throwing the expected exception
		ExpressionsThrowingExceptionVisitor throwingExceptionsVisitor = new ExpressionsThrowingExceptionVisitor(
				exceptionType);
		if (!hasSingleNodeThrowingException(methodDeclaration, throwingExceptionsVisitor)) {
			return false;
		}

		ASTNode nodeThrowingException = throwingExceptionsVisitor.getNodesThrowingExpectedException()
			.get(0);
		boolean isLastStatement = verifyPosition(methodDeclaration, nodeThrowingException);
		if (!isLastStatement) {
			return false;
		}

		refactor(methodDeclaration, exceptionType, nodeThrowingException, expectedExceptionVisitor);
		return false;
	}

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		aliveVariableScope.clearCompilationUnitScope(compilationUnit);
		super.endVisit(compilationUnit);
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		aliveVariableScope.clearFieldScope(typeDeclaration);
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		aliveVariableScope.clearLocalVariablesScope(methodDeclaration);
	}

	@Override
	public void endVisit(FieldDeclaration fieldDeclaration) {
		aliveVariableScope.clearLocalVariablesScope(fieldDeclaration);
	}

	@Override
	public void endVisit(Initializer initializer) {
		aliveVariableScope.clearLocalVariablesScope(initializer);
	}

	private boolean analyzeUsagesOfExpectedException(MethodDeclaration methodDeclaration,
			ExpectedExceptionVisitor visitor) {
		Block body = methodDeclaration.getBody();
		body.accept(visitor);
		if (visitor.hasUnsupportedMethods() || visitor.hasUnresolvedInvocations()) {
			return false;
		}

		if (!visitor.hasUniqueExpectedExceptionRule()) {
			return false;
		}

		List<MethodInvocation> expectExceptionsInvocations = visitor.getExpectExceptionInvocations();
		if (expectExceptionsInvocations.size() != 1) {
			return false;
		}

		MethodInvocation expectExceptionInvocation = expectExceptionsInvocations.get(0);
		if (expectExceptionInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return false;
		}

		List<Expression> expectedExceptions = visitor.getExpectedExceptionsExpression();
		if (expectedExceptions.size() != 1) {
			return false;
		}

		return visitor.verifyExpectCauseMatchers();
	}

	private boolean hasSingleNodeThrowingException(MethodDeclaration methodDeclaration,
			ExpressionsThrowingExceptionVisitor throwingExceptionsVisitor) {
		Block body = methodDeclaration.getBody();
		body.accept(throwingExceptionsVisitor);
		List<ASTNode> nodesThrowingException = throwingExceptionsVisitor.getNodesThrowingExpectedException();
		if (nodesThrowingException.size() != 1) {
			return false;
		}
		ASTNode nodeThrowingException = nodesThrowingException.get(0);
		return nodeThrowingException.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY
				|| nodeThrowingException.getLocationInParent() == ThrowStatement.EXPRESSION_PROPERTY;
	}

	private boolean isTestAnnotatedMethod(MethodDeclaration methodDeclaration) {
		List<MarkerAnnotation> annotations = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(),
				MarkerAnnotation.class);
		if (annotations.size() != 1) {
			return false;
		}
		MarkerAnnotation annotation = annotations.get(0);
		Name typeName = annotation.getTypeName();
		ITypeBinding annotationTypeBinding = typeName.resolveTypeBinding();
		return ClassRelationUtil.isContentOfTypes(annotationTypeBinding,
				Arrays.asList(ORG_JUNIT_JUPITER_API_TEST, ORG_JUNIT_TEST));
	}

	private boolean verifyPosition(MethodDeclaration methodDeclaration, ASTNode nodeThrowingException) {
		Block testBody = methodDeclaration.getBody();
		List<Statement> statements = ASTNodeUtil.convertToTypedList(testBody.statements(), Statement.class);
		Statement lastStatement = statements.get(statements.size() - 1);
		return lastStatement == nodeThrowingException.getParent();
	}

	@SuppressWarnings("unchecked")
	private void refactor(MethodDeclaration methodDeclaration,
			ITypeBinding exceptionType, ASTNode nodeThrowingException,
			ExpectedExceptionVisitor visitor) {

		List<Expression> expectedMessages = visitor
			.getExpectedMessages(mi -> visitor.hasSingleParameterOfType(mi, java.lang.String.class.getName()));
		List<Expression> expectedMessageMatchers = visitor
			.getExpectedMessages(mi -> visitor.hasSingleParameterOfType(mi, ORG_HAMCREST_MATCHER));
		List<Expression> expectedCauseMatchers = visitor.getExpectedCauses();
		MethodInvocation expectExceptionInvocation = visitor.getExpectExceptionInvocations()
			.get(0);
		Expression expectedException = visitor.getExpectedExceptionsExpression()
			.get(0);

		AST ast = methodDeclaration.getAST();
		Optional<Name> qualifiedPrefix = addImportForStaticMethod(assertThrowsQualifiedName, methodDeclaration);
		MethodInvocation assertThrows = ast.newMethodInvocation();
		assertThrows.setName(ast.newSimpleName(ASSERT_THROWS));
		qualifiedPrefix.ifPresent(assertThrows::setExpression);
		Expression firstArg = (Expression) astRewrite.createCopyTarget(expectedException);
		LambdaExpression lambdaExpression = ast.newLambdaExpression();
		ASTNode lambdaBody = createThrowRunnable(nodeThrowingException);
		lambdaExpression.setBody(lambdaBody);
		@SuppressWarnings("rawtypes")
		List assertionArguments = assertThrows.arguments();
		assertionArguments.add(firstArg);
		assertionArguments.add(lambdaExpression);

		removeThrowsDeclarations(methodDeclaration, exceptionType);

		if (expectedMessages.isEmpty() && expectedMessageMatchers.isEmpty() && expectedCauseMatchers.isEmpty()) {
			ExpressionStatement assertionStatement = ast.newExpressionStatement(assertThrows);
			astRewrite.replace(nodeThrowingException.getParent(), assertionStatement, null);
			astRewrite.remove(expectExceptionInvocation.getParent(), null);
			onRewrite();
		} else {
			VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
			fragment.setInitializer(assertThrows);
			// the scope is already the methodDeclaration
			aliveVariableScope.lazyLoadScopeNames(methodDeclaration);
			String exceptionIdentifier = createExceptionName();
			fragment.setName(ast.newSimpleName(exceptionIdentifier));
			VariableDeclarationStatement exceptionDeclaration = ast.newVariableDeclarationStatement(fragment);
			verifyImport(getCompilationUnit(), exceptionType.getQualifiedName());
			Name exceptionTypeName = addImport(exceptionType.getQualifiedName(), methodDeclaration);
			Type type = ast.newSimpleType(exceptionTypeName);
			exceptionDeclaration.setType(type);

			astRewrite.replace(nodeThrowingException.getParent(), exceptionDeclaration, null);
			astRewrite.remove(expectExceptionInvocation.getParent(), null);
			createAssertTrueInvocations(methodDeclaration, expectedMessages, exceptionIdentifier);

			String getMessageIdentifier = GET_MESSAGE;
			createAssertThatInvocations(methodDeclaration, expectedMessageMatchers, exceptionIdentifier,
					getMessageIdentifier);

			String getCauseIdentifier = "getCause"; //$NON-NLS-1$
			createAssertThatInvocations(methodDeclaration, expectedCauseMatchers, exceptionIdentifier,
					getCauseIdentifier);

			onRewrite();
		}
	}

	private void removeThrowsDeclarations(MethodDeclaration methodDeclaration, ITypeBinding exceptionType) {
		List<Type> exceptionTypes = ASTNodeUtil.convertToTypedList(methodDeclaration.thrownExceptionTypes(),
				Type.class);
		ListRewrite listRewrite = astRewrite.getListRewrite(methodDeclaration,
				MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY);
		for (Type type : exceptionTypes) {
			ITypeBinding typeBinding = type.resolveBinding();
			if (ClassRelationUtil.compareITypeBinding(typeBinding, exceptionType)) {
				listRewrite.remove(type, null);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void createAssertTrueInvocations(MethodDeclaration methodDeclaration, List<Expression> expectedMessages,
			String exceptionIdentifier) {
		AST ast = methodDeclaration.getAST();
		ListRewrite rewriter = astRewrite.getListRewrite(methodDeclaration.getBody(), Block.STATEMENTS_PROPERTY);
		for (Expression expectedMmessage : expectedMessages) {
			MethodInvocation assertTrue = ast.newMethodInvocation();
			Optional<Name> qualifier = addImportForStaticMethod(ORG_JUNIT_ASSERT_ASSERT_TRUE, methodDeclaration);
			qualifier.ifPresent(assertTrue::setExpression);
			assertTrue.setName(ast.newSimpleName(ASSERT_TRUE));
			MethodInvocation getMessage = ast.newMethodInvocation();
			getMessage.setName(ast.newSimpleName(GET_MESSAGE));
			getMessage.setExpression(ast.newSimpleName(exceptionIdentifier));
			MethodInvocation contains = ast.newMethodInvocation();
			contains.setName(ast.newSimpleName("contains")); //$NON-NLS-1$
			contains.setExpression(getMessage);
			contains.arguments()
				.add(astRewrite.createCopyTarget(expectedMmessage));
			assertTrue.arguments()
				.add(contains);
			ExpressionStatement expressionStatement = ast.newExpressionStatement(assertTrue);
			rewriter.insertLast(expressionStatement, null);
			astRewrite.remove(ASTNodeUtil.getSpecificAncestor(expectedMmessage, Statement.class), null);
		}
	}

	@SuppressWarnings("unchecked")
	private void createAssertThatInvocations(MethodDeclaration methodDeclaration,
			List<Expression> expectedCauseMatchers,
			String exceptionIdentifier, String getCauseIdentifier) {
		String assertThatIdentifer = "assertThat"; //$NON-NLS-1$
		AST ast = methodDeclaration.getAST();
		ListRewrite rewriter = astRewrite.getListRewrite(methodDeclaration.getBody(), Block.STATEMENTS_PROPERTY);
		for (Expression expectedMmessage : expectedCauseMatchers) {
			MethodInvocation assertTrue = ast.newMethodInvocation();
			Optional<Name> qualifier = addImportForStaticMethod(ORG_HAMCREST_MATCHER_ASSERT_ASSERT_THAT,
					methodDeclaration);
			qualifier.ifPresent(assertTrue::setExpression);
			assertTrue.setName(ast.newSimpleName(assertThatIdentifer));
			MethodInvocation getMessage = ast.newMethodInvocation();
			getMessage.setName(ast.newSimpleName(getCauseIdentifier));
			getMessage.setExpression(ast.newSimpleName(exceptionIdentifier));
			assertTrue.arguments()
				.add(getMessage);
			assertTrue.arguments()
				.add(astRewrite.createCopyTarget(expectedMmessage));
			ExpressionStatement expressionStatement = ast.newExpressionStatement(assertTrue);
			rewriter.insertLast(expressionStatement, null);
			astRewrite.remove(ASTNodeUtil.getSpecificAncestor(expectedMmessage, Statement.class), null);
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
		boolean isClass = ClassRelationUtil.isContentOfType(argumentType, java.lang.Class.class.getName());
		if (isClass && argumentType.isParameterizedType()) {
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

	private String createExceptionName() {
		String name = EXCEPTION_NAME;
		int suffix = 1;
		while (aliveVariableScope.isInScope(name)) {
			name = EXCEPTION_NAME + suffix;
			suffix++;
		}
		return name;
	}

}
