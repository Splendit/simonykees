package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveModifiersInInterfacePropertiesASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveModifiersInInterfacePropertiesASTVisitor
 * 
 * @since 3.3.0
 *
 */
public class RemoveModifiersInInterfacePropertiesRule
		extends RefactoringRuleImpl<RemoveModifiersInInterfacePropertiesASTVisitor> {

	public RemoveModifiersInInterfacePropertiesRule() {
		this.visitorClass = RemoveModifiersInInterfacePropertiesASTVisitor.class;
		this.id = "RemoveModifiersInInterfaceProperties"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.RemoveModifiersInInterfacePropertiesRule_name,
				Messages.RemoveModifiersInInterfacePropertiesRule_description, Duration.ofMinutes(1),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
