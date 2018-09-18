package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveToStringOnStringASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveToStringOnStringASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveToStringOnStringRule extends RefactoringRuleImpl<RemoveToStringOnStringASTVisitor> {

	public RemoveToStringOnStringRule() {
		super();
		this.visitorClass = RemoveToStringOnStringASTVisitor.class;
		this.id = "RemoveToStringOnString"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.RemoveToStringOnStringRule_name,
				Messages.RemoveToStringOnStringRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION, Tag.PERFORMANCE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
