package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.MapGetOrDefaultASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see MapGetOrDefaultASTVisitor
 * 
 * @since 3.4.0
 *
 */
public class MapGetOrDefaultRule extends RefactoringRuleImpl<MapGetOrDefaultASTVisitor> {

	public static final String RULE_ID = "MapGetOrDefault"; //$NON-NLS-1$

	public MapGetOrDefaultRule() {
		this.visitorClass = MapGetOrDefaultASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.MapGetOrDefaultRule_name,
				Messages.MapGetOrDefaultRule_description,
				Duration.ofMinutes(2), Arrays.asList(Tag.JAVA_1_8, Tag.OLD_LANGUAGE_CONSTRUCTS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
