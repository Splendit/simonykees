package eu.jsparrow.core.requirements;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.rule.impl.logger.SupportedLogger;

/**
 * Tests for the LoggingTypes library check.
 * 
 * Tests which logging framework is present within the Project and sets the
 * {@link SupportedLogger} within the rule, which will be used to configure the
 * behavior of the ASTVisitor.
 * 
 * @author Martin Huter
 * @since 1.2
 *
 */

@SuppressWarnings("nls")
public class LoggingTypesTest {

	IJavaProject testproject = null;
	List<IClasspathEntry> entries = null;

	@BeforeEach
	public void setUp() throws Exception {
		testproject = RulesTestUtil.createJavaProject("javaVersionTestProject", "bin");
		entries = new ArrayList<>();
	}

	@AfterEach
	public void tearDown() {
		testproject = null;
	}

	@Test
	public void allPresent() throws Exception {
		List<IClasspathEntry> entries = new ArrayList<>();
		entries.add(RulesTestUtil.generateMavenEntryFromDepedencyString("org.slf4j", "slf4j-api", "1.7.25"));
		entries.add(RulesTestUtil.generateMavenEntryFromDepedencyString("ch.qos.logback", "logback-classic", "1.2.3"));
		entries
			.add(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.logging.log4j", "log4j-api", "2.7"));
		RulesTestUtil.addToClasspath(testproject, entries);

		StandardLoggerRule slr = new StandardLoggerRule();
		slr.calculateEnabledForProject(testproject);

		assertEquals(SupportedLogger.SLF4J, slr.getAvailableLoggerType());
	}

	@Test
	public void slf4jLog4jPresent() throws Exception {
		List<IClasspathEntry> entries = new ArrayList<>();
		entries.add(RulesTestUtil.generateMavenEntryFromDepedencyString("org.slf4j", "slf4j-api", "1.7.25"));
		entries
			.add(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.logging.log4j", "log4j-api", "2.7"));
		RulesTestUtil.addToClasspath(testproject, entries);

		StandardLoggerRule slr = new StandardLoggerRule();
		slr.calculateEnabledForProject(testproject);

		assertEquals(SupportedLogger.SLF4J, slr.getAvailableLoggerType());
	}

	@Test
	public void slf4jLogbackPresent() throws Exception {
		List<IClasspathEntry> entries = new ArrayList<>();
		entries.add(RulesTestUtil.generateMavenEntryFromDepedencyString("org.slf4j", "slf4j-api", "1.7.25"));
		entries.add(RulesTestUtil.generateMavenEntryFromDepedencyString("ch.qos.logback", "logback-classic", "1.2.3"));
		RulesTestUtil.addToClasspath(testproject, entries);

		StandardLoggerRule slr = new StandardLoggerRule();
		slr.calculateEnabledForProject(testproject);

		assertEquals(SupportedLogger.SLF4J, slr.getAvailableLoggerType());
	}

	@Test
	public void slf4jPresent() throws Exception {
		List<IClasspathEntry> entries = new ArrayList<>();
		entries.add(RulesTestUtil.generateMavenEntryFromDepedencyString("org.slf4j", "slf4j-api", "1.7.25"));
		RulesTestUtil.addToClasspath(testproject, entries);

		StandardLoggerRule slr = new StandardLoggerRule();
		slr.calculateEnabledForProject(testproject);

		assertEquals(SupportedLogger.SLF4J, slr.getAvailableLoggerType());
	}

	@Test
	public void log4jApiPresent() throws Exception {
		entries
			.add(RulesTestUtil.generateMavenEntryFromDepedencyString("org.apache.logging.log4j", "log4j-api", "2.7"));
		RulesTestUtil.addToClasspath(testproject, entries);

		StandardLoggerRule slr = new StandardLoggerRule();
		slr.calculateEnabledForProject(testproject);

		assertEquals(SupportedLogger.LOG4J, slr.getAvailableLoggerType());
	}

	@Test
	public void noLoggerPresent() throws Exception {
		RulesTestUtil.addToClasspath(testproject, entries);

		StandardLoggerRule slr = new StandardLoggerRule();
		slr.calculateEnabledForProject(testproject);

		assertEquals(null, slr.getAvailableLoggerType());
	}

}
