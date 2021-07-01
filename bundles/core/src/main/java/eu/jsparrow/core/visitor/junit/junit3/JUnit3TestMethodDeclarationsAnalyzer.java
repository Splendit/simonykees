package eu.jsparrow.core.visitor.junit.junit3;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class JUnit3TestMethodDeclarationsAnalyzer {
	static final String SET_UP = "setUp"; //$NON-NLS-1$
	static final String TEAR_DOWN = "tearDown"; //$NON-NLS-1$
	static final String TEST = "test"; //$NON-NLS-1$

	private final List<MethodDeclaration> jUnit3TestMethodDeclarations = new ArrayList<>();
	private final List<TestMethodAnnotationData> testMethodAnnotationDataList = new ArrayList<>();

	boolean collectMethodDeclarationAnalysisData(JUnit3DataCollectorVisitor junit3DataCollectorVisitor,
			JUnit3TestCaseDeclarationsAnalyzer replaceJUnit3TestCasesAnalyzer,
			Junit3MigrationConfiguration migrationConfiguration) {
		List<MethodDeclaration> methodDeclarationsToAnalyze = junit3DataCollectorVisitor
			.getMethodDeclarationsToAnalyze();
		List<TypeDeclaration> typeDeclarationsToAnalyze = junit3DataCollectorVisitor.getTypeDeclarationsToAnalyze();
		List<TypeDeclaration> jUnit3TestCaseDeclarations = replaceJUnit3TestCasesAnalyzer
			.getJUnit3TestCaseDeclarations();

		for (MethodDeclaration methodDeclaration : methodDeclarationsToAnalyze) {
			if (isTestMethodDeclaration(jUnit3TestCaseDeclarations, methodDeclaration)) {
				jUnit3TestMethodDeclarations.add(methodDeclaration);
				testMethodAnnotationDataList
					.add(createTestMethodAnnotationData(methodDeclaration, migrationConfiguration));
			} else if (isUnexpectedMethodDeclaration(typeDeclarationsToAnalyze, methodDeclaration)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isTestMethodDeclaration(List<TypeDeclaration> jUnit3TestCaseDeclarations,
			MethodDeclaration methodDeclaration) {
		if (!jUnit3TestCaseDeclarations.contains(methodDeclaration.getParent())) {
			return false;
		}

		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		if (methodBinding.getParameterTypes().length != 0) {
			return false;
		}

		String methodName = methodBinding.getName();
		return methodName.startsWith(TEST) || methodName.equals(SET_UP) || methodName.equals(TEAR_DOWN);
	}

	private TestMethodAnnotationData createTestMethodAnnotationData(MethodDeclaration methodDeclaration,
			Junit3MigrationConfiguration migrationConfiguration) {
		String methodName = methodDeclaration.getName()
			.getIdentifier();
		String annotationQualifiedName;
		if (methodName.equals(SET_UP)) {
			annotationQualifiedName = migrationConfiguration.getSetupAnnotationQualifiedName();
		} else if (methodName.equals(TEAR_DOWN)) {
			annotationQualifiedName = migrationConfiguration.getTeardownAnnotationQualifiedName();
		} else {
			annotationQualifiedName = migrationConfiguration.getTestAnnotationQualifiedName();
		}
		return new TestMethodAnnotationData(methodDeclaration, annotationQualifiedName);
	}

	private static boolean isUnexpectedMethodDeclaration(List<TypeDeclaration> jUnit3TestCaseDeclarations,
			MethodDeclaration methodDeclaration) {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();

		if (!jUnit3TestCaseDeclarations.contains(methodDeclaration.getParent())) {
			return UnexpectedJunit3References.hasUnexpectedJUnitReference(methodBinding);
		}
		// TODO:
		// Unexpected Method declarations are Method declarations overriding the
		// following methods of TestCase
		//
		// countTestCases()
		// createResult()
		// getName()
		// run()
		// run(TestResult result)
		// runBare()
		// runTest()
		// setName(java.lang.String name)
		// toString()

		return false;
	}

	public List<MethodDeclaration> getJUnit3TestMethodDeclarations() {
		return jUnit3TestMethodDeclarations;
	}

	public List<TestMethodAnnotationData> getTestMethodAnnotationDataList() {
		return testMethodAnnotationDataList;
	}

}
