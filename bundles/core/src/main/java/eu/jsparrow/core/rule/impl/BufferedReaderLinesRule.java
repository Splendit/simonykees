package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.loop.bufferedreader.BufferedReaderLinesASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see BufferedReaderLinesASTVisitor
 * 
 * @since 3.3.0
 *
 */
public class BufferedReaderLinesRule extends RefactoringRuleImpl<BufferedReaderLinesASTVisitor> {

	public  static final String RULE_ID = "BufferedReaderLines"; //$NON-NLS-1$

	public BufferedReaderLinesRule() {
		this.visitorClass = BufferedReaderLinesASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.BufferedReaderLinesRule_name,
				Messages.BufferedReaderLinesRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.IO_OPERATIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}
}
