package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.security.random.ReuseRandomObjectsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ReuseRandomObjectsASTVisitor
 * 
 * @since 3.20.0
 *
 */
public class ReuseRandomObjectsRule extends RefactoringRuleImpl<ReuseRandomObjectsASTVisitor> {

	public ReuseRandomObjectsRule() {
		this.visitorClass = ReuseRandomObjectsASTVisitor.class;
		this.id = "ReuseRandomObjects"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				Messages.ReuseRandomObjectsRule_name,
				Messages.ReuseRandomObjectsRule_description,
				Duration.ofMinutes(5),
				Tag.SECURITY, Tag.JAVA_1_1);
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}