package eu.jsparrow.core.rule.impl.unused;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.unused.RemoveUnusedFieldsASTVisitor;
import eu.jsparrow.core.visitor.unused.UnusedFieldWrapper;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedFieldsRule extends RefactoringRuleImpl<RemoveUnusedFieldsASTVisitor> {

	private List<UnusedFieldWrapper> unusedFields;

	public RemoveUnusedFieldsRule(List<UnusedFieldWrapper> unusedFields) {
		this.visitorClass = RemoveUnusedFieldsASTVisitor.class;
		this.id = "RemoveUnusedFields"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Remove Unused Fields Rule",
				"Finds and remove fields that are never used actively. ", Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
		this.unusedFields = unusedFields;
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	@Override
	protected AbstractASTRewriteASTVisitor visitorFactory() {
		return new RemoveUnusedFieldsASTVisitor(unusedFields);
	}
}
