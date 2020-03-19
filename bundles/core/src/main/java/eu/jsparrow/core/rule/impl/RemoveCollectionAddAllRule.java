package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveCollectionAddAllASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveCollectionAddAllASTVisitor
 * @since 3.15.0
 *
 */
public class RemoveCollectionAddAllRule extends RefactoringRuleImpl<RemoveCollectionAddAllASTVisitor> {

	public RemoveCollectionAddAllRule() {
		this.visitorClass = RemoveCollectionAddAllASTVisitor.class;
		this.id = "RemoveCollectionAddAll"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.RemoveCollectionAddAllRule_name,
				Messages.RemoveCollectionAddAllRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_2, Tag.READABILITY));

	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_2;
	}

}
