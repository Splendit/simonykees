package eu.jsparrow.core.rule.impl.unused;

import static eu.jsparrow.core.rule.impl.unused.Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.unused.RemoveUnusedLocalVariabesASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * @see RemoveUnusedLocalVariabesASTVisitor
 * 
 * @since 4.9.0
 *
 */
public class RemoveUnusedLocalVariablesRule extends RefactoringRuleImpl<RemoveUnusedLocalVariabesASTVisitor> {

	public RemoveUnusedLocalVariablesRule() {

		this.visitorClass = RemoveUnusedLocalVariabesASTVisitor.class;
		this.id = "RemoveUnusedLocalVariables"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.RemoveUnusedLocalVariablesRule_name,
				Messages.RemoveUnusedLocalVariablesRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	protected AbstractASTRewriteASTVisitor visitorFactory() throws InstantiationException, IllegalAccessException {
		Map<String, Boolean> options = new HashMap<>();
		options.put(REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
		AbstractASTRewriteASTVisitor visitor = new RemoveUnusedLocalVariabesASTVisitor(options);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}
}
