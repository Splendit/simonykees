package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.impl.RemoveNewStringConstructorASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveNewStringConstructorASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveNewStringConstructorRule extends RefactoringRule<RemoveNewStringConstructorASTVisitor> {

	public RemoveNewStringConstructorRule() {
		super();
		this.visitorClass = RemoveNewStringConstructorASTVisitor.class;
		this.id = "RemoveNewStringConstructor"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.RemoveNewStringConstructorRule_name,
				Messages.RemoveNewStringConstructorRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION, Tag.PERFORMANCE));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
