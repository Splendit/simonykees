package eu.jsparrow.core.visitor.junit;

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
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ThrowStatement;

/**
 * This visitor replaces expected annotation property in
 * {@code @Test(expected=...)} with {@code assertThrows()}. For example, the
 * following code:
 * 
 * <pre>
 * <code>
 * &#64;Test(expected = IOException.class)
 * public void singleMarkerValuePair() throws IOException {
 * 	throwsIOException("Simply throw an IOException");
 * }
 * </code>
 * </pre>
 * 
 * becomes:
 * 
 * <pre>
 * <code>
 * &#64;Test
 * public void singleMarkerValuePair() throws IOException {
 * 	assertThrows(IOException.class, () -> throwsIOException("Simply throw an IOException"));
 * }
 * </code>
 * </pre>
 * 
 * @since 3.24.0
 *
 */
public class ReplaceJUnitExpectedAnnotationPropertyASTVisitor extends AbstractReplaceExpectedASTVisitor {

	protected static final String EXCEPTION_TYPE_NAME = java.lang.Exception.class.getName();
	public static final String ORG_JUNIT_TEST = "org.junit.Test"; //$NON-NLS-1$
	private static final String ASSERT_THROWS = "assertThrows"; //$NON-NLS-1$

	private String assertThrowsQualifiedName;

	public ReplaceJUnitExpectedAnnotationPropertyASTVisitor(String assertThrowsQualifiedName) {
		this.assertThrowsQualifiedName = assertThrowsQualifiedName;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyStaticMethodImport(compilationUnit, assertThrowsQualifiedName);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		NormalAnnotation annotation = TestMethodUtil.isTestAnnotatedMethod(methodDeclaration);
		if (annotation == null) {
			return false;
		}

		MemberValuePair expectedValuePair = TestMethodUtil.findNamedValuePair(annotation, "expected"); //$NON-NLS-1$
		if (expectedValuePair == null) {
			return false;
		}

		Expression expectedExpressionExpression = expectedValuePair.getValue();
		ITypeBinding exceptionType = findExceptionTypeArgument(expectedExpressionExpression).orElse(null);
		if (exceptionType == null) {
			return false;
		}

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

		refactor(methodDeclaration, exceptionType, nodeThrowingException, expectedValuePair, annotation);

		return true;
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

	@SuppressWarnings("unchecked")
	private void refactor(MethodDeclaration methodDeclaration,
			ITypeBinding exceptionType, ASTNode nodeThrowingException, MemberValuePair expectedException,
			NormalAnnotation annotation) {

		AST ast = methodDeclaration.getAST();
		Optional<Name> qualifiedPrefix = addImportForStaticMethod(assertThrowsQualifiedName, methodDeclaration);
		MethodInvocation assertThrows = ast.newMethodInvocation();
		assertThrows.setName(ast.newSimpleName(ASSERT_THROWS));
		qualifiedPrefix.ifPresent(assertThrows::setExpression);
		Expression firstArg = (Expression) astRewrite.createCopyTarget(expectedException.getValue());
		LambdaExpression lambdaExpression = ast.newLambdaExpression();
		ASTNode lambdaBody = createThrowRunnable(nodeThrowingException);
		lambdaExpression.setBody(lambdaBody);
		List<Expression> assertionArguments = assertThrows.arguments();
		assertionArguments.add(firstArg);
		assertionArguments.add(lambdaExpression);

		removeThrowsDeclarations(methodDeclaration, exceptionType);

		ExpressionStatement assertionStatement = ast.newExpressionStatement(assertThrows);
		astRewrite.replace(nodeThrowingException.getParent(), assertionStatement, null);

		TestMethodUtil.removeAnnotationProperty(astRewrite, annotation, expectedException);
		onRewrite();

	}

}
