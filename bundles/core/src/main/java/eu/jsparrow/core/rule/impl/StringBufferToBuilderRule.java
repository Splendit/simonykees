package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.StringBufferToBuilderASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class StringBufferToBuilderRule extends RefactoringRuleImpl<StringBufferToBuilderASTVisitor> {

	public static final String RULE_ID = "StringBufferToBuilder"; //$NON-NLS-1$

	public StringBufferToBuilderRule() {
		super();
		this.visitorClass = StringBufferToBuilderASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.StringBufferToBuilderRule_name,
				Messages.StringBufferToBuilderRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_5, Tag.PERFORMANCE, Tag.STRING_MANIPULATION));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}

}
