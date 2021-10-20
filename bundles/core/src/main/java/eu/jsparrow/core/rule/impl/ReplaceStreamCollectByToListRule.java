package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import eu.jsparrow.core.visitor.stream.tolist.ReplaceStreamCollectByToListASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * This rule replaces invocations of the method
 * {@code Stream#collect(Collector)} by invocations of the Java 16 method
 * {@code Stream#toList()}.
 * 
 * @see ReplaceStreamCollectByToListASTVisitor
 * 
 * @since 4.4.0
 * 
 */
public class ReplaceStreamCollectByToListRule
		extends RefactoringRuleImpl<ReplaceStreamCollectByToListASTVisitor> {

	public ReplaceStreamCollectByToListRule() {
		this.visitorClass = ReplaceStreamCollectByToListASTVisitor.class;
		this.id = "ReplaceStreamCollectByToList"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ReplaceStreamCollectByToListRule_name,
				Messages.ReplaceStreamCollectByToListRule_description,
				Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_16, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return "16"; //$NON-NLS-1$
	}
}