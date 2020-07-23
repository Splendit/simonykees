package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.ReuseRandomObjectsASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class ReuseRandomObjectsRule extends RefactoringRuleImpl<ReuseRandomObjectsASTVisitor> {

	public ReuseRandomObjectsRule() {
		this.visitorClass = ReuseRandomObjectsASTVisitor.class;
		this.id = "ReuseRandomObjects"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Reuse Random Objects",
				"Extracts local random objects to private fields", Duration.ofMinutes(5),
				Tag.SECURITY, Tag.JAVA_1_1);
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}