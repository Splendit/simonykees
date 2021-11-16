package eu.jsparrow.rules.java16.javarecords;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.java16.UsePatternMatchingForInstanceofASTVisitor;

/**
 * This rule replaces immutable class declarations by corresponding record
 * declarations.
 * 
 * @see UseJavaRecordsASTVisitor
 * 
 * 
 * @since 4.5.0
 *
 */
public class UseJavaRecordsRule
		extends RefactoringRuleImpl<UseJavaRecordsASTVisitor> {

	public UseJavaRecordsRule() {
		this.visitorClass = UseJavaRecordsASTVisitor.class;
		this.id = "UseJavaRecords"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UseJavaRecordsRule_name,
				Messages.UseJavaRecordsRule_description, Duration.ofMinutes(15),
				Arrays.asList(Tag.JAVA_16, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_16;
	}
}