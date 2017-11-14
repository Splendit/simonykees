package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.PrimitiveBoxedForStringASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see PrimitiveBoxedForStringASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class PrimitiveBoxedForStringRule extends RefactoringRule<PrimitiveBoxedForStringASTVisitor> {

	public PrimitiveBoxedForStringRule() {
		super();
		this.visitorClass = PrimitiveBoxedForStringASTVisitor.class;
		this.id = "PrimitiveBoxedForString"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.PrimitiveBoxedForStringRule_name,
				Messages.PrimitiveBoxedForStringRule_description, Duration.ofMinutes(5),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
