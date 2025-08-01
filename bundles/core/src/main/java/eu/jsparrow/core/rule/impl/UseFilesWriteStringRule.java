package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.files.writestring.UseFilesWriteStringASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseFilesWriteStringASTVisitor
 * 
 * @since 3.24.0
 *
 */
public class UseFilesWriteStringRule extends RefactoringRuleImpl<UseFilesWriteStringASTVisitor> {

	public static final String RULE_ID = "UseFilesWriteString"; //$NON-NLS-1$
	public UseFilesWriteStringRule() {
		this.id = RULE_ID;
		this.visitorClass = UseFilesWriteStringASTVisitor.class;
		this.ruleDescription = new RuleDescription(Messages.UseFilesWriteStringRule_name,
				Messages.UseFilesWriteStringRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_11, Tag.PERFORMANCE, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.IO_OPERATIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_11;
	}
}
