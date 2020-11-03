package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.files.UseFilesBufferedReaderASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseFilesBufferedReaderASTVisitor
 * 
 * @since 3.21.0
 *
 */
public class UseFilesBufferedReaderRule extends RefactoringRuleImpl<UseFilesBufferedReaderASTVisitor> {

	public UseFilesBufferedReaderRule() {
		this.id = "UseFilesBufferedReader"; //$NON-NLS-1$
		this.visitorClass = UseFilesBufferedReaderASTVisitor.class;
		this.ruleDescription = new RuleDescription(Messages.UseFilesBufferedReaderRule_name,
				Messages.UseFilesBufferedReaderRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_7, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.PERFORMANCE, Tag.IO_OPERATIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_7;
	}
}
