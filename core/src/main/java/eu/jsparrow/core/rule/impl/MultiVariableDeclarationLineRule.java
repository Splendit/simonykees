package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.MultiVariableDeclarationLineASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see MultiVariableDeclarationLineASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class MultiVariableDeclarationLineRule extends RefactoringRule<MultiVariableDeclarationLineASTVisitor> {

	public MultiVariableDeclarationLineRule() {
		super();
		this.visitorClass = MultiVariableDeclarationLineASTVisitor.class;
		this.id = "MultiVariableDeclarationLine"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.MultiVariableDeclarationLineRule_name,
				Messages.MultiVariableDeclarationLineRule_description, Duration.ofMinutes(2),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
