package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.impl.StringUtilsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see StringUtilsASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class StringUtilsRule extends RefactoringRuleImpl<StringUtilsASTVisitor> {

	public static final String RULE_ID = "StringUtils"; //$NON-NLS-1$
	private static final String STRING_UTILS_QUALIFIED_NAME = org.apache.commons.lang3.StringUtils.class.getName();

	@SuppressWarnings("nls")
	private static final List<String> supportedVersion = Collections
		.unmodifiableList(Arrays.asList(
				"3.0",
				"3.0.1",
				"3.1",
				"3.2",
				"3.2.1",
				"3.3",
				"3.3.1",
				"3.3.2",
				"3.4",
				"3.5",
				"3.6",
				"3.7",
				"3.8",
				"3.8.1",
				"3.9",
				"3.10",
				"3.11"));

	public StringUtilsRule() {
		this.visitorClass = StringUtilsASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.StringUtilsRule_name, Messages.StringUtilsRule_description,
				Duration.ofMinutes(10), Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		String fullyQuallifiedClassName = STRING_UTILS_QUALIFIED_NAME;
		Predicate<Version> versionComparator = currentVersion -> supportedVersion.stream()
			.map(Version::parseVersion)
			.anyMatch(suppportedVersion -> currentVersion.compareTo(suppportedVersion) == 0);
		return isInProjectLibraries(project, fullyQuallifiedClassName, versionComparator);
	}

	@Override
	public String requiredLibraries() {
		return STRING_UTILS_QUALIFIED_NAME;
	}
}
