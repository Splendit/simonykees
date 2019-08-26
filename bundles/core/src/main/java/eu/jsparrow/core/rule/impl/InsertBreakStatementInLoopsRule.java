package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.InsertBreakStatementInLoopsASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class InsertBreakStatementInLoopsRule extends RefactoringRuleImpl<InsertBreakStatementInLoopsASTVisitor> {
	
	public InsertBreakStatementInLoopsRule() {
		this.visitorClass = InsertBreakStatementInLoopsASTVisitor.class;
		this.id = "InsertBreakStatementInLoops"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Break Loops Computing Boolean Values",
				"Inserts a break statements in the loops whose sole purpose is to compute a boolean value without side effects.", Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_5, Tag.LOOP, Tag.PERFORMANCE));
	}


	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}

}
