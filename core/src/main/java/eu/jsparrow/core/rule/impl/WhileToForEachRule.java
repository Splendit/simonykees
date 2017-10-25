package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.loop.whiletoforeach.WhileToForEachASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see WhileToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class WhileToForEachRule extends RefactoringRule<WhileToForEachASTVisitor> {

	Logger logger = LoggerFactory.getLogger(WhileToForEachASTVisitor.class);

	public WhileToForEachRule() {
		super();
		this.visitorClass = WhileToForEachASTVisitor.class;
		this.name = Messages.WhileToForEachRule_name;
		this.description = Messages.WhileToForEachRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.WhileToForEachRule_name, Messages.WhileToForEachRule_description,
				Duration.ofMinutes(5), TagUtil.getTagsForRule(this.getClass()));
	}
}
