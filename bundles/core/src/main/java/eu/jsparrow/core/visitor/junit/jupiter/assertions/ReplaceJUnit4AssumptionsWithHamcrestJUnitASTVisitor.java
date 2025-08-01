package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * Replaces invocations of the JUnit 4 methods invocations
 * <ul>
 * <li>{@code org.junit.Assume.assumeNoException},</li>
 * <li>{@code org.junit.Assume.assumeNotNull} and</li>
 * <li>{@code org.junit.Assume.assumeThat}</li>
 * </ul>
 * by corresponding invocations of
 * {@code org.hamcrest.junit.MatcherAssume.assumeThat}.
 * 
 * @since 4.0.0
 * 
 */
public class ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor
		extends AbstractReplaceJUnit4InvocationsASTVisitor {
	private static final String AS_LIST = "asList"; //$NON-NLS-1$
	private static final String JAVA_UTIL_ARRAYS = java.util.Arrays.class.getName();
	private static final String NULL_VALUE = "nullValue"; //$NON-NLS-1$
	private static final String NOT_NULL_VALUE = "notNullValue"; //$NON-NLS-1$
	private static final String EVERY_ITEM = "everyItem"; //$NON-NLS-1$
	private static final String ORG_HAMCREST_CORE_MATCHERS = "org.hamcrest.CoreMatchers"; //$NON-NLS-1$
	private static final String ASSUME_NOT_NULL = "assumeNotNull"; //$NON-NLS-1$
	private static final String ASSUME_NO_EXCEPTION = "assumeNoException"; //$NON-NLS-1$
	private static final String ASSUME_THAT = "assumeThat"; //$NON-NLS-1$

	public ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor() {
		super("org.hamcrest.junit.MatcherAssume"); //$NON-NLS-1$
	}

	@Override
	protected Optional<JUnit4InvocationReplacementAnalysis> findAnalysisResult(MethodInvocation methodInvocation,
			IMethodBinding methodBinding) {
		return super.findAnalysisResult(methodInvocation, methodBinding)
			.filter(JUnit4InvocationReplacementAnalysis::analyzeAssumptionToHamcrest);
	}

	@Override
	protected void transform(JUnit4TransformationDataCollections transformationDataCollections) {

		verifyImport(getCompilationUnit(), JAVA_UTIL_ARRAYS);
		verifyImport(getCompilationUnit(), ORG_HAMCREST_CORE_MATCHERS);
		verifyStaticMethodImport(getCompilationUnit(), ORG_HAMCREST_CORE_MATCHERS + '.' + NULL_VALUE);
		verifyStaticMethodImport(getCompilationUnit(), ORG_HAMCREST_CORE_MATCHERS + '.' + NOT_NULL_VALUE);
		verifyStaticMethodImport(getCompilationUnit(), ORG_HAMCREST_CORE_MATCHERS + '.' + EVERY_ITEM);
		verifyStaticMethodImport(getCompilationUnit(), JAVA_UTIL_ARRAYS + '.' + AS_LIST);

		super.transform(transformationDataCollections);

		boolean qualifierNeededForAssumeThat = transformationDataCollections.getNewStaticAssertionMethodImports()
			.stream()
			.noneMatch(fullyQualifiedName -> fullyQualifiedName.endsWith('.' + ASSUME_THAT));

		transformationDataCollections
			.getNotNullAssumptionsOnNullableArray()
			.forEach(data -> insertAssumptionThatEveryItemNotNull(data, qualifierNeededForAssumeThat));
	}

	@Override
	protected JUnit4InvocationReplacementData createTransformationData(
			JUnit4InvocationReplacementAnalysis invocationData,
			Map<String, String> supportedStaticImportsMap) {

		MethodInvocation methodInvocation = invocationData.getMethodInvocation();
		String originalMethodName = invocationData.getOriginalMethodName();

		List<Expression> originalArguments = invocationData.getArguments();

		final Supplier<List<Expression>> newArgumentsSupplier;
		AssumeNotNullArgumentsAnalysis assumeNotNullAnalysis = invocationData.getAssumeNotNullArgumentsAnalysis()
			.orElse(null);
		if (assumeNotNullAnalysis != null) {
			newArgumentsSupplier = () -> createAssumeThatListIsNotNullArguments(methodInvocation, originalArguments,
					assumeNotNullAnalysis);

		} else if (originalMethodName.equals(ASSUME_NO_EXCEPTION)) {
			newArgumentsSupplier = () -> createAssumeThatExceptionIsNullArguments(methodInvocation, originalArguments);

		} else {
			newArgumentsSupplier = () -> originalArguments.stream()
				.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
				.collect(Collectors.toList());
		}
		return createTransformationData(invocationData, supportedStaticImportsMap, newArgumentsSupplier);
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
			List<Expression> originalArguments, AssumeNotNullArgumentsAnalysis assumeNotNullAnalysis) {

		if (originalArguments.isEmpty() || assumeNotNullAnalysis.isMultipleVarargs()
				|| assumeNotNullAnalysis.isSingleVarargArrayCreation()) {
			MethodInvocation asListInvocation = createAsListInvocation(context, originalArguments);
			return Arrays.<Expression>asList(asListInvocation, createCoreMatchersInvocation(context, EVERY_ITEM));
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
			.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
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
	void insertAssumptionThatEveryItemNotNull(AssumeNotNullWithNullableArray assumptionThatEveryItemNotNull,
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