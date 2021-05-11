package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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
	private static final String AS_LIST = "asList"; //$NON-NLS-1$
	private static final String JAVA_UTIL_ARRAYS = "java.util.Arrays"; //$NON-NLS-1$
	private static final String NULL_VALUE = "nullValue"; //$NON-NLS-1$
	private static final String NOT_NULL_VALUE = "notNullValue"; //$NON-NLS-1$
	private static final String EVERY_ITEM = "everyItem"; //$NON-NLS-1$
	private static final String ORG_HAMCREST_CORE_MATCHERS = "org.hamcrest.CoreMatchers"; //$NON-NLS-1$
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
	protected void verifyImports(CompilationUnit compilationUnit) {
		verifyImport(compilationUnit, classDeclaringJUnit4MethodReplacement);
		verifyImport(compilationUnit, ORG_HAMCREST_CORE_MATCHERS);
		verifyStaticMethodImport(compilationUnit, ORG_HAMCREST_CORE_MATCHERS + '.' + NULL_VALUE);
		verifyStaticMethodImport(compilationUnit, ORG_HAMCREST_CORE_MATCHERS + '.' + NOT_NULL_VALUE);
		verifyStaticMethodImport(compilationUnit, ORG_HAMCREST_CORE_MATCHERS + '.' + EVERY_ITEM);
		verifyStaticMethodImport(compilationUnit, JAVA_UTIL_ARRAYS + '.' + AS_LIST);
	}

	@Override
	protected void transform(List<ImportDeclaration> staticAssertMethodImportsToRemove,
			Set<String> newStaticAssertionMethodImports,
			List<JUnit4MethodInvocationReplacementData> jUnit4AssertTransformationDataList) {
		super.transform(staticAssertMethodImportsToRemove, newStaticAssertionMethodImports,
				jUnit4AssertTransformationDataList);

		boolean qualifierNeededForAssumeThat = newStaticAssertionMethodImports.stream()
			.noneMatch(fullyQualifiedName -> fullyQualifiedName.endsWith('.' + ASSUME_THAT));

		jUnit4AssertTransformationDataList.stream()
			.map(JUnit4MethodInvocationReplacementData::getAssumptionThatEveryItemNotNull)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.forEach(data -> insertAssumptionThatEveryItemNotNull(data, qualifierNeededForAssumeThat));
	}

	@Override
	protected Optional<JUnit4MethodInvocationAnalysisResult> findAnalysisResult(MethodInvocation methodInvocation,
			IMethodBinding methodBinding, List<Expression> arguments) {
		return analyzer.analyzeAssumptionToHamcrest(methodInvocation, methodBinding, arguments);
	}

	@Override
	protected JUnit4MethodInvocationReplacementData createTransformationData(
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
			return new JUnit4MethodInvocationReplacementData(invocationData, newMethodStaticImport);
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
			return new JUnit4MethodInvocationReplacementData(invocationData,
					() -> createNewInvocationWithoutQualifier(newMethodName, newArgumentsSupplier),
					newMethodStaticImport);
		}
		Supplier<MethodInvocation> newMethodInvocationSupplier = () -> createNewInvocationWithQualifier(
				methodInvocation,
				newMethodName, newArgumentsSupplier);

		return new JUnit4MethodInvocationReplacementData(invocationData, newMethodInvocationSupplier);
	}

	@Override
	protected boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		if (isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assume")) {//$NON-NLS-1$
			String methodName = methodBinding.getName();
			return methodName.equals(ASSUME_NO_EXCEPTION) ||
					methodName.equals(ASSUME_NOT_NULL) ||
					methodName.equals(ASSUME_THAT);
		}
		return false;
	}

	@Override
	protected Set<String> getSupportedMethodNameReplacements() {
		return potentialMethodNameReplacements;
	}

	private List<Expression> createAssumeThatExceptionIsNullArguments(ASTNode context,
			List<Expression> originalArguments) {
		List<Expression> newArguments = new ArrayList<>();
		originalArguments.stream()
			.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
			.forEach(newArguments::add);
		MethodInvocation nullValueInvocation = createCoreMatchersInvocation(context, NULL_VALUE);
		newArguments.add(nullValueInvocation);
		return newArguments;
	}

	private List<Expression> createAssumeThatListIsNotNullArguments(ASTNode context,
			List<Expression> originalArguments) {

		if (originalArguments.size() != 1 || originalArguments.get(0)
			.getNodeType() == ASTNode.ARRAY_CREATION) {

			MethodInvocation asListInvocation = createAsListInvocation(context, originalArguments);
			return Arrays.<Expression>asList(asListInvocation,
					createCoreMatchersInvocation(context, EVERY_ITEM));
		}
		return Arrays.<Expression>asList(
				(Expression) astRewrite.createCopyTarget(originalArguments.get(0)),
				createCoreMatchersInvocation(context, NOT_NULL_VALUE));

	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createAsListInvocation(ASTNode context, List<Expression> originalArguments) {
		AST ast = astRewrite.getAST();
		MethodInvocation asListInvocation = ast.newMethodInvocation();
		asListInvocation.setName(ast.newSimpleName(AS_LIST));
		Name qualifier = addImportForStaticMethod(JAVA_UTIL_ARRAYS + '.' + AS_LIST, context).orElse(null);
		if (qualifier != null) {
			asListInvocation.setExpression(qualifier);
		}
		List<Expression> asListArguments = originalArguments.stream()
			.map(arg -> (Expression) astRewrite.createCopyTarget(originalArguments.get(0)))
			.collect(Collectors.toList());
		asListInvocation.arguments()
			.addAll(asListArguments);
		return asListInvocation;
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createCoreMatchersInvocation(ASTNode context, String coreMatchersMethodName) {
		AST ast = astRewrite.getAST();
		MethodInvocation coreMatchersInvocation = ast.newMethodInvocation();
		coreMatchersInvocation.setName(ast.newSimpleName(coreMatchersMethodName));
		Name qualifier = addImportForStaticMethod(ORG_HAMCREST_CORE_MATCHERS + '.' + coreMatchersMethodName, context)
			.orElse(null);
		if (qualifier != null) {
			coreMatchersInvocation.setExpression(qualifier);
		}
		if (coreMatchersMethodName.equals(EVERY_ITEM)) {
			coreMatchersInvocation.arguments()
				.add(createCoreMatchersInvocation(context, NOT_NULL_VALUE));
		}
		return coreMatchersInvocation;
	}

	@SuppressWarnings("unchecked")
	void insertAssumptionThatEveryItemNotNull(AssumptionThatEveryItemNotNull assumptionThatEveryItemNotNull,
			boolean qualifierNeededForAssumeThat) {
		ExpressionStatement assumeNotNullStatement = assumptionThatEveryItemNotNull.getAssumeNotNullStatement();
		List<Expression> asListArguments = Arrays
			.asList(assumptionThatEveryItemNotNull.getAssumeNotNullArrayArgument());
		AST ast = astRewrite.getAST();
		MethodInvocation assumeThatInvocation = ast.newMethodInvocation();
		assumeThatInvocation.setName(ast.newSimpleName(ASSUME_THAT));

		MethodInvocation asListInvocation = createAsListInvocation(assumeNotNullStatement, asListArguments);
		MethodInvocation everyItemInvocation = createCoreMatchersInvocation(assumeNotNullStatement, EVERY_ITEM);
		assumeThatInvocation.arguments()
			.add(asListInvocation);
		assumeThatInvocation.arguments()
			.add(everyItemInvocation);

		if (qualifierNeededForAssumeThat) {
			Name qualifier = addImport(classDeclaringJUnit4MethodReplacement, assumeNotNullStatement);
			assumeThatInvocation.setExpression(qualifier);
		}

		ExpressionStatement assumeThatStatement = ast.newExpressionStatement(assumeThatInvocation);

		ListRewrite listRewrite = astRewrite.getListRewrite(
				assumptionThatEveryItemNotNull.getAssumeNotNullStatementParent(),
				Block.STATEMENTS_PROPERTY);
		listRewrite.insertAfter(assumeThatStatement, assumeNotNullStatement, null);
	}
}