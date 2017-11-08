package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.SerialVersionUidASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see SerialVersionUidRule
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class SerialVersionUidRule extends RefactoringRule<SerialVersionUidASTVisitor> {

	public SerialVersionUidRule() {
		super();
		this.visitorClass = SerialVersionUidASTVisitor.class;
		this.id = "SerialVersionUid"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.SerialVersionUidRule_name,
				Messages.SerialVersionUidRule_description, Duration.ofMinutes(5),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
