package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveRedundantTypeCastASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveRedundantTypeCastASTVisitor
 * 
 * @since 3.15.0
 *
 */
public class RemoveRedundantTypeCastRule extends RefactoringRuleImpl<RemoveRedundantTypeCastASTVisitor> {

	public static final String RULE_ID = "RemoveRedundantTypeCast"; //$NON-NLS-1$

	public RemoveRedundantTypeCastRule() {
		this.visitorClass = RemoveRedundantTypeCastASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.RemoveRedundantTypeCastRule_name,
				Messages.RemoveRedundantTypeCastRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
