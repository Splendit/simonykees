package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.PrimitiveBoxedForStringASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see PrimitiveBoxedForStringASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class PrimitiveBoxedForStringRule extends RefactoringRuleImpl<PrimitiveBoxedForStringASTVisitor> {

	public PrimitiveBoxedForStringRule() {
		super();
		this.visitorClass = PrimitiveBoxedForStringASTVisitor.class;
		this.id = "PrimitiveBoxedForString"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.PrimitiveBoxedForStringRule_name,
				Messages.PrimitiveBoxedForStringRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION, Tag.PERFORMANCE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	@Override
	public boolean isFree() {
		return true;
	}
}
