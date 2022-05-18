package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.FlatMapInsteadOfNestedLoopsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class FlatMapInsteadOfNestedLoopsRule extends RefactoringRuleImpl<FlatMapInsteadOfNestedLoopsASTVisitor> {

	public static final String RULE_ID = "FlatMapInsteadOfNestedLoops"; //$NON-NLS-1$

	public FlatMapInsteadOfNestedLoopsRule() {
		super();
		this.visitorClass = FlatMapInsteadOfNestedLoopsASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.FlatMapInsteadOfNestedLoopsRule_name,
				Messages.FlatMapInsteadOfNestedLoopsRule_description, Duration.ofMinutes(15),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
