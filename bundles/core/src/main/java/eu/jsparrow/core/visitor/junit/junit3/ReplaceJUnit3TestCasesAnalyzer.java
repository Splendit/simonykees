package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class ReplaceJUnit3TestCasesAnalyzer {

	Optional<ReplaceJUnit3TestCasesAnalysisData> collectTestCaseDeclarationData(
			JUnit3DataCollectorVisitor junit3DataCollectorVisitor,
			Junit3MigrationConfiguration migrationConfiguration) {
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

}