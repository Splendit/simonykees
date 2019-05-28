package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.ReorderModifiersASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class ReorderModifiersRule extends RefactoringRuleImpl<ReorderModifiersASTVisitor> {

	public ReorderModifiersRule() {
		this.visitorClass = ReorderModifiersASTVisitor.class;
		this.id = "ReorderModifiers";
		this.ruleDescription = new RuleDescription("Reorder Modifiers",
				"Reorders Modifiers of Fields and Methods according to java conventions", Duration.ofMinutes(2),
				Tag.READABILITY);
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
