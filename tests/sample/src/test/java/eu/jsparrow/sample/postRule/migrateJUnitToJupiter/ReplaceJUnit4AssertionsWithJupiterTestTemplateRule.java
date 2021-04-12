package eu.jsparrow.sample.postRule.migrateJUnitToJupiter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * see also
 * https://junit.org/junit5/docs/current/user-guide/#extensions-test-templates
 */
public class ReplaceJUnit4AssertionsWithJupiterTestTemplateRule {

	final List<String> values = Arrays.asList("value-1", "value-2", "value-3");

	@TestTemplate
	@ExtendWith(TestTemplateInvocationContextProviderExample.class)
	void testTemplate(String value) {
		assertTrue(values.contains(value));
	}

	public class TestTemplateInvocationContextProviderExample
			implements TestTemplateInvocationContextProvider {

		@Override
		public boolean supportsTestTemplate(ExtensionContext context) {
			return true;
		}

		@Override
		public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
				ExtensionContext context) {
			return Stream.of(createTestTemplateInvocationContext("value-1"),
					createTestTemplateInvocationContext("value-2"));
		}

		private TestTemplateInvocationContext createTestTemplateInvocationContext(String parameter) {
			return new TestTemplateInvocationContext() {
				@Override
				public String getDisplayName(int invocationIndex) {
					return "Index of invocation: " + 1 + ", parameter: " + parameter + ";";
				}

				@Override
				public List<Extension> getAdditionalExtensions() {
					return Collections.singletonList(new ParameterResolver() {
						@Override
						public boolean supportsParameter(ParameterContext parameterContext,
								ExtensionContext extensionContext) {
							return true;
						}

						@Override
						public Object resolveParameter(ParameterContext parameterContext,
								ExtensionContext extensionContext) {
							return parameter;
						}
					});
				}
			};
		}
	}
}