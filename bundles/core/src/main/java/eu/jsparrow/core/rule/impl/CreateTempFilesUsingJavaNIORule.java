package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.security.CreateTempFilesUsingJavaNIOASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see CreateTempFilesUsingJavaNIOASTVisitor
 * 
 * @since 3.21.0
 *
 */
public class CreateTempFilesUsingJavaNIORule
		extends RefactoringRuleImpl<CreateTempFilesUsingJavaNIOASTVisitor> {
	
	public static final String RULE_ID = "CreateTempFilesUsingJavaNIO"; //$NON-NLS-1$

	public CreateTempFilesUsingJavaNIORule() {
		this.visitorClass = CreateTempFilesUsingJavaNIOASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.CreateTempFilesUsingJavaNioRule_name,
				Messages.CreateTempFilesUsingJavaNioRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_7, Tag.SECURITY, Tag.IO_OPERATIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_7;
	}
}
