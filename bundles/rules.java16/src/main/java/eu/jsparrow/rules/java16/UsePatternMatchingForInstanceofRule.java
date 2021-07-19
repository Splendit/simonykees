package eu.jsparrow.rules.java16;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class UsePatternMatchingForInstanceofRule extends RefactoringRuleImpl<UsePatternMatchingForInstanceofASTVisitor> {

	public UsePatternMatchingForInstanceofRule() {
		this.visitorClass = UsePatternMatchingForInstanceofASTVisitor.class;
		this.id = "UsePatternMatchingForInstanceof"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Use PatternMatching for Instanceof ", //$NON-NLS-1$
				"Replace instaceof expressions with Pattern matching for instance of", Duration.ofMinutes(2), //$NON-NLS-1$
				Arrays.asList(Tag.JAVA_16, Tag.FORMATTING, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_16;
	}

}
