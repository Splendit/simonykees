package eu.jsparrow.core.visitor.junit.junit3;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ReplaceJUnit3TestCasesAnalysisData {
	private final List<TestMethodAnnotationData> testMethodAnnotationDataList;
	private final List<JUnit3AssertionAnalysisResult> assertionAnalysisResults;
	private MethodDeclaration mainMethodToRemove;

	public ReplaceJUnit3TestCasesAnalysisData(List<TestMethodAnnotationData> testMethodAnnotationDataList,
			List<JUnit3AssertionAnalysisResult> assertionAnalysisResults, MethodDeclaration mainMethodToRemove) {
		this(testMethodAnnotationDataList, assertionAnalysisResults);
		this.mainMethodToRemove = mainMethodToRemove;
	}

	public ReplaceJUnit3TestCasesAnalysisData(List<TestMethodAnnotationData> testMethodAnnotationDataList,
			List<JUnit3AssertionAnalysisResult> assertionAnalysisResults) {
		this.testMethodAnnotationDataList = testMethodAnnotationDataList;
		this.assertionAnalysisResults = assertionAnalysisResults;
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

}
