package eu.jsparrow.rules.java10;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * Replaces the type of the local variable declarations with {@code var} keyword. 
 * @see LocalVariableTypeInferenceASTVisitor
 * 
 * @since 2.6.0
 *
 */
public class LocalVariableTypeInferenceRule extends RefactoringRuleImpl<LocalVariableTypeInferenceASTVisitor> {

	public LocalVariableTypeInferenceRule() {
		this.visitorClass = LocalVariableTypeInferenceASTVisitor.class;
		this.id = "LocalVariableTypeInference"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.LocalVariableTypeInferenceRule_name,
				Messages.LocalVariableTypeInferenceRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_10, Tag.FORMATTING, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_10;
	}

}
