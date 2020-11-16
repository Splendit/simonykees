package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.files.UseFilesBufferedReaderASTVisitor;
import eu.jsparrow.core.visitor.files.UseFilesWriteStringASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseFilesBufferedReaderASTVisitor
 * 
 * @since 3.24.0
 *
 */
public class UseFilesWriteStringRule extends RefactoringRuleImpl<UseFilesWriteStringASTVisitor> {

	public UseFilesWriteStringRule() {
		this.id = "UseFilesWriteString"; //$NON-NLS-1$
		this.visitorClass = UseFilesWriteStringASTVisitor.class;
		this.ruleDescription = new RuleDescription(Messages.UseFilesWriteStringRule_name,
				Messages.UseFilesWriteStringRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_11, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.PERFORMANCE, Tag.IO_OPERATIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_11;
	}
}
