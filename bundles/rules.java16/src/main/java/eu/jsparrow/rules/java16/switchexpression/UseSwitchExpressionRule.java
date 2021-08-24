package eu.jsparrow.rules.java16.switchexpression;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class UseSwitchExpressionRule extends RefactoringRuleImpl<UseSwitchExpressionASTVisitor> {

	public UseSwitchExpressionRule() {
		this.visitorClass = UseSwitchExpressionASTVisitor.class;
		this.id = "UseSwitchExpression"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Use Switch Expression",
				"Replace switch statement by switch expression from Java 14", Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_14, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}
	
	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_14;
	}

}
