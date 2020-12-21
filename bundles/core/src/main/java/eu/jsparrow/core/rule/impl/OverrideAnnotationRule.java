package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.OverrideAnnotationRuleASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see OverrideAnnotationRuleASTVisitor
 * 
 *      Required java version is {@value JavaCore#VERSION_1_6} because with
 *      previous version it was not possible to annotate the methods inherited
 *      from the interfaces.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class OverrideAnnotationRule extends RefactoringRuleImpl<OverrideAnnotationRuleASTVisitor> {

	public static final String OVERRIDE_ANNOTATION_RULE_ID = "OverrideAnnotation"; //$NON-NLS-1$

	public OverrideAnnotationRule() {
		super();
		this.visitorClass = OverrideAnnotationRuleASTVisitor.class;
		this.id = OVERRIDE_ANNOTATION_RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.OverrideAnnotationRule_name,
				Messages.OverrideAnnotationRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_6, Tag.READABILITY, Tag.CODING_CONVENTIONS, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_6;
	}
}
