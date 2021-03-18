package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.api.Assertions;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.JdtUnitException;

public abstract class AbstractReplaceJUnit4WithJupiterASTVisitorTest extends UsesJDTUnitFixture {
	
	protected void assertChange(String original, String expected, List<String> expectedImportsToString)
			throws JavaModelException, JdtUnitException, BadLocationException {
		assertChange(original, expected);
		List<String> actualImportsToString = defaultFixture.getImports()
			.stream()
			.map(ImportDeclaration::toString)
			.map(String::trim)
			.collect(Collectors.toList());
		expectedImportsToString.sort((left, right) -> left.compareTo(right));
		actualImportsToString.sort((left, right) -> left.compareTo(right));
		Assertions.assertEquals(expectedImportsToString, actualImportsToString);
	}
	
	@Override
	protected void assertNoChange(String original)
			throws JavaModelException, JdtUnitException, BadLocationException {
		List<String> expectedImportsToString = defaultFixture.getImports()
			.stream()
			.map(ImportDeclaration::toString)
			.map(String::trim)
			.collect(Collectors.toList());
		assertChange(original, original, expectedImportsToString);
	}
}
