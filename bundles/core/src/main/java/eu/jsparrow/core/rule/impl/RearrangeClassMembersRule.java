package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RearrangeClassMembersASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RearrangeClassMembersASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.1
 */
public class RearrangeClassMembersRule extends RefactoringRuleImpl<RearrangeClassMembersASTVisitor> {

	public RearrangeClassMembersRule() {
		super();
		this.visitorClass = RearrangeClassMembersASTVisitor.class;
		this.id = "RearrangeClassMembers"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.RearrangeClassMembersRule_name,
				Messages.RearrangeClassMembersRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
