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
		if(annotation == null) {
			return false;
		}
		
		MemberValuePair timeoutPair = TestMethodUtil.findExpectedValuePair(annotation, "timeout");
		if(timeoutPair == null) {
			return false;
		}
		Expression expectedDuration = timeoutPair.getValue();
		
		//TODO find all thrown exceptions. Are there checked exceptions?
		
		Block body = methodDeclaration.getBody();
		List<Statement> statements = ASTNodeUtil.convertToTypedList(body.statements(), Statement.class);
		if(statements.size() == 1 && statements.get(0).getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
			ExpressionStatement statement = (ExpressionStatement) statements.get(0);
			Expression expression = statement.getExpression();
			
			AST ast = methodDeclaration.getAST();
			
			MethodInvocation assertTimeOut = ast.newMethodInvocation();
			assertTimeOut.setName(ast.newSimpleName("assertTimeout"));
			Optional<Name> qualifiedPrefix = addImportForStaticMethod(ASSERT_TIMEOUT, methodDeclaration);
			qualifiedPrefix.ifPresent(assertTimeOut::setExpression);
			
			MethodInvocation ofMillis = ast.newMethodInvocation();
			ofMillis.setName(ast.newSimpleName("ofMillis"));
			Optional<Name> ofMillisPrefix = addImportForStaticMethod(DURATION_OF_MILLIS, methodDeclaration);
			ofMillisPrefix.ifPresent(ofMillis::setExpression);
			ofMillis.arguments().add(astRewrite.createCopyTarget(expectedDuration));
			
			LambdaExpression executable = ast.newLambdaExpression();
			executable.setBody(astRewrite.createCopyTarget(expression));
			
			List assertionArguments = assertTimeOut.arguments();
			assertionArguments.add(ofMillis);
			assertionArguments.add(executable);
			
			astRewrite.replace(expression, assertTimeOut, null);
			onRewrite();
			
			TestMethodUtil.removeAnnotationProperty(astRewrite, annotation, expectedDuration);
		} else {
			
			AST ast = methodDeclaration.getAST();
			
			MethodInvocation assertTimeOut = ast.newMethodInvocation();
			assertTimeOut.setName(ast.newSimpleName("assertTimeout"));
			Optional<Name> qualifiedPrefix = addImportForStaticMethod(ASSERT_TIMEOUT, methodDeclaration);
			qualifiedPrefix.ifPresent(assertTimeOut::setExpression);
			
			MethodInvocation ofMillis = ast.newMethodInvocation();
			ofMillis.setName(ast.newSimpleName("ofMillis"));
			Optional<Name> ofMillisPrefix = addImportForStaticMethod(DURATION_OF_MILLIS, methodDeclaration);
			ofMillisPrefix.ifPresent(ofMillis::setExpression);
			ofMillis.arguments().add(astRewrite.createCopyTarget(expectedDuration));
			
			LambdaExpression executable = ast.newLambdaExpression();
			executable.setBody(astRewrite.createMoveTarget(body)); // 2 
			
			List assertionArguments = assertTimeOut.arguments();
			assertionArguments.add(ofMillis);
			assertionArguments.add(executable);
			
			Block newTestCase = ast.newBlock();

			ExpressionStatement assertionStatement = ast.newExpressionStatement(assertTimeOut);
			newTestCase.statements().add(assertionStatement);
			
			astRewrite.replace(body, newTestCase, null);
			
			onRewrite();
			
			TestMethodUtil.removeAnnotationProperty(astRewrite, annotation, expectedDuration);
			
		}

		return true;
	}
}
