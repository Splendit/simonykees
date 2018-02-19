package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.BracketsToControlASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * @see BracketsToControlASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class BracketsToControlRule extends RefactoringRule<BracketsToControlASTVisitor> {

	public BracketsToControlRule() {
		super();
		this.visitorClass = BracketsToControlASTVisitor.class;
		this.id = "BracketsToControl"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.BracketsToControlRule_name,
				Messages.BracketsToControlRule_description, Duration.ofMinutes(2),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
