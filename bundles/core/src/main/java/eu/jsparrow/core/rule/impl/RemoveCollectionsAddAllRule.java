package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveCollectionsAddAllASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveCollectionsAddAllASTVisitor
 * @since 3.14.0
 *
 */
public class RemoveCollectionsAddAllRule extends RefactoringRuleImpl<RemoveCollectionsAddAllASTVisitor> {

	public RemoveCollectionsAddAllRule() {
		this.visitorClass = RemoveCollectionsAddAllASTVisitor.class;
		this.id = "RemoveCollectionsAddAll"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.RemoveCollectionsAddAllRule_name,
				Messages.RemoveCollectionsAddAllRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_2, Tag.READABILITY));

	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_2;
	}

}
