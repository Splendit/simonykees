package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.IndexOfToContainsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 *
 */
public class IndexOfToContainsRule extends RefactoringRuleImpl<IndexOfToContainsASTVisitor> {

	public static final String RULE_ID = "IndexOfToContains"; //$NON-NLS-1$
	public IndexOfToContainsRule() {
		super();
		this.visitorClass = IndexOfToContainsASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.IndexOfToContainsRule_name,
				Messages.IndexOfToContainsRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_5, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5; // for lists 1.2, but for strings 1.5
	}

}
