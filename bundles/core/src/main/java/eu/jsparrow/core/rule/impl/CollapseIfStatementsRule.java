package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.CollapseIfStatementsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @since 3.2.0
 *
 */
public class CollapseIfStatementsRule extends RefactoringRuleImpl<CollapseIfStatementsASTVisitor>{
	
	public CollapseIfStatementsRule() {
		this.visitorClass = CollapseIfStatementsASTVisitor.class;
		this.id = "CollapseIfStatements"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Collapse If Statements",
				"Merges the collapsible if - statements", Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
