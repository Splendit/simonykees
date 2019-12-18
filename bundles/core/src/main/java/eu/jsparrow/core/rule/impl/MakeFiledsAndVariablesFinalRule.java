package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.make_final.MakeFiledsAndVariablesFinalASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @see MakeFiledsAndVariablesFinalASTVisitor
 * 
 * @since 3.12.0
 */
public class MakeFiledsAndVariablesFinalRule extends RefactoringRuleImpl<MakeFiledsAndVariablesFinalASTVisitor> {

	public static final String RULE_ID = "MakeFieldsAndVariablesFinal"; //$NON-NLS-1$

	public MakeFiledsAndVariablesFinalRule() {
		super();
		this.visitorClass = MakeFiledsAndVariablesFinalASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.MakeFiledsAndVariablesFinalRule_name,
				Messages.MakeFiledsAndVariablesFinalRule_description,
				Duration.ofMinutes(5), Tag.JAVA_1_1, Tag.READABILITY);
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}
