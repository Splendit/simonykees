package eu.jsparrow.rules.java16;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * This rule replaces <b>instanceof expressions</b> by <b>Pattern Matching for
 * Instanceof expressions</b> that are introduced in Java 16.
 * 
 * @see UsePatternMatchingForInstanceofASTVisitor
 * 
 * @since 4.2.0
 * 
 */
public class UsePatternMatchingForInstanceofRule
		extends RefactoringRuleImpl<UsePatternMatchingForInstanceofASTVisitor> {

	public UsePatternMatchingForInstanceofRule() {
		this.visitorClass = UsePatternMatchingForInstanceofASTVisitor.class;
		this.id = "UsePatternMatchingForInstanceof"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UsePatternMatchingForInstanceofRule_name,
				Messages.UsePatternMatchingForInstanceofRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_16, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_16;
	}

}