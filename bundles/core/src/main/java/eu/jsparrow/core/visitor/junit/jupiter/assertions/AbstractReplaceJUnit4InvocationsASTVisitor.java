package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodInvocationsCollectorVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Parent class of visitors which transform invocations of JUnit4 methods
 * declared in one of the following classes:
 * <ul>
 * <li>{@code org.junit.Assert},</li>
 * <li>{@code org.junit.Assume}.</li>
 * </ul>
 * <p>
 * 
 * @since 3.31.0
 *
 */
abstract class AbstractReplaceJUnit4InvocationsASTVisitor extends AbstractAddImportASTVisitor {

	protected static final String ORG_J_UNIT_JUPITER_API_ASSUMPTIONS = "org.junit.jupiter.api.Assumptions"; //$NON-NLS-1$
	protected static final String ORG_J_UNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
	protected final String classDeclaringJUnit4MethodReplacement;

	AbstractReplaceJUnit4InvocationsASTVisitor(String classDeclaringJUnit4MethodReplacement) {
		this.classDeclaringJUnit4MethodReplacement = classDeclaringJUnit4MethodReplacement;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		super.visit(compilationUnit);
		verifyImport(compilationUnit, classDeclaringJUnit4MethodReplacement);

		List<JUnit4InvocationReplacementAnalysis> methodInvocationAnalysisResults = new ArrayList<>();
		List<MethodInvocation> notTransformedJUnit4Invocations = new ArrayList<>();

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		for (MethodInvocation methodInvocation : invocationCollectorVisitor.getMethodInvocations()) {

			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			if (methodBinding != null && isSupportedJUnit4Method(methodBinding)) {
				JUnit4InvocationReplacementAnalysis result = null;

				List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
						Expression.class);
				if (arguments.stream()
					.allMatch(this::isArgumentWithExplicitType)) {
					result = findAnalysisResult(methodInvocation, methodBinding, arguments)
						.orElse(null);
				}

				if (result != null) {
					methodInvocationAnalysisResults.add(result);
				} else {
					notTransformedJUnit4Invocations.add(methodInvocation);
				}
			}
		}

		List<ImportDeclaration> staticMethodImportsToRemove = collectStaticMethodImportsToRemove(compilationUnit,
				notTransformedJUnit4Invocations);

		Map<String, String> supportedStaticImportsMap = collectSupportedStaticImportsMap(staticMethodImportsToRemove,
				methodInvocationAnalysisResults);

		List<JUnit4InvocationReplacementData> jUnit4AssertTransformationDataList = methodInvocationAnalysisResults
			.stream()
			.filter(data -> !isInvocationRemaningUnchanged(data, supportedStaticImportsMap))
			.map(data -> this.createTransformationData(data, supportedStaticImportsMap))
			.collect(Collectors.toList());

		Set<String> newStaticAssertionMethodImports = new HashSet<>();

		methodInvocationAnalysisResults.stream()
			.map(JUnit4InvocationReplacementAnalysis::getNewMethodName)
			.forEach(newMethodName -> {
				if (supportedStaticImportsMap.containsKey(newMethodName)) {
					newStaticAssertionMethodImports.add(supportedStaticImportsMap.get(newMethodName));
				}
			});
		JUnit4TransformationDataCollections transformationDataCollections = new JUnit4TransformationDataCollections(
				staticMethodImportsToRemove, newStaticAssertionMethodImports, methodInvocationAnalysisResults,
				jUnit4AssertTransformationDataList);

		transform(transformationDataCollections);
		return false;
	}

	private boolean isInvocationRemaningUnchanged(JUnit4InvocationReplacementAnalysis invocationData,
			Map<String, String> supportedStaticImportsMap) {
		if (invocationData.isChangingArguments()) {
			return false;
		}
		String newMethodName = invocationData.getNewMethodName();
		MethodInvocation methodInvocation = invocationData.getMethodInvocation();
		String originalMethodName = invocationData.getOriginalMethodName();
		boolean useNewMethodStaticImport = supportedStaticImportsMap.containsKey(newMethodName);
		boolean keepUnqualified = useNewMethodStaticImport && methodInvocation.getExpression() == null;
		return keepUnqualified && newMethodName.equals(originalMethodName);
	}

	protected JUnit4InvocationReplacementData createTransformationData(
			JUnit4InvocationReplacementAnalysis invocationData,
			Map<String, String> supportedStaticImportsMap) {

		List<Expression> originalArguments = invocationData.getArguments();
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

		Supplier<List<Expression>> newArgumentsSupplier = () -> newArguments.stream()
			.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
			.collect(Collectors.toList());

		return createTransformationData(invocationData, supportedStaticImportsMap, newArgumentsSupplier);
	}

	protected JUnit4InvocationReplacementData createTransformationData(
			JUnit4InvocationReplacementAnalysis invocationData,
			Map<String, String> supportedStaticImportsMap, Supplier<List<Expression>> newArgumentsSupplier) {

		String newMethodName = invocationData.getNewMethodName();
		MethodInvocation methodInvocation = invocationData.getMethodInvocation();

		Supplier<MethodInvocation> newMethodInvocationSupplier;
		boolean useNewMethodStaticImport = supportedStaticImportsMap.containsKey(newMethodName);
		if (useNewMethodStaticImport) {
			newMethodInvocationSupplier = () -> createNewInvocationWithoutQualifier(newMethodName,
					newArgumentsSupplier);
		} else {
			newMethodInvocationSupplier = () -> createNewInvocationWithQualifier(
					methodInvocation,
					newMethodName, newArgumentsSupplier);
		}
		return new JUnit4InvocationReplacementData(invocationData, newMethodInvocationSupplier);
	}

	private boolean isArgumentWithExplicitType(Expression expression) {
		if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			return methodBinding != null && !(methodBinding.isParameterizedMethod() && methodInvocation.typeArguments()
				.isEmpty());
		}
		if (expression.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) expression;
			IMethodBinding superMethodBinding = superMethodInvocation.resolveMethodBinding();
			return superMethodBinding != null
					&& !(superMethodBinding.isParameterizedMethod() && superMethodInvocation.typeArguments()
						.isEmpty());
		}
		return true;
	}

	protected List<ImportDeclaration> collectStaticMethodImportsToRemove(CompilationUnit compilationUnit,
			List<MethodInvocation> notTransformedJUnit4Invocations) {

		Set<String> simpleNamesOfStaticAssertMethodImportsToKeep = notTransformedJUnit4Invocations
			.stream()
			.filter(methodInvocation -> methodInvocation
				.getExpression() == null)
			.map(MethodInvocation::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toSet());

		return ASTNodeUtil
			.convertToTypedList(compilationUnit.imports(), ImportDeclaration.class)
			.stream()
			.filter(importDeclaration -> canRemoveStaticImport(importDeclaration,
					simpleNamesOfStaticAssertMethodImportsToKeep))
			.collect(Collectors.toList());
	}

	private boolean canRemoveStaticImport(ImportDeclaration importDeclaration,
			Set<String> simpleNamesOfStaticAssertMethodImportsToKeep) {
		if (!importDeclaration.isStatic()) {
			return false;
		}

		if (importDeclaration.isOnDemand()) {
			return false;
		}
		IBinding importBinding = importDeclaration.resolveBinding();
		if (importBinding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = ((IMethodBinding) importBinding);
			return isSupportedJUnit4Method(methodBinding)
					&& !simpleNamesOfStaticAssertMethodImportsToKeep.contains(methodBinding.getName());
		}
		return false;
	}

	private Map<String, String> collectSupportedStaticImportsMap(
			List<ImportDeclaration> staticMethodImportsToRemove,
			List<JUnit4InvocationReplacementAnalysis> methodInvocationAnalysisResults) {

		Set<String> simpleNamesOfStaticMethodImportsToRemove = staticMethodImportsToRemove
			.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(QualifiedName.class::cast)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toSet());

		Set<String> supportedNewMethodNames = methodInvocationAnalysisResults
			.stream()
			.map(JUnit4InvocationReplacementAnalysis::getNewMethodName)
			.collect(Collectors.toSet());

		Map<String, String> simpleNameToFullyQualifiedNameMap = new HashMap<>();
		String newMethodFullyQualifiedNamePrefix = classDeclaringJUnit4MethodReplacement + "."; //$NON-NLS-1$
		supportedNewMethodNames.forEach(supportedNewMethodName -> {
			String supportedNewMethodFullyQualifiedName = newMethodFullyQualifiedNamePrefix + supportedNewMethodName;
			if (simpleNamesOfStaticMethodImportsToRemove.contains(supportedNewMethodName)
					|| canAddStaticAssertionsMethodImport(supportedNewMethodFullyQualifiedName)) {
				simpleNameToFullyQualifiedNameMap.put(supportedNewMethodName, supportedNewMethodFullyQualifiedName);
			}
		});

		return simpleNameToFullyQualifiedNameMap;
	}

	private boolean canAddStaticAssertionsMethodImport(String fullyQualifiedAssertionsMethodName) {
		verifyStaticMethodImport(getCompilationUnit(), fullyQualifiedAssertionsMethodName);
		return canAddStaticMethodImport(fullyQualifiedAssertionsMethodName);
	}

	protected void transform(JUnit4TransformationDataCollections transformationDataCollections) {

		List<ImportDeclaration> staticAssertMethodImportsToRemove = transformationDataCollections
			.getStaticAssertMethodImportsToRemove();
		Set<String> newStaticAssertionMethodImports = transformationDataCollections
			.getNewStaticAssertionMethodImports();
		List<JUnit4InvocationReplacementData> jUnit4AssertTransformationDataList = transformationDataCollections
			.getJUnit4AssertTransformationDataList();

		if (!staticAssertMethodImportsToRemove.isEmpty() || !newStaticAssertionMethodImports.isEmpty()
				|| !jUnit4AssertTransformationDataList.isEmpty()) {

			staticAssertMethodImportsToRemove.forEach(importDeclaration -> {
				astRewrite.remove(importDeclaration, null);
				onRewrite();
			});

			AST ast = astRewrite.getAST();
			ListRewrite newImportsListRewrite = astRewrite.getListRewrite(getCompilationUnit(),
					CompilationUnit.IMPORTS_PROPERTY);

			newStaticAssertionMethodImports
				.forEach(fullyQualifiedMethodName -> {
					ImportDeclaration newImportDeclaration = ast.newImportDeclaration();
					newImportDeclaration.setName(ast.newName(fullyQualifiedMethodName));
					newImportDeclaration.setStatic(true);
					newImportsListRewrite.insertLast(newImportDeclaration, null);
				});

			jUnit4AssertTransformationDataList
				.forEach(data -> {
					MethodInvocation methodInvocationToReplace = data.getOriginalMethodInvocation();
					MethodInvocation methodInvocationReplacement = data.createMethodInvocationReplacement();
					astRewrite.replace(methodInvocationToReplace, methodInvocationReplacement, null);
					onRewrite();
				});
		}
	}

	@SuppressWarnings({ "unchecked" })
	protected MethodInvocation createNewInvocationWithoutQualifier(String newMethodName,
			Supplier<List<Expression>> newArgumentsSupplier) {
		AST ast = astRewrite.getAST();
		MethodInvocation newInvocation = ast.newMethodInvocation();
		newInvocation.setName(ast.newSimpleName(newMethodName));
		List<Expression> newInvocationArguments = newInvocation.arguments();
		newInvocationArguments.addAll(newArgumentsSupplier.get());
		return newInvocation;
	}

	protected MethodInvocation createNewInvocationWithQualifier(MethodInvocation contextForImport,
			String newMethodName, Supplier<List<Expression>> newArgumentsSupplier) {
		MethodInvocation newInvocation = createNewInvocationWithoutQualifier(newMethodName, newArgumentsSupplier);
		Name newQualifier = addImport(classDeclaringJUnit4MethodReplacement, contextForImport);
		newInvocation.setExpression(newQualifier);
		return newInvocation;
	}

	protected abstract boolean isSupportedJUnit4Method(IMethodBinding methodBinding);

	protected abstract Optional<JUnit4InvocationReplacementAnalysis> findAnalysisResult(
			MethodInvocation methodInvocation, IMethodBinding methodBinding, List<Expression> arguments);

}
