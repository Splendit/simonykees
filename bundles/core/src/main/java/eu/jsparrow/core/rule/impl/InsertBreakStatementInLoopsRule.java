package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.InsertBreakStatementInLoopsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see InsertBreakStatementInLoopsASTVisitor
 * 
 * @since 3.9.0
 *
 */
public class InsertBreakStatementInLoopsRule extends RefactoringRuleImpl<InsertBreakStatementInLoopsASTVisitor> {

	public static final String RULE_ID = "InsertBreakStatementInLoops"; //$NON-NLS-1$
	public InsertBreakStatementInLoopsRule() {
		this.visitorClass = InsertBreakStatementInLoopsASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.InsertBreakStatementInLoopsRule_name,
				Messages.InsertBreakStatementInLoopsRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_5, Tag.LOOP, Tag.PERFORMANCE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}

}
