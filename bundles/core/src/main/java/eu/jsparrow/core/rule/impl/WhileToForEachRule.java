package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.loop.whiletoforeach.WhileToForEachASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see WhileToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class WhileToForEachRule extends RefactoringRuleImpl<WhileToForEachASTVisitor> {

	public static final String RULE_ID = "WhileToForEach"; //$NON-NLS-1$
	public WhileToForEachRule() {
		super();
		this.visitorClass = WhileToForEachASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.WhileToForEachRule_name,
				Messages.WhileToForEachRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_5, Tag.LOOP, Tag.OLD_LANGUAGE_CONSTRUCTS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}

}
