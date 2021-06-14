package eu.jsparrow.core.visitor.junit.dedicated;

import static eu.jsparrow.core.visitor.junit.dedicated.BooleanAssertionAnalyzer.ORG_JUNIT_ASSERT;
import static eu.jsparrow.core.visitor.junit.dedicated.BooleanAssertionAnalyzer.ORG_JUNIT_JUPITER_API_ASSERTIONS;
import static eu.jsparrow.core.visitor.junit.dedicated.BooleanAssertionAnalyzer.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Replaces boolean assertions by dedicated assertions, for example:
 * 
 * <pre>
 * assertTrue(a.equals(b));
 * </pre>
 * 
 * is replaced by
 * 
 * <pre>
 * assertEquals(a, b);
 * </pre>
 * 
 * 
 * @since 3.31.0
 * 
 */
public class UseDedicatedAssertionsASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		super.visit(compilationUnit);
		verifyImport(compilationUnit, ORG_JUNIT_ASSERT);
		verifyImport(compilationUnit, ORG_JUNIT_JUPITER_API_ASSERTIONS);

		List<String> newMethodNames = Arrays.asList(
				ASSERT_SAME,
				ASSERT_NOT_SAME,
				ASSERT_NULL,
				ASSERT_NOT_NULL,
				ASSERT_EQUALS,
				ASSERT_NOT_EQUALS);
		newMethodNames
			.forEach(methodName -> verifyStaticMethodImport(compilationUnit, ORG_JUNIT_ASSERT + '.' + methodName));
		newMethodNames
			.forEach(methodName -> verifyStaticMethodImport(compilationUnit,
					ORG_JUNIT_JUPITER_API_ASSERTIONS + '.' + methodName));

		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		BooleanAssertionAnalyzer assertionAnalyzer = new BooleanAssertionAnalyzer();
		DedicatedAssertionsAnalysisResult analysisResult = assertionAnalyzer.analyzeAssertInvocation(node)
			.orElse(null);

		if (analysisResult != null) {
			AST ast = astRewrite.getAST();
			MethodInvocation methodInvocationReplacement = ast.newMethodInvocation();
			String newMethodName = analysisResult.getNewMethodName();
			methodInvocationReplacement.setName(ast.newSimpleName(newMethodName));
			@SuppressWarnings("unchecked")
			List<Expression> newInvocationArguments = methodInvocationReplacement.arguments();
			analysisResult
				.getNewArguments()
				.stream()
				.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
				.forEach(newInvocationArguments::add);

			String newMethodFullyQualifiedName = analysisResult.getDeclaringClassQualifiedName() + '.' + newMethodName;
			Optional<Name> qualifier = addImportForStaticMethod(newMethodFullyQualifiedName, node);
			qualifier.ifPresent(methodInvocationReplacement::setExpression);

			astRewrite.replace(node, methodInvocationReplacement, null);
			onRewrite();
		}
		return true;
	}
}