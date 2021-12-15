package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.PutIfAbsentASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * This rule replaces map.put(..) with map.putIfAbsent(..) if certain criteria
 * are met.
 * 
 * @see PutIfAbsentASTVisitor
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.3.0
 */
public class PutIfAbsentRule extends RefactoringRuleImpl<PutIfAbsentASTVisitor> {

	public static final String RULE_ID = "PutIfAbsent"; //$NON-NLS-1$
	public PutIfAbsentRule() {
		super();
		this.visitorClass = PutIfAbsentASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.PutIfAbsentRule_name, Messages.PutIfAbsentRule_description,
				Duration.ofMinutes(2), Arrays.asList(Tag.JAVA_1_8, Tag.OLD_LANGUAGE_CONSTRUCTS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
