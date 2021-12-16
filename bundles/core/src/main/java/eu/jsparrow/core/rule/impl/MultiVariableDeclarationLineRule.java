package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.MultiVariableDeclarationLineASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see MultiVariableDeclarationLineASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class MultiVariableDeclarationLineRule extends RefactoringRuleImpl<MultiVariableDeclarationLineASTVisitor> {

	public static final String RULE_ID = "MultiVariableDeclarationLine"; //$NON-NLS-1$

	public MultiVariableDeclarationLineRule() {
		super();
		this.visitorClass = MultiVariableDeclarationLineASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.MultiVariableDeclarationLineRule_name,
				Messages.MultiVariableDeclarationLineRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}
