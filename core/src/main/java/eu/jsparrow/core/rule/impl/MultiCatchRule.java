package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.impl.trycatch.MultiCatchASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see MultiCatchASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class MultiCatchRule extends RefactoringRule<MultiCatchASTVisitor> {

	public MultiCatchRule() {
		super();
		this.visitorClass = MultiCatchASTVisitor.class;
		this.id = "MultiCatch"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.MultiCatchRule_name, Messages.MultiCatchRule_description,
				Duration.ofMinutes(5), Arrays.asList(Tag.JAVA_1_7, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_7;
	}

}
