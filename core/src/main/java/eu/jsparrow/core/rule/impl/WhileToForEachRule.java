package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.loop.whiletoforeach.WhileToForEachASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * @see WhileToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class WhileToForEachRule extends RefactoringRule<WhileToForEachASTVisitor> {

	public WhileToForEachRule() {
		super();
		this.visitorClass = WhileToForEachASTVisitor.class;
		this.id = "WhileToForEach"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.WhileToForEachRule_name,
				Messages.WhileToForEachRule_description, Duration.ofMinutes(5),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}

}
