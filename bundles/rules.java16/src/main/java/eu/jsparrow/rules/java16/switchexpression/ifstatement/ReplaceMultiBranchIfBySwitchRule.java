package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ReplaceMultiBranchIfBySwitchASTVisitor
 * 
 * @since 4.13.0
 *
 */
public class ReplaceMultiBranchIfBySwitchRule extends RefactoringRuleImpl<ReplaceMultiBranchIfBySwitchASTVisitor> {

	public ReplaceMultiBranchIfBySwitchRule() {
		this.visitorClass = ReplaceMultiBranchIfBySwitchASTVisitor.class;
		this.id = "ReplaceMultiBranchIfBySwitch"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ReplaceMultiBranchIfBySwitchRule_name,
				Messages.ReplaceMultiBranchIfBySwitchRule_description,
				Duration.ofMinutes(15),
				Arrays.asList(Tag.JAVA_14, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_14;
	}

}
