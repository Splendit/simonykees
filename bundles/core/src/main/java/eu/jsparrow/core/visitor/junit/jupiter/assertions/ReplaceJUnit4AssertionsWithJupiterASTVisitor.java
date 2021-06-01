package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleType;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Replaces invocations of methods of the JUnit 4 class {@code org.junit.Assert}
 * by invocations of the corresponding methods of the JUnit Jupiter class
 * {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.28.0
 * 
 */
public class ReplaceJUnit4AssertionsWithJupiterASTVisitor extends AbstractReplaceJUnit4InvocationsASTVisitor {

	private static final String ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE = "org.junit.jupiter.api.function.Executable"; //$NON-NLS-1$
	protected static final String ORG_J_UNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$

	public ReplaceJUnit4AssertionsWithJupiterASTVisitor() {
		super(ORG_J_UNIT_JUPITER_API_ASSERTIONS);
	}

	@Override
	protected Optional<JUnit4InvocationReplacementAnalysis> findAnalysisResult(MethodInvocation methodInvocation,
			IMethodBinding methodBinding) {

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		if (!arguments.stream()
			.allMatch(this::isArgumentWithExplicitType)) {
			return Optional.empty();
		}

		CompilationUnit compilationUnit = getCompilationUnit();
		JUnitJupiterTestMethodsStore jUnitJupiterTestMethodsStore = new JUnitJupiterTestMethodsStore(compilationUnit);
		if (!jUnitJupiterTestMethodsStore.isSurroundedWithJUnitJupiterTest(methodInvocation)) {
			return Optional.empty();
		}

		JUnit4InvocationReplacementAnalysis analysisObject = new JUnit4InvocationReplacementAnalysis(
				methodInvocation, methodBinding, arguments);

		if (analysisObject.analyzeAssertionToJupiter()) {
			return Optional.of(analysisObject);
		}
		return Optional.empty();

	}

	@Override
	protected void transform(JUnit4TransformationDataCollections transformationDataCollections) {

		verifyImport(getCompilationUnit(), ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE);

		super.transform(transformationDataCollections);
		AST ast = astRewrite.getAST();
		transformationDataCollections
			.getThrowingRunnableTypesToReplace()
			.forEach(typeToReplace -> {
				Name executableTypeName = addImport(ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE, typeToReplace);
				SimpleType typeReplacement = ast.newSimpleType(executableTypeName);
				astRewrite.replace(typeToReplace, typeReplacement, null);
			});
	}

	@Override
	protected boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		return isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assert") //$NON-NLS-1$
				&& !methodBinding.getName()
					.equals("assertThat"); //$NON-NLS-1$
	}
}