package eu.jsparrow.core.visitor.junit.junit3;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;

public class ReplaceJUnit3TestCasesAnalysisData {
	private final List<TestMethodAnnotationData> testMethodAnnotationDataList;
	private final List<JUnit3AssertionAnalysisResult> assertionAnalysisResults;
	private final List<SimpleType> jUnit3TestCaseSuperTypesToRemove;
	private MethodDeclaration mainMethodToRemove;

	public ReplaceJUnit3TestCasesAnalysisData(List<TestMethodAnnotationData> testMethodAnnotationDataList,
			List<JUnit3AssertionAnalysisResult> assertionAnalysisResults,
			List<SimpleType> jUnit3TestCaseSuperTypesToRemove, MethodDeclaration mainMethodToRemove) {
		this(testMethodAnnotationDataList, assertionAnalysisResults, jUnit3TestCaseSuperTypesToRemove);
		this.mainMethodToRemove = mainMethodToRemove;
	}

	public ReplaceJUnit3TestCasesAnalysisData(List<TestMethodAnnotationData> testMethodAnnotationDataList,
			List<JUnit3AssertionAnalysisResult> assertionAnalysisResults,
			List<SimpleType> jUnit3TestCaseSuperTypesToRemove) {
		this.testMethodAnnotationDataList = testMethodAnnotationDataList;
		this.assertionAnalysisResults = assertionAnalysisResults;
		this.jUnit3TestCaseSuperTypesToRemove = jUnit3TestCaseSuperTypesToRemove;
	}

	public List<TestMethodAnnotationData> getTestMethodAnnotationDataList() {
		return testMethodAnnotationDataList;
	}

	public List<JUnit3AssertionAnalysisResult> getAssertionAnalysisResults() {
		return assertionAnalysisResults;
	}

	public Optional<MethodDeclaration> getMainMethodToRemove() {
		return Optional.ofNullable(mainMethodToRemove);
	}

	public List<SimpleType> getJUnit3TestCaseSuperTypesToRemove() {
		return jUnit3TestCaseSuperTypesToRemove;
	}
}
