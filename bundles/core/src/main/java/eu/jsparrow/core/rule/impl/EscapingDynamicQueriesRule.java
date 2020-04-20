package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.security.EscapingDynamicQueriesASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @since 3.17.0
 *
 */
public class EscapingDynamicQueriesRule extends RefactoringRuleImpl<EscapingDynamicQueriesASTVisitor> {

	public EscapingDynamicQueriesRule() {
		this.visitorClass = EscapingDynamicQueriesASTVisitor.class;
		this.id = "EscapingDynamicQueries"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Escaping Dynamic Queries",
				"This rule escapes quotation marks in dynamic queries.",
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.SECURITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
