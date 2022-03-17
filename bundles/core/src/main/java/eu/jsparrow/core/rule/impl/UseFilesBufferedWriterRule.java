package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.files.UseFilesBufferedWriterASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseFilesBufferedWriterASTVisitor
 * 
 * @since 3.22.0
 *
 */
public class UseFilesBufferedWriterRule extends RefactoringRuleImpl<UseFilesBufferedWriterASTVisitor> {

	public static final String RULE_ID = "UseFilesBufferedWriter"; //$NON-NLS-1$

	public UseFilesBufferedWriterRule() {
		this.id = RULE_ID;
		this.visitorClass = UseFilesBufferedWriterASTVisitor.class;
		this.ruleDescription = new RuleDescription(Messages.UseFilesBufferedWriterRule_name,
				Messages.UseFilesBufferedWriterRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_7, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.PERFORMANCE, Tag.IO_OPERATIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_7;
	}
}
