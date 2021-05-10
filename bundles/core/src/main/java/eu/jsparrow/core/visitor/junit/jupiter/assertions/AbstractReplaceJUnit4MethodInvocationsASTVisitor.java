package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodInvocationsCollectorVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

abstract class AbstractReplaceJUnit4MethodInvocationsASTVisitor extends AbstractAddImportASTVisitor {

	protected static final String ORG_J_UNIT_JUPITER_API_ASSUMPTIONS = "org.junit.jupiter.api.Assumptions"; //$NON-NLS-1$
	protected static final String ORG_J_UNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
	protected final String classDeclaringJUnit4MethodReplacement;

	AbstractReplaceJUnit4MethodInvocationsASTVisitor(String classDeclaringJUnit4MethodReplacement) {
		this.classDeclaringJUnit4MethodReplacement = classDeclaringJUnit4MethodReplacement;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		super.visit(compilationUnit);

		verifyImports(compilationUnit);

		List<JUnit4MethodInvocationAnalysisResult> allSupportedJUnit4InvocationDataList = collectJUnit4MethodInvocationAnalysisResult(
				compilationUnit);

		List<ImportDeclaration> staticMethodImportsToRemove = collectStaticMethodImportsToRemove(compilationUnit,
				allSupportedJUnit4InvocationDataList);

		Set<String> supportedNewStaticMethodImports = findSupportedStaticImports(staticMethodImportsToRemove,
				allSupportedJUnit4InvocationDataList);

		List<JUnit4MethodInvocationReplacementData> jUnit4AssertTransformationDataList = allSupportedJUnit4InvocationDataList
			.stream()
			.filter(JUnit4MethodInvocationAnalysisResult::isTransformable)
			.map(data -> this.createTransformationData(data, supportedNewStaticMethodImports))
			.collect(Collectors.toList());

		Set<String> newStaticAssertionMethodImports = jUnit4AssertTransformationDataList.stream()
			.map(JUnit4MethodInvocationReplacementData::getStaticMethodImport)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());

		transform(staticMethodImportsToRemove, newStaticAssertionMethodImports, jUnit4AssertTransformationDataList);
		return false;
	}

	List<JUnit4MethodInvocationAnalysisResult> collectJUnit4MethodInvocationAnalysisResult(
			CompilationUnit compilationUnit) {

		JUnit4MethodInvocationAnalyzer analyzer = new JUnit4MethodInvocationAnalyzer(compilationUnit);
		List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults = new ArrayList<>();

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		invocationCollectorVisitor.getMethodInvocations()
			.forEach(methodInvocation -> {
				IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
				List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
						Expression.class);
				if (methodBinding != null && isSupportedJUnit4Method(methodBinding)) {
					JUnit4MethodInvocationAnalysisResult result = findAnalysisResult(analyzer, methodInvocation,
							methodBinding,
							arguments);
					methodInvocationAnalysisResults.add(result);
				}
			});
		return methodInvocationAnalysisResults;
	}

	protected List<ImportDeclaration> collectStaticMethodImportsToRemove(CompilationUnit compilationUnit,
			List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults) {

		Set<String> simpleNamesOfStaticAssertMethodImportsToKeep = methodInvocationAnalysisResults.stream()
			.filter(result -> !result.isTransformable())
			.map(JUnit4MethodInvocationAnalysisResult::getMethodInvocation)
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

	protected Set<String> findSupportedStaticImports(
			List<ImportDeclaration> staticMethodImportsToRemove,
			List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults) {

		Set<String> simpleNamesOfStaticMethodImportsToRemove = staticMethodImportsToRemove
			.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(QualifiedName.class::cast)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toSet());

		Set<String> supportedNewMethodNames = collectSupportedNewMethodNames(methodInvocationAnalysisResults);

		Set<String> supportedNewStaticMethodImports = new HashSet<>();
		String newMethodFullyQualifiedNamePrefix = classDeclaringJUnit4MethodReplacement + "."; //$NON-NLS-1$
		supportedNewMethodNames.forEach(supportedNewMethodName -> {
			String supportedNewMethodFullyQualifiedName = newMethodFullyQualifiedNamePrefix + supportedNewMethodName;
			if (simpleNamesOfStaticMethodImportsToRemove.contains(supportedNewMethodName)
					|| canAddStaticAssertionsMethodImport(supportedNewMethodFullyQualifiedName)) {
				supportedNewStaticMethodImports.add(supportedNewMethodFullyQualifiedName);
			}
		});

		return supportedNewStaticMethodImports;
	}

	private Set<String> collectSupportedNewMethodNames(
			List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults) {

		Set<String> supportedNewMethodSimpleNames = new HashSet<>();
		methodInvocationAnalysisResults
			.stream()
			.filter(JUnit4MethodInvocationAnalysisResult::isTransformable)
			.map(JUnit4MethodInvocationAnalysisResult::getMethodBinding)
			.map(IMethodBinding::getName)
			.forEach(supportedNewMethodSimpleNames::add);

		supportedNewMethodSimpleNames.addAll(getSupportedMethodNameReplacements());

		return supportedNewMethodSimpleNames;
	}

	private boolean canAddStaticAssertionsMethodImport(String fullyQualifiedAssertionsMethodName) {
		verifyStaticMethodImport(getCompilationUnit(), fullyQualifiedAssertionsMethodName);
		return canAddStaticMethodImport(fullyQualifiedAssertionsMethodName);
	}

	protected void transform(List<ImportDeclaration> staticAssertMethodImportsToRemove,
			Set<String> newStaticAssertionMethodImports,
			List<JUnit4MethodInvocationReplacementData> jUnit4AssertTransformationDataList) {
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

			jUnit4AssertTransformationDataList.forEach(data -> {
				MethodInvocation methodInvocationReplacement = data.createMethodInvocationReplacement()
					.orElse(null);
				if (methodInvocationReplacement != null) {
					MethodInvocation methodInvocationToReplace = data.getOriginalMethodInvocation();
					astRewrite.replace(methodInvocationToReplace, methodInvocationReplacement, null);
					onRewrite();
				}
			});
		}
	}

	protected List<Expression> createNewMethodArguments(List<Expression> arguments) {
		return arguments.stream()
			.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
			.collect(Collectors.toList());
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

	protected abstract void verifyImports(CompilationUnit compilationUnit);

	protected abstract Set<String> getSupportedMethodNameReplacements();

	protected abstract boolean isSupportedJUnit4Method(IMethodBinding methodBinding);

	protected abstract JUnit4MethodInvocationAnalysisResult findAnalysisResult(JUnit4MethodInvocationAnalyzer analyzer,
			MethodInvocation methodInvocation, IMethodBinding methodBinding, List<Expression> arguments);

	protected abstract JUnit4MethodInvocationReplacementData createTransformationData(
			JUnit4MethodInvocationAnalysisResult invocationData,
			Set<String> supportedNewStaticMethodImports);

}
