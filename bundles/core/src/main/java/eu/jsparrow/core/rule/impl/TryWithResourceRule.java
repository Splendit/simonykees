package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.trycatch.TryWithResourceASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see TryWithResourceASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class TryWithResourceRule extends RefactoringRuleImpl<TryWithResourceASTVisitor> {

	public static final String RULE_ID = "TryWithResource"; //$NON-NLS-1$

	public TryWithResourceRule() {
		super();
		this.visitorClass = TryWithResourceASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.TryWithResourceRule_name,
				Messages.TryWithResourceRule_description, Duration.ofMinutes(15),
				Arrays.asList(Tag.JAVA_1_7, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_7;
	}
}
