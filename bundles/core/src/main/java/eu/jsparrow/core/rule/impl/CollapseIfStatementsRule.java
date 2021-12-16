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
 * @see CollapseIfStatementsASTVisitor
 * 
 * @since 3.2.0
 *
 */
public class CollapseIfStatementsRule extends RefactoringRuleImpl<CollapseIfStatementsASTVisitor> {

	public static final String RULE_ID = "CollapseIfStatements"; //$NON-NLS-1$

	public CollapseIfStatementsRule() {
		this.visitorClass = CollapseIfStatementsASTVisitor.class;
		this.id = RULE_ID;
		String name = Messages.CollapseIfStatementsRule_name;
		String description = Messages.CollapseIfStatementsRule_description;
		this.ruleDescription = new RuleDescription(name, description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS, Tag.READABILITY, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
