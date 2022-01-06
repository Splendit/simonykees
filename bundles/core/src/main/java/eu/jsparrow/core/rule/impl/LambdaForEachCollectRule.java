package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachCollectASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see LambdaForEachCollectASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaForEachCollectRule extends RefactoringRuleImpl<LambdaForEachCollectASTVisitor> {

	public static final String RULE_ID = "LambdaForEachCollect"; //$NON-NLS-1$

	public LambdaForEachCollectRule() {
		super();
		this.visitorClass = LambdaForEachCollectASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.LambdaForEachCollectRule_name,
				Messages.LambdaForEachCollectRule_description, Duration.ofMinutes(15),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
