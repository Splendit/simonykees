package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
	JUnitJupiterTestMethodsStore jUnitJupiterTestMethodsStore = new JUnitJupiterTestMethodsStore();

	public ReplaceJUnit4AssertionsWithJupiterASTVisitor() {
		super(ORG_J_UNIT_JUPITER_API_ASSERTIONS);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		jUnitJupiterTestMethodsStore.collectJUnitJupiterTestMethods(compilationUnit);
		return super.visit(compilationUnit);
	}

	@Override
	protected void transform(JUnit4TransformationDataCollections transformationDataCollections) {

		verifyImport(getCompilationUnit(), ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE);

		super.transform(transformationDataCollections);
		AST ast = astRewrite.getAST();
		transformationDataCollections
			.getThrowingRunnableTypesToReplace()
			.stream()
			.forEach(typeToReplace -> {
				Name executableTypeName = addImport(ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE, typeToReplace);
				SimpleType typeReplacement = ast.newSimpleType(executableTypeName);
				astRewrite.replace(typeToReplace, typeReplacement, null);
			});
	}

	@Override
	protected Optional<JUnit4InvocationReplacementAnalysis> findAnalysisResult(MethodInvocation methodInvocation,
			IMethodBinding methodBinding, List<Expression> arguments) {

		if (!jUnitJupiterTestMethodsStore.isSurroundedWithJUnitJupiterTest(methodInvocation)) {
			return Optional.empty();
		}
		JUnit4InvocationReplacementAnalysis invocationAnalyzer = new JUnit4InvocationReplacementAnalysis(
				methodInvocation, methodBinding, arguments);
		if (invocationAnalyzer.analyzeAssertion()) {
			return Optional.of(invocationAnalyzer);
		}

		return Optional.empty();
	}

	@Override
	protected JUnit4InvocationReplacementData createTransformationData(
			JUnit4InvocationReplacementAnalysis invocationData,
			Map<String, String> supportedStaticImportsMap) {

		MethodInvocation methodInvocation = invocationData.getMethodInvocation();
		String originalMethodName = invocationData.getOriginalMethodName();
		String newMethodName = invocationData.getNewMethodName();

		List<Expression> originalArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		Expression messageMovingToLastPosition = invocationData.getMessageMovedToLastPosition()
			.orElse(null);

		List<Expression> newArguments;
		if (messageMovingToLastPosition != null) {
			newArguments = new ArrayList<>(originalArguments);
			newArguments.remove(messageMovingToLastPosition);
			newArguments.add(messageMovingToLastPosition);
		} else {
			newArguments = originalArguments;
		}

		boolean useNewMethodStaticImport = supportedStaticImportsMap.containsKey(newMethodName);

		if (useNewMethodStaticImport
				&& methodInvocation.getExpression() == null
				&& newArguments == originalArguments
				&& newMethodName.equals(originalMethodName)) {
			return new JUnit4InvocationReplacementData(invocationData);
		}

		Supplier<List<Expression>> newArgumentsSupplier = () -> newArguments.stream()
			.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
			.collect(Collectors.toList());

		if (useNewMethodStaticImport) {
			return new JUnit4InvocationReplacementData(invocationData,
					() -> createNewInvocationWithoutQualifier(newMethodName, newArgumentsSupplier));
		}

		Supplier<MethodInvocation> newMethodInvocationSupplier = () -> createNewInvocationWithQualifier(
				methodInvocation,
				newMethodName, newArgumentsSupplier);

		return new JUnit4InvocationReplacementData(invocationData, newMethodInvocationSupplier);
	}

	@Override
	protected boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		return isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assert") //$NON-NLS-1$
				&& !methodBinding.getName()
					.equals("assertThat"); //$NON-NLS-1$
	}
}