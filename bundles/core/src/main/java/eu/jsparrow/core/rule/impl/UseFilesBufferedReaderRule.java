package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.files.UseFilesBufferedReaderASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class UseFilesBufferedReaderRule extends RefactoringRuleImpl<UseFilesBufferedReaderASTVisitor> {
	
	public UseFilesBufferedReaderRule() {
		this.id = "UseFilesBufferedReader"; //$NON-NLS-1$
		this.visitorClass = UseFilesBufferedReaderASTVisitor.class;
		this.ruleDescription = new RuleDescription("Use Files.newBufferedReader",
				"", Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}
}
