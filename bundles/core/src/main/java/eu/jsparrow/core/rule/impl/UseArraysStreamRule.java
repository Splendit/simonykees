package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UseArraysStreamASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class UseArraysStreamRule extends RefactoringRuleImpl<UseArraysStreamASTVisitor> {
	
	public UseArraysStreamRule() {
		this.id = "UseArraysStream"; //$NON-NLS-1$
		this.visitorClass = UseArraysStreamASTVisitor.class;
		this.ruleDescription = new RuleDescription("Use Arrays Stream",
				"Replace Arrays.asList().stream() with Arrays.stream()", Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.PERFORMANCE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
