package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodInvocationsCollectorVisitor;

class JUnit4MethodInvocationAnalysisResultStore {
	private final List<JUnit4MethodInvocationAnalysisResult> supportedJUnit4InvocationDataList = new ArrayList<>();
	private final List<Type> throwingRunnableTypesToReplace = new ArrayList<>();

	JUnit4MethodInvocationAnalysisResultStore(CompilationUnit compilationUnit,
			Predicate<IMethodBinding> supportedJUnit4MethodPredicate, boolean isAssertionAnalysis) {

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);

		JUnit4MethodInvocationAnalyzer analyzer = new JUnit4MethodInvocationAnalyzer(compilationUnit,
				supportedJUnit4MethodPredicate);

		for (MethodInvocation methodInvocation : invocationCollectorVisitor.getMethodInvocations()) {

			JUnit4AssertThrowsInvocationAnalysisResult assertThrowsAnalysisResult = isAssertionAnalysis
					? analyzer.findAssertThrowsAnalysisResult(methodInvocation)
						.orElse(null)
					: null;
			if (assertThrowsAnalysisResult != null) {
				supportedJUnit4InvocationDataList.add(assertThrowsAnalysisResult.getJUnit4InvocationData());
				assertThrowsAnalysisResult.getTypeOfThrowingRunnableToReplace()
					.ifPresent(throwingRunnableTypesToReplace::add);
			} else {
				analyzer.findAnalysisResult(methodInvocation)
					.ifPresent(supportedJUnit4InvocationDataList::add);
			}
		}
	}

	List<JUnit4MethodInvocationAnalysisResult> getSupportedJUnit4InvocationDataList() {
		return supportedJUnit4InvocationDataList;
	}

	List<Type> getThrowingRunnableTypesToReplace() {
		return throwingRunnableTypesToReplace;
	}
}
