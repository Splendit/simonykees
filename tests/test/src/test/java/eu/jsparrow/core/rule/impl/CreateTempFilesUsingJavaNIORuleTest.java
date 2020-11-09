package eu.jsparrow.core.rule.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class CreateTempFilesUsingJavaNIORuleTest {

	private CreateTempFilesUsingJavaNIORule rule;

	@BeforeEach
	public void setUp() {
		rule = new CreateTempFilesUsingJavaNIORule();
	}

	@Test
	public void test_ruleId() {
		String ruleId = rule.getId();
		assertThat(ruleId, equalTo("CreateTempFilesUsingJavaNIO"));
	}

	@Test
	public void test_ruleDescription() {
		RuleDescription description = rule.getRuleDescription();
		assertThat(description.getTags(),
				contains(Tag.JAVA_1_7, Tag.SECURITY, Tag.IO_OPERATIONS));
		assertThat(description.getTags(), hasSize(3));
		assertThat(description.getName(), equalTo("Create Temp Files Using Java NIO"));
		assertThat(description.getDescription(), equalTo(
				"According to the documentation of 'File.createTempFile(String, String)', a suitable "
						+ "alternative for creating temporary files in security-sensitive applications is to "
						+ "use 'java.nio.file.Files.createTempFile(String, String, FileAttribute<?>...)'. "
						+ "The reason behind it is that files created by the latter have more restrictive access permissions."
						+ "\n\nThis rule replaces the temporary file creation using 'java.io.File' by the alternative methods "
						+ "defined in 'java.nio.file.Files'."));
	}
}
