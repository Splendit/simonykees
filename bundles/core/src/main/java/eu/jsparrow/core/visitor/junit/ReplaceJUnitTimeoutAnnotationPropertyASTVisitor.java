package eu.jsparrow.core.visitor.junit;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * JUnit Jupiter API provides timeout assertions, i.e., assertions that
 * execution of some code completes before a timeout exceeds. In JUnit 4, this
 * was only possible through the {@code timeout} property of the {@code @Test}
 * annotation. This visitor removes the {@code timeout} annotation property and
 * inserts an {@code assertTimeout(duration, executable)} instead.
 * 
 * <br/>
 * 
 * For example, the following:
 * 
 * <pre>
 * <code>
 * &#64;Test(timeout=500)
 * public void timeoutTest() throws PersistenceException {
 * 	userRepository.save(new User("10", "John", "wolf"));
 * }
 * </code>
 * </pre>
 * 
 * is transformed to:
 * 
 * <pre>
 * <code>
 * &#64;Test
 * public void timeoutTest() throws PersistenceException {
 * 	assertTimeout(ofMillis(500), () -> userRepository.save(new User("10", "John", "wolf")));
 * }
 * </code>
 * </pre>
 * 
 * @since 3.26.0
 *
 */
public class ReplaceJUnitTimeoutAnnotationPropertyASTVisitor extends AbstractAddImportASTVisitor {

	public static final String ORG_JUNIT_TEST = "org.junit.Test"; //$NON-NLS-1$
	private static final String ASSERT_TIMEOUT = "org.junit.jupiter.api.Assertions.assertTimeout"; //$NON-NLS-1$
	private static final String DURATION_OF_MILLIS = "java.time.Duration.ofMillis"; //$NON-NLS-1$

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyStaticMethodImport(compilationUnit, ASSERT_TIMEOUT);
			verifyStaticMethodImport(compilationUnit, DURATION_OF_MILLIS);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		NormalAnnotation annotation = TestMethodUtil.isTestAnnotatedMethod(methodDeclaration);
		if (annotation == null) {
			return false;
		}

		MemberValuePair timeoutPair = TestMethodUtil.findNamedValuePair(annotation, "timeout"); //$NON-NLS-1$
		if (timeoutPair == null) {
			return false;
		}
		refactor(methodDeclaration, annotation, timeoutPair);

		return true;
	}

	private void refactor(MethodDeclaration methodDeclaration, NormalAnnotation annotation,
			MemberValuePair expectedDuration) {
		Block body = methodDeclaration.getBody();
		List<Statement> statements = ASTNodeUtil.convertToTypedList(body.statements(), Statement.class);

		if (statements.size() == 1 && statements.get(0)
			.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
			ExpressionStatement statement = (ExpressionStatement) statements.get(0);
			Expression expression = statement.getExpression();
			MethodInvocation assertTimeOut = createAssertTimeoutParameters(expression, expectedDuration.getValue());
			astRewrite.replace(expression, assertTimeOut, null);
		} else {
			MethodInvocation assertTimeOut = createAssertTimeoutParameters(body, expectedDuration.getValue());
			AST ast = methodDeclaration.getAST();
			Block newTestCase = ast.newBlock();
			ExpressionStatement assertionStatement = ast.newExpressionStatement(assertTimeOut);
			@SuppressWarnings("unchecked")
			List<Statement> newStatements = newTestCase.statements();
			newStatements.add(assertionStatement);
			astRewrite.replace(body, newTestCase, null);
		}

		TestMethodUtil.removeAnnotationProperty(astRewrite, annotation, expectedDuration);
		onRewrite();
	}

	private MethodInvocation createAssertTimeoutParameters(ASTNode body,
			Expression expectedDuration) {
		AST ast = body.getAST();
		MethodInvocation assertTimeOut = ast.newMethodInvocation();
		assertTimeOut.setName(ast.newSimpleName("assertTimeout")); //$NON-NLS-1$
		Optional<Name> qualifiedPrefix = addImportForStaticMethod(ASSERT_TIMEOUT, body);
		qualifiedPrefix.ifPresent(assertTimeOut::setExpression);

		MethodInvocation ofMillis = ast.newMethodInvocation();
		ofMillis.setName(ast.newSimpleName("ofMillis")); //$NON-NLS-1$
		Optional<Name> ofMillisPrefix = addImportForStaticMethod(DURATION_OF_MILLIS, body);
		ofMillisPrefix.ifPresent(ofMillis::setExpression);
		@SuppressWarnings("unchecked")
		List<Expression> ofMillisArguments = ofMillis.arguments();
		ofMillisArguments.add((Expression) astRewrite.createCopyTarget(expectedDuration));

		LambdaExpression executable = ast.newLambdaExpression();
		executable.setBody(astRewrite.createMoveTarget(body));
		@SuppressWarnings("unchecked")
		List<Expression> assertionArguments = assertTimeOut.arguments();
		assertionArguments.add(ofMillis);
		assertionArguments.add(executable);

		return assertTimeOut;
	}
}
