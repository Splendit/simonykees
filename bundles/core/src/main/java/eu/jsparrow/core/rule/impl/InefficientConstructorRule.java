package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.impl.InefficientConstructorASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see InefficientConstructorASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class InefficientConstructorRule extends RefactoringRuleImpl<InefficientConstructorASTVisitor> {

	public InefficientConstructorRule() {
		super();
		this.visitorClass = InefficientConstructorASTVisitor.class;
		this.id = "InefficientConstructor"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.InefficientConstructorRule_name,
				Messages.InefficientConstructorRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_5, Tag.PERFORMANCE, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}

}
