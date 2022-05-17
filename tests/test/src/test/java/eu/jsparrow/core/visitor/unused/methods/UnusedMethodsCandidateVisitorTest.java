package eu.jsparrow.core.visitor.unused.methods;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.unused.method.NonPrivateUnusedMethodCandidate;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodsCandidateVisitor;

@SuppressWarnings("nls")
class UnusedMethodsCandidateVisitorTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void testNestedClassWithMainMethod_shouldNotBeCandidate() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("public-methods", true);
		UnusedMethodsCandidateVisitor visitor = new UnusedMethodsCandidateVisitor(options);
		String methodWithUusedLocalTypeDeclaration = "" +
				"	static class NestedClassWithMainMethod {\n"
				+ "		public static void main(String[] args) {\n"
				+ "\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithUusedLocalTypeDeclaration);
		defaultFixture.accept(visitor);

		List<NonPrivateUnusedMethodCandidate> nonPrivateCandidates = visitor.getNonPrivateCandidates();

		assertTrue(nonPrivateCandidates.isEmpty());
	}
}
