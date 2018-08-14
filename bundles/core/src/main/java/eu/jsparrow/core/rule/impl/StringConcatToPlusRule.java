package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.StringConcatToPlusASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see StringConcatToPlusASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class StringConcatToPlusRule extends RefactoringRuleImpl<StringConcatToPlusASTVisitor> {

	public StringConcatToPlusRule() {
		super();
		this.visitorClass = StringConcatToPlusASTVisitor.class;
		this.id = "StringConcatToPlus"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.StringConcatToPlusRule_name,
				Messages.StringConcatToPlusRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
