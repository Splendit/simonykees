package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.ReplaceWrongClassForLoggerASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ReplaceWrongClassForLoggerASTVisitor
 * 
 * @since 4.13.0
 *
 */
public class ReplaceWrongClassForLoggerRule extends RefactoringRuleImpl<ReplaceWrongClassForLoggerASTVisitor> {

	public static final String RULE_ID = "ReplaceWrongClassForLogger"; //$NON-NLS-1$

	public ReplaceWrongClassForLoggerRule() {
		this.visitorClass = ReplaceWrongClassForLoggerASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.ReplaceWrongClassForLoggerRule_name,
				Messages.ReplaceWrongClassForLoggerRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODE_SMELL, Tag.LOGGING));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
