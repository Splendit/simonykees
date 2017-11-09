package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.BracketsToControlASTVisitor;
import eu.jsparrow.core.visitor.impl.PutIfAbsentASTVisitor;
import eu.jsparrow.i18n.Messages;

public class PutIfAbsentRule extends RefactoringRule<PutIfAbsentASTVisitor> {

	public PutIfAbsentRule() {
		super();
		this.visitorClass = PutIfAbsentASTVisitor.class;
		this.id = "BracketsToControl"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.BracketsToControlRule_name,
				Messages.BracketsToControlRule_description, Duration.ofMinutes(2),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
