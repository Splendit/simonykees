package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

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

	public TryWithResourceRule() {
		super();
		this.visitorClass = TryWithResourceASTVisitor.class;
		this.id = "TryWithResource"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.TryWithResourceRule_name,
				Messages.TryWithResourceRule_description, Duration.ofMinutes(15),
				Arrays.asList(Tag.JAVA_1_7, Tag.OLD_LANGUAGE_CONSTRUCTS));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_7;
	}

}
