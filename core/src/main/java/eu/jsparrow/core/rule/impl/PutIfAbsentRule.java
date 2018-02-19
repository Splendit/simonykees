package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.PutIfAbsentASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * This rule replaces map.put(..) with map.putIfAbsent(..) if certain criteria
 * are met.
 * 
 * @see PutIfAbsentASTVisitor
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.3.0
 */
public class PutIfAbsentRule extends RefactoringRule<PutIfAbsentASTVisitor> {

	public PutIfAbsentRule() {
		super();
		this.visitorClass = PutIfAbsentASTVisitor.class;
		this.id = "PutIfAbsent"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.PutIfAbsentRule_name, Messages.PutIfAbsentRule_description,
				Duration.ofMinutes(2), TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
