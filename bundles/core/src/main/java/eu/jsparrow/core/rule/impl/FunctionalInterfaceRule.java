package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.functionalinterface.FunctionalInterfaceASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see FunctionalInterfaceASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class FunctionalInterfaceRule extends RefactoringRuleImpl<FunctionalInterfaceASTVisitor> {

	public FunctionalInterfaceRule() {
		super();
		this.visitorClass = FunctionalInterfaceASTVisitor.class;
		this.id = "FunctionalInterface"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.FunctionalInterfaceRule_name,
				Messages.FunctionalInterfaceRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.LAMBDA));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
