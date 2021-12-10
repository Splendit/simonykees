package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UseCollectionsSingletonListASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseCollectionsSingletonListASTVisitor
 * 
 * @since 3.8.0
 *
 */
public class UseCollectionsSingletonListRule extends RefactoringRuleImpl<UseCollectionsSingletonListASTVisitor> {

	public static final String RULE_ID = "UseCollectionsSingletonList"; //$NON-NLS-1$
	public UseCollectionsSingletonListRule() {
		this.id = RULE_ID;
		this.visitorClass = UseCollectionsSingletonListASTVisitor.class;
		this.ruleDescription = new RuleDescription(Messages.UseCollectionsSingletonListRule_name,
				Messages.UseCollectionsSingletonListRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_3, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.PERFORMANCE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_3;
	}

}
