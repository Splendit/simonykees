package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveModifiersInInterfacePropertiesASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class RemoveModifiersInInterfacePropertiesRule extends RefactoringRuleImpl<RemoveModifiersInInterfacePropertiesASTVisitor> {
	

	public RemoveModifiersInInterfacePropertiesRule() {
		this.visitorClass = RemoveModifiersInInterfacePropertiesASTVisitor.class;
		this.id = "RemoveModifiersFromInterfaceProperties"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Remove Modifiers from Interface Properties",
				"", Duration.ofMinutes(1),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY));
	}
	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
