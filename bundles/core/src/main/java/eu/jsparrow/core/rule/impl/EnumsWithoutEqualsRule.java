package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.EnumsWithoutEqualsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see EnumsWithoutEqualsRuleASTVisitor
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.1.1
 */
public class EnumsWithoutEqualsRule extends RefactoringRuleImpl<EnumsWithoutEqualsASTVisitor> {

	public static final String ENUMS_WITHOUT_EQUALS_RULE_ID = "EnumsWithoutEquals"; //$NON-NLS-1$

	public EnumsWithoutEqualsRule() {
		super();
		this.visitorClass = EnumsWithoutEqualsASTVisitor.class;
		this.id = ENUMS_WITHOUT_EQUALS_RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.EnumsWithoutEqualsRule_name,
				Messages.EnumsWithoutEqualsRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_5, Tag.CODING_CONVENTIONS, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		// Enums exist since 1.5
		return JavaCore.VERSION_1_5;
	}
}
