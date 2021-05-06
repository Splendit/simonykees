package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

/**
 * Replaces the JUnit 4 method invocations
 * {@code org.junit.Assume.assumeNoException},
 * {@code org.junit.Assume.assumeNotNull} and
 * {@code org.junit.Assume.assumeThat} by invocations of the corresponding
 * methods of {@code org.hamcrest.junit.MatcherAssume.assumeThat}.
 * 
 * @since 3.31.0
 * 
 */
public class ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor
		extends AbstractReplaceJUnit4MethodInvocationsASTVisitor {

	private static final String ORG_HAMCREST_CORE_MATCHERS_NULL_VALUE = "org.hamcrest.CoreMatchers.nullValue"; //$NON-NLS-1$
	private static final String ASSUME_NOT_NULL = "assumeNotNull"; //$NON-NLS-1$
	private static final String ASSUME_NO_EXCEPTION = "assumeNoException"; //$NON-NLS-1$
	private static final String ASSUME_THAT = "assumeThat"; //$NON-NLS-1$
	private final Set<String> potentialMethodNameReplacements;

	public ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor() {
		super("org.hamcrest.junit.MatcherAssume"); //$NON-NLS-1$
		Set<String> tmp = new HashSet<>();
		tmp.add(ASSUME_THAT);
		potentialMethodNameReplacements = Collections.unmodifiableSet(tmp);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		super.visit(compilationUnit);

		verifyImport(compilationUnit, classDeclaringJUnit4MethodReplacement);
		verifyStaticMethodImport(compilationUnit, ORG_HAMCREST_CORE_MATCHERS_NULL_VALUE);

		JUnit4MethodInvocationAnalysisResultStore transformationDataStore = createTransformationDataStore(
				compilationUnit);
		List<ImportDeclaration> staticMethodImportsToRemove = collectStaticMethodImportsToRemove(compilationUnit,
				transformationDataStore);

		Set<String> supportedNewStaticMethodImports = findSupportedStaticImports(staticMethodImportsToRemove,
				transformationDataStore);

		List<JUnit4MethodInvocationAnalysisResult> allSupportedJUnit4InvocationDataList = transformationDataStore
			.getMethodInvocationAnalysisResults();

		List<JUnit4MethodInvocationReplacementData> jUnit4AssertTransformationDataList = allSupportedJUnit4InvocationDataList
			.stream()
			.filter(JUnit4MethodInvocationAnalysisResult::isTransformable)
			.map(data -> this.createMethodInvocationReplacementData(data, supportedNewStaticMethodImports))
			.collect(Collectors.toList());

		Set<String> newStaticAssertionMethodImports = jUnit4AssertTransformationDataList.stream()
			.map(JUnit4MethodInvocationReplacementData::getStaticMethodImport)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());

		transform(staticMethodImportsToRemove, newStaticAssertionMethodImports, jUnit4AssertTransformationDataList);

		return true;
	}

	private JUnit4MethodInvocationReplacementData createMethodInvocationReplacementData(
			JUnit4MethodInvocationAnalysisResult invocationData,
			Set<String> supportedNewStaticMethodImports) {

		MethodInvocation methodInvocation = invocationData.getMethodInvocation();
		IMethodBinding originalMethodBinding = invocationData.getMethodBinding();
		String originalMethodName = originalMethodBinding.getName();

		boolean changeInvocation = originalMethodName.equals(ASSUME_NO_EXCEPTION)
				|| originalMethodName.equals(ASSUME_NOT_NULL);
		String newMethodName = ASSUME_THAT;
		List<Expression> originalArguments = invocationData.getArguments();

		String newMethodStaticImport = classDeclaringJUnit4MethodReplacement + "." + newMethodName; //$NON-NLS-1$
		boolean useNewStaticimport = supportedNewStaticMethodImports.contains(newMethodStaticImport);

		boolean keepUnqualified = methodInvocation.getExpression() == null && useNewStaticimport;
		if (!changeInvocation && keepUnqualified) {
			return new JUnit4MethodInvocationReplacementData(methodInvocation, newMethodStaticImport);
		}

		Supplier<List<Expression>> newArgumentsSupplier;
		if (originalMethodName.equals(ASSUME_NO_EXCEPTION)) {
			newArgumentsSupplier = () -> createAssumeThatExceptionIsNullArguments(methodInvocation,
					originalArguments);
		} else if (originalMethodName.equals(ASSUME_NOT_NULL)) {
			newArgumentsSupplier = () -> createAssumeThatListIsNotNullArguments(methodInvocation, originalArguments);
		} else {
			newArgumentsSupplier = () -> createNewMethodArguments(originalArguments);
		}

		if (useNewStaticimport) {
			return new JUnit4MethodInvocationReplacementData(methodInvocation,
					() -> createNewInvocationWithoutQualifier(newMethodName, newArgumentsSupplier),
					newMethodStaticImport);
		}
		Supplier<MethodInvocation> newMethodInvocationSupplier = () -> createNewInvocationWithQualifier(
				methodInvocation,
				newMethodName, newArgumentsSupplier);

		return new JUnit4MethodInvocationReplacementData(methodInvocation, newMethodInvocationSupplier);
	}

	@Override
	protected boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		if (isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assume")) {//$NON-NLS-1$
			String methodName = methodBinding.getName();
			return methodName.equals(ASSUME_NO_EXCEPTION) ||
			// TODO: implement transformation of assumeNotNull
			// methodName.equals(ASSUME_NOT_NULL) ||
					methodName.equals(ASSUME_THAT);
		}
		return false;
	}

	@Override
	protected Set<String> getSupportedMethodNameReplacements() {
		return potentialMethodNameReplacements;
	}

	private List<Expression> createAssumeThatExceptionIsNullArguments(MethodInvocation methodInvocation,
			List<Expression> originalArguments) {
		List<Expression> newArguments = new ArrayList<>();
		originalArguments.stream()
			.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
			.forEach(newArguments::add);
		AST ast = astRewrite.getAST();
		MethodInvocation nullValueInvocation = ast.newMethodInvocation();
		nullValueInvocation.setName(ast.newSimpleName("nullValue")); //$NON-NLS-1$
		Name qualifier = addImportForStaticMethod(ORG_HAMCREST_CORE_MATCHERS_NULL_VALUE, methodInvocation).orElse(null);
		if (qualifier != null) {
			nullValueInvocation.setExpression(qualifier);
		}
		newArguments.add(nullValueInvocation);
		return newArguments;
	}

	private List<Expression> createAssumeThatListIsNotNullArguments(MethodInvocation methodInvocation,
			List<Expression> originalArguments) {
		List<Expression> newArguments = new ArrayList<>();
		return newArguments;
	}
}