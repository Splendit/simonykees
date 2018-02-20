package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ForToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ForToForEachRule extends RefactoringRule<ForToForEachASTVisitor> {

	public ForToForEachRule() {
		super();
		this.visitorClass = ForToForEachASTVisitor.class;
		this.id = "ForToForEach"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ForToForEachRule_name,
				Messages.ForToForEachRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_5, Tag.LOOP, Tag.OLD_LANGUAGE_CONSTRUCTS));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}

}
