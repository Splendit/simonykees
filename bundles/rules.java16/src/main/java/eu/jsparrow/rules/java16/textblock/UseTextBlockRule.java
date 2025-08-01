package eu.jsparrow.rules.java16.textblock;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * This rule replaces concatenation expressions made up of String literals by
 * Text Block Sting literals which have been introduced in Java 15. Thus
 * readability of String expressions is improved.
 * 
 * 
 * @see UseTextBlockASTVisitor
 * 
 * @since 4.3.0
 * 
 */
public class UseTextBlockRule
		extends RefactoringRuleImpl<UseTextBlockASTVisitor> {

	public static final String RULE_ID = "UseTextBlock"; //$NON-NLS-1$

	public UseTextBlockRule() {
		this.visitorClass = UseTextBlockASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.UseTextBlockRule_name,
				Messages.UseTextBlockRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_15, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_15;
	}

}