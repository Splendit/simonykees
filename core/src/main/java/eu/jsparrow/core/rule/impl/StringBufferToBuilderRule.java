package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.StringBufferToBuilderASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class StringBufferToBuilderRule extends RefactoringRule<StringBufferToBuilderASTVisitor> {

	public StringBufferToBuilderRule() {
		super();
		this.visitorClass = StringBufferToBuilderASTVisitor.class;
		this.name = Messages.StringBufferToBuilderRule_name;
		this.description = Messages.StringBufferToBuilderRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}
	
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.StringBufferToBuilderRule_name, Messages.StringBufferToBuilderRule_description,
				Duration.ofMinutes(2), TagUtil.getTagsForRule(this.getClass()));
	}

}
