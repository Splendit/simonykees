package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.OverrideAnnotationRuleASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * @see OverrideAnnotationRuleASTVisitor
 * 
 *      Required java version is {@value JavaVersion#JAVA_1_6} because with
 *      previous version it was not possible to annotate the methods inherited
 *      from the interfaces.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class OverrideAnnotationRule extends RefactoringRule<OverrideAnnotationRuleASTVisitor> {

	public OverrideAnnotationRule() {
		super();
		this.visitorClass = OverrideAnnotationRuleASTVisitor.class;
		this.id = "OverrideAnnotation"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.OverrideAnnotationRule_name,
				Messages.OverrideAnnotationRule_description, Duration.ofMinutes(5),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_6;
	}

}
