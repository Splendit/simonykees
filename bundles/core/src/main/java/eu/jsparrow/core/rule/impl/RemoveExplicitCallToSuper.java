package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveExplicitCallToSuperASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveExplicitCallToSuperASTVisitor
 * 
 * @since 2.7.0
 *
 */
public class RemoveExplicitCallToSuper extends RefactoringRuleImpl<RemoveExplicitCallToSuperASTVisitor> {

	public RemoveExplicitCallToSuper() {
		super();
		this.visitorClass = RemoveExplicitCallToSuperASTVisitor.class;
		this.id = "RemoveExplicitCallToSuper";
		this.ruleDescription = new RuleDescription("Remove Explicit Call To super()",
				"Removes unnecessary explicit call to the default constructor of the super class.",
				Duration.ofMinutes(1), Tag.READABILITY);
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
