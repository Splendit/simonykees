package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UseStringJoinASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class UseStringJoinRule extends RefactoringRuleImpl<UseStringJoinASTVisitor>{
	
	public UseStringJoinRule() {
		this.visitorClass = UseStringJoinASTVisitor.class;
		this.id = "UseStringJoin"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Use String Join",
				"Use String::join instead of Stream::collect", Duration.ofMinutes(5),
				Tag.STRING_MANIPULATION, Tag.PERFORMANCE);
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
