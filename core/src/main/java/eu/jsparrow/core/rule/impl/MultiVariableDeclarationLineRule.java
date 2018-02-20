package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.impl.MultiVariableDeclarationLineASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

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
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
