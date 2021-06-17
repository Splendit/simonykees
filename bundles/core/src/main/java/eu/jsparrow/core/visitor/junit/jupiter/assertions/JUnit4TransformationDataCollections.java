package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Type;

/**
 * Stores a separate list for each kind of data needed for the transformation of
 * <ul>
 * <li>JUnit 4 assertions to JUnit Jupiter or</li>
 * <li>JUnit 4 assumptions to JUnit Jupiter or</li>
 * <li>JUnit 4 assumptions to Hamcrest JUnit</li>
 * </ul>
 * which can be found in a given compilation unit.
 * <p>
 * 
 * @since 4.0.0
 *
 */
public class JUnit4TransformationDataCollections {

	private final List<ImportDeclaration> staticAssertMethodImportsToRemove;
	private final Set<String> newStaticAssertionMethodImports;
	private final List<JUnit4InvocationReplacementData> jUnit4AssertTransformationDataList;
	private final List<Type> throwingRunnableTypesToReplace;
	private final List<AssumeNotNullWithNullableArray> notNullAssumptionsOnNullableArray;

	public JUnit4TransformationDataCollections(List<ImportDeclaration> staticAssertMethodImportsToRemove,
			Set<String> newStaticAssertionMethodImports,
			List<JUnit4InvocationReplacementAnalysis> methodInvocationAnalysisResults,
			List<JUnit4InvocationReplacementData> jUnit4AssertTransformationDataList) {

		this.staticAssertMethodImportsToRemove = staticAssertMethodImportsToRemove;
		this.newStaticAssertionMethodImports = newStaticAssertionMethodImports;
		this.jUnit4AssertTransformationDataList = jUnit4AssertTransformationDataList;

		throwingRunnableTypesToReplace = methodInvocationAnalysisResults
			.stream()
			.map(JUnit4InvocationReplacementAnalysis::getTypeOfThrowingRunnableToReplace)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		notNullAssumptionsOnNullableArray = methodInvocationAnalysisResults
			.stream()
			.map(JUnit4InvocationReplacementAnalysis::getAssumeNotNullWithNullableArray)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	public List<ImportDeclaration> getStaticAssertMethodImportsToRemove() {
		return staticAssertMethodImportsToRemove;
	}

	public Set<String> getNewStaticAssertionMethodImports() {
		return newStaticAssertionMethodImports;
	}

	public List<JUnit4InvocationReplacementData> getJUnit4AssertTransformationDataList() {
		return jUnit4AssertTransformationDataList;
	}

	public List<Type> getThrowingRunnableTypesToReplace() {
		return throwingRunnableTypesToReplace;
	}

	public List<AssumeNotNullWithNullableArray> getNotNullAssumptionsOnNullableArray() {
		return notNullAssumptionsOnNullableArray;
	}
}
