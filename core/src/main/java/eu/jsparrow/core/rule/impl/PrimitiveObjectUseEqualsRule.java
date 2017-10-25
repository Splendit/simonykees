package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.PrimitiveObjectUseEqualsASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * This rule replaces ==, != when called on primitive objects with equals.
 * 
 * @see PrimitiveObjectUseEqualsASTVisitor
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.1.1
 */
public class PrimitiveObjectUseEqualsRule extends RefactoringRule<PrimitiveObjectUseEqualsASTVisitor> {

	public PrimitiveObjectUseEqualsRule() {
		super();
		this.visitorClass = PrimitiveObjectUseEqualsASTVisitor.class;
		this.name = Messages.PrimitiveObjectUseEqualsRule_name;
		this.description = Messages.PrimitiveObjectUseEqualsRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.PrimitiveObjectUseEqualsRule_name, Messages.PrimitiveObjectUseEqualsRule_description,
				Duration.ofMinutes(2), TagUtil.getTagsForRule(this.getClass()));
	}
}
