package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.impl.PrimitiveObjectUseEqualsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

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
		this.id = "PrimitiveObjectUseEquals"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.PrimitiveObjectUseEqualsRule_name,
				Messages.PrimitiveObjectUseEqualsRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS, Tag.STRING_MANIPULATION));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
