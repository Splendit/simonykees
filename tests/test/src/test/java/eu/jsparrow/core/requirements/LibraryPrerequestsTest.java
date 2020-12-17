package eu.jsparrow.core.requirements;

import java.util.Arrays;
import java.util.stream.Stream;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.core.rule.impl.StringUtilsRule;
import eu.jsparrow.core.util.RulesTestUtil;

/**
 * Tests IJavaProject if a specific version of a library is present. The first
 * parameter contains a list of {@link IClasspathEntriy} that represent the
 * maven dependency in a eclipse appropriate way. The second one is a boolean
 * that represents the enabled state of the tested rule, for the test case.
 * 
 * @author Martin Huter
 * @since 1.2
 *
 */
@SuppressWarnings("nls")
public class LibraryPrerequestsTest {

	IJavaProject testproject = null;

	@BeforeEach
	public void setUp() throws Exception {
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
	}

	@AfterEach
	public void tearDown() {
		testproject = null;
	}

	public static Stream<Arguments> data() throws Exception {
		return Stream.of(
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.0"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.0.1"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.1"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.2"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.2.1"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.3"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.3.1"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.3.2"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.4"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.5"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.6"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.7"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.8"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.8"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.8.1"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.10"), true ),
					Arguments.of(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.commons",
							"commons-lang3", "3.11"), true )
					
				);
	}

	@ParameterizedTest(name = "{index}: test with pom:[{0}]")
	@MethodSource("data")
	public void filterWithStringUtilsIsPresent(IClasspathEntry entry, boolean enabled) throws Exception {
		RulesTestUtil.addToClasspath(testproject, Arrays.asList(entry));

		StringUtilsRule sur = new StringUtilsRule();
		sur.calculateEnabledForProject(testproject);
		

		Assertions.assertEquals(enabled, sur.isEnabled());
	}
}
