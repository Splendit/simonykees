package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UseStringBuilderAppendASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class UseStringBuilderAppendRule extends RefactoringRuleImpl<UseStringBuilderAppendASTVisitor> {

	public UseStringBuilderAppendRule() {
		this.visitorClass = UseStringBuilderAppendASTVisitor.class;
		this.id = "UseStringBuilderAppend";
		this.ruleDescription = new RuleDescription("Use StringBuilder Append",
				"Replaces the string concatenation with StringBuilder::append", Duration.ofMinutes(2), Tag.PERFORMANCE);

	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}

}
