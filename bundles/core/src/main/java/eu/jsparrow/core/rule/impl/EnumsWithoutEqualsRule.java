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

	public EnumsWithoutEqualsRule() {
		super();
		this.visitorClass = EnumsWithoutEqualsASTVisitor.class;
		this.id = "EnumsWithoutEquals"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.EnumsWithoutEqualsRule_name,
				Messages.EnumsWithoutEqualsRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_5, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		// Enums exist since 1.5
		return JavaCore.VERSION_1_5;
	}

	@Override
	public boolean isFree() {
		return true;
	}
}
