package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ForToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ForToForEachRule extends RefactoringRuleImpl<ForToForEachASTVisitor> {

	public static final String FOR_TO_FOR_EACH_RULE_ID = "ForToForEach"; //$NON-NLS-1$

	public ForToForEachRule() {
		super();
		this.visitorClass = ForToForEachASTVisitor.class;
		this.id = FOR_TO_FOR_EACH_RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.ForToForEachRule_name,
				Messages.ForToForEachRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_5, Tag.LOOP, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}
}
