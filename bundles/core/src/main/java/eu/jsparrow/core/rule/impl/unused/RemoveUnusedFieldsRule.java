package eu.jsparrow.core.rule.impl.unused;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.unused.RemoveUnusedFieldsASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedFieldsRule extends RefactoringRuleImpl<RemoveUnusedFieldsASTVisitor>{

	public RemoveUnusedFieldsRule() {
		this.visitorClass = RemoveUnusedFieldsASTVisitor.class;
		this.id = "RemoveUnusedFields"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Remove Unused Fields Rule",
				"Finds and remove fields that are never used actively. ", Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
	}
	
	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	@Override
	protected AbstractASTRewriteASTVisitor visitorFactory() {
		
		RemoveUnusedFieldsASTVisitor visitor = new RemoveUnusedFieldsASTVisitor();
		
		return visitor;
	}
}
