package eu.jsparrow.core.rule.impl.unused;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.unused.method.RemoveUnusedMethodsASTVisitor;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodWrapper;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedMethodsRule extends RefactoringRuleImpl<RemoveUnusedMethodsASTVisitor> {
	
	private List<UnusedMethodWrapper> unusedMethods;

	public RemoveUnusedMethodsRule(List<UnusedMethodWrapper> unusedMethods) {
		this.visitorClass = RemoveUnusedMethodsASTVisitor.class;
		this.id = "RemoveUnusedFields"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Remove Unused Methods", //$NON-NLS-1$
				"Finds and removes unused methods", Duration.ofMinutes(2), //$NON-NLS-1$
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
		this.unusedMethods = unusedMethods;
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
	
	@Override
	protected AbstractASTRewriteASTVisitor visitorFactory() {
		RemoveUnusedMethodsASTVisitor visitor = new RemoveUnusedMethodsASTVisitor(unusedMethods);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}
}
