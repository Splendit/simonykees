package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

abstract class AbstractReplaceJUnit4MethodInvocationsASTVisitor extends AbstractAddImportASTVisitor {

	protected final String classDeclaringJUnitJupiterMethod;

	AbstractReplaceJUnit4MethodInvocationsASTVisitor(String classDeclaringJUnitJupiterMethod) {
		super();
		this.classDeclaringJUnitJupiterMethod = classDeclaringJUnitJupiterMethod;
	}

	protected JUnit4MethodInvocationAnalysisResultStore createTransformationDataStore(CompilationUnit compilationUnit) {
		JUnit4MethodInvocationAnalyzer analyzer = new JUnit4MethodInvocationAnalyzer(compilationUnit,
				this::isSupportedJUnit4Method);
		return analyzer.collectAnalysisResults();
	}

	protected List<ImportDeclaration> collectStaticMethodImportsToRemove(CompilationUnit compilationUnit,
			JUnit4MethodInvocationAnalysisResultStore transformationDataStore) {

		Set<String> simpleNamesOfStaticAssertMethodImportsToKeep = transformationDataStore
			.getNotTransformedJUnt4MethodInvocations()
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

	protected Set<String> findSupportedStaticImports(
			List<ImportDeclaration> staticMethodImportsToRemove,
			JUnit4MethodInvocationAnalysisResultStore transformationDataStore) {

		Set<String> simpleNamesOfStaticMethodImportsToRemove = staticMethodImportsToRemove
			.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(QualifiedName.class::cast)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toSet());

		Set<String> supportedNewMethodNames = collectSupportedNewMethodNames(transformationDataStore);

		Set<String> supportedNewStaticMethodImports = new HashSet<>();
		String newMethodFullyQualifiedNamePrefix = classDeclaringJUnitJupiterMethod + "."; //$NON-NLS-1$
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
			JUnit4MethodInvocationAnalysisResultStore transformationDataStore) {

		List<JUnit4MethodInvocationAnalysisResult> simpleAnalysisResultList = new ArrayList<>();

		simpleAnalysisResultList.addAll(transformationDataStore.getMethodInvocationAnalysisResults());
		transformationDataStore.getAssertThrowsInvocationAnalysisResults()
			.stream()
			.map(JUnit4AssertThrowsInvocationAnalysisResult::getJUnit4InvocationData)
			.forEach(simpleAnalysisResultList::add);

		transformationDataStore.getAssumeNotNullInvocationAnalysisResults()
			.stream()
			.map(JUnit4AssumeNotNullInvocationAnalysisResult::getJUnit4InvocationData)
			.forEach(simpleAnalysisResultList::add);

		Set<String> supportedNewMethodSimpleNames = new HashSet<>();
		simpleAnalysisResultList.stream()
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

	protected abstract Set<String> getSupportedMethodNameReplacements();

	protected abstract boolean isSupportedJUnit4Method(IMethodBinding methodBinding);
}
