package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.SerialVersionUidASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see SerialVersionUidRule
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class SerialVersionUidRule extends RefactoringRuleImpl<SerialVersionUidASTVisitor> {

	public SerialVersionUidRule() {
		super();
		this.visitorClass = SerialVersionUidASTVisitor.class;
		this.id = "SerialVersionUID"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.SerialVersionUidRule_name,
				Messages.SerialVersionUidRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
