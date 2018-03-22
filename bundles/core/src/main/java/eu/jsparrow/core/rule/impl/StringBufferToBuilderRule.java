package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.impl.StringBufferToBuilderASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class StringBufferToBuilderRule extends RefactoringRule<StringBufferToBuilderASTVisitor> {

	public StringBufferToBuilderRule() {
		super();
		this.visitorClass = StringBufferToBuilderASTVisitor.class;
		this.id = "StringBufferToBuilder"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.StringBufferToBuilderRule_name,
				Messages.StringBufferToBuilderRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_5, Tag.PERFORMANCE, Tag.STRING_MANIPULATION));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}

}
