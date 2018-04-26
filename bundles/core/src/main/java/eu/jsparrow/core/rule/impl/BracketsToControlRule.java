package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.impl.BracketsToControlASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see BracketsToControlASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class BracketsToControlRule extends RefactoringRuleImpl<BracketsToControlASTVisitor> {

	public BracketsToControlRule() {
		super();
		this.visitorClass = BracketsToControlASTVisitor.class;
		this.id = "BracketsToControl"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.BracketsToControlRule_name,
				Messages.BracketsToControlRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.FORMATTING, Tag.READABILITY));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
