package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.loop.whiletoforeach.BufferedReaderLinesASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class BufferedReaderLinesRule extends RefactoringRuleImpl<BufferedReaderLinesASTVisitor> {

	public BufferedReaderLinesRule() {
		this.visitorClass = BufferedReaderLinesASTVisitor.class;
		this.id = "BufferedReaderLines"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Use BufferedReader::lines",
				"Replace while loops iterating over lines of a file by BufferedReader::lines stream",
				Duration.ofMinutes(5), Arrays.asList(Tag.JAVA_1_8, Tag.LOOP, Tag.OLD_LANGUAGE_CONSTRUCTS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
