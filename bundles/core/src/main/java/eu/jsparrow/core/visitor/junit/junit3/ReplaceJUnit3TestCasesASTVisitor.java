package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class ReplaceJUnit3TestCasesASTVisitor extends AbstractAddImportASTVisitor {
	private final Junit3MigrationConfiguration migrationConfiguration;

	public ReplaceJUnit3TestCasesASTVisitor(Junit3MigrationConfiguration migrationConfiguration) {
		this.migrationConfiguration = migrationConfiguration;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		super.visit(compilationUnit);
		String classDeclaringMethodReplacement = migrationConfiguration.getAssertionClassQualifiedName();
		verifyImport(compilationUnit, classDeclaringMethodReplacement);
		verifyStaticMethodImport(compilationUnit, classDeclaringMethodReplacement + ".assertEquals"); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, classDeclaringMethodReplacement + ".assertFalse"); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, classDeclaringMethodReplacement + ".assertTrue"); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, classDeclaringMethodReplacement + ".assertNotNull"); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, classDeclaringMethodReplacement + ".assertNull"); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, classDeclaringMethodReplacement + ".assertNotSame"); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, classDeclaringMethodReplacement + ".assertSame"); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, classDeclaringMethodReplacement + ".fail"); //$NON-NLS-1$

		verifyImport(compilationUnit, migrationConfiguration.getSetupAnnotationQualifiedName());
		verifyImport(compilationUnit, migrationConfiguration.getTeardownAnnotationQualifiedName());
		verifyImport(compilationUnit, migrationConfiguration.getTestAnnotationQualifiedName());

		collectTestCaseDeclarationData(compilationUnit).ifPresent(this::transform);
		return true;
	}

	Optional<ReplaceJUnit3TestCasesAnalysisData> collectTestCaseDeclarationData(CompilationUnit compilationUnit) {

		JUnit3DataCollectorVisitor junit3DataCollectorVisitor = new JUnit3DataCollectorVisitor();
		compilationUnit.accept(junit3DataCollectorVisitor);

		JUnit3TestCaseDeclarationsAnalyzer jUnit3TestCaseDeclarationsAnalyzer = new JUnit3TestCaseDeclarationsAnalyzer();
		boolean transformationPossible = jUnit3TestCaseDeclarationsAnalyzer
			.collectTestCaseDeclarationAnalysisData(junit3DataCollectorVisitor);

		if (!transformationPossible) {
			return Optional.empty();
		}

		JUnit3TestMethodDeclarationsAnalyzer jUnit3TestMethodDeclarationsAnalyzer = new JUnit3TestMethodDeclarationsAnalyzer();
		transformationPossible = jUnit3TestMethodDeclarationsAnalyzer
			.collectMethodDeclarationAnalysisData(junit3DataCollectorVisitor, jUnit3TestCaseDeclarationsAnalyzer,
					migrationConfiguration);

		if (!transformationPossible) {
			return Optional.empty();
		}

		List<MethodInvocation> methodInvocationsToAnalyze = junit3DataCollectorVisitor.getMethodInvocationsToAnalyze();
		String classDeclaringMethodReplacement = migrationConfiguration.getAssertionClassQualifiedName();
		JUnit3AssertionAnalyzer assertionAnalyzer = new JUnit3AssertionAnalyzer(jUnit3TestMethodDeclarationsAnalyzer,
				classDeclaringMethodReplacement);
		List<JUnit3AssertionAnalysisResult> jUnit3AssertionAnalysisResults = new ArrayList<>();

		for (MethodInvocation methodinvocation : methodInvocationsToAnalyze) {
			IMethodBinding methodBinding = methodinvocation.resolveMethodBinding();
			if (methodBinding == null) {
				return Optional.empty();
			}
			JUnit3AssertionAnalysisResult assertionAnalysisResult = assertionAnalyzer
				.findAssertionAnalysisResult(methodinvocation, methodBinding)
				.orElse(null);
			if (assertionAnalysisResult != null) {
				jUnit3AssertionAnalysisResults.add(assertionAnalysisResult);
			} else if (UnexpectedJunit3References.hasUnexpectedJUnitReference(methodBinding)) {
				return Optional.empty();
			}
		}
		List<TestMethodAnnotationData> testMethodAnnotationDataList = jUnit3TestMethodDeclarationsAnalyzer
			.getTestMethodAnnotationDataList();

		List<SimpleType> jUnit3TestCaseSuperTypesToRemove = jUnit3TestCaseDeclarationsAnalyzer
			.getJUnit3TestCaseSuperTypesToRemove();

		MethodDeclaration mainMethodToRemove = junit3DataCollectorVisitor.getMainMethodToRemove()
			.orElse(null);
		if (mainMethodToRemove != null) {
			return Optional.of(new ReplaceJUnit3TestCasesAnalysisData(testMethodAnnotationDataList,
					jUnit3AssertionAnalysisResults, jUnit3TestCaseSuperTypesToRemove, mainMethodToRemove));
		}
		return Optional
			.of(new ReplaceJUnit3TestCasesAnalysisData(testMethodAnnotationDataList, jUnit3AssertionAnalysisResults,
					jUnit3TestCaseSuperTypesToRemove));

	}

	private void transform(ReplaceJUnit3TestCasesAnalysisData analysisData) {

		MethodDeclaration mainMethodToRemove = analysisData.getMainMethodToRemove()
			.orElse(null);

		if (mainMethodToRemove != null) {
			astRewrite.remove(mainMethodToRemove, null);
			onRewrite();
		}

		List<TestMethodAnnotationData> testMethodAnnotationDataList = analysisData.getTestMethodAnnotationDataList();
		testMethodAnnotationDataList.forEach(data -> {
			MethodDeclaration methodDeclaration = data.getMethodDeclaration();
			Name annotationName = addImport(data.getAnnotationQualifiedName(), methodDeclaration);
			AST ast = astRewrite.getAST();
			MarkerAnnotation testMethodAnnotation = ast.newMarkerAnnotation();
			testMethodAnnotation.setTypeName(annotationName);

			ListRewrite listRewrite = astRewrite.getListRewrite(methodDeclaration,
					MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(testMethodAnnotation, null);
			onRewrite();
		});

		List<JUnit3AssertionAnalysisResult> assertionAnalysisResults = analysisData.getAssertionAnalysisResults();
		assertionAnalysisResults.forEach(data -> {
			MethodInvocation methodInvocation = data.getMethodInvocation();
			Expression oldQualifier = methodInvocation.getExpression();
			Name newQualifier = addImport(data.getClassDeclaringMethodReplacement(), methodInvocation);
			if (oldQualifier != null) {
				astRewrite.replace(oldQualifier, newQualifier, null);
			} else {
				astRewrite.set(methodInvocation, MethodInvocation.EXPRESSION_PROPERTY, newQualifier, null);
			}
			Expression messageMovingToLastPosition = data.getMessageMovingToLastPosition()
				.orElse(null);
			if (messageMovingToLastPosition != null) {
				ASTNode messageMoveTarget = astRewrite.createMoveTarget(messageMovingToLastPosition);
				astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY)
					.insertLast(messageMoveTarget, null);
			}
			onRewrite();
		});
	}
}