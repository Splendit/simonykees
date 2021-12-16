package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UseIsEmptyOnCollectionsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseIsEmptyOnCollectionsASTVisitor
 * 
 * @author Martin Huter
 * @since 2.1.0
 */
public class UseIsEmptyOnCollectionsRule extends RefactoringRuleImpl<UseIsEmptyOnCollectionsASTVisitor> {

	public static final String RULE_ID = "UseIsEmptyOnCollections"; //$NON-NLS-1$

	public UseIsEmptyOnCollectionsRule() {
		super();
		this.visitorClass = UseIsEmptyOnCollectionsASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.UseIsEmptyOnCollectionsRule_name,
				Messages.UseIsEmptyOnCollectionsRule_description, Duration.ofMinutes(2), Arrays.asList(Tag.JAVA_1_6,
						Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.CODING_CONVENTIONS, Tag.READABILITY, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		// string 1.6, collection 1.2, map 1.2
		return JavaCore.VERSION_1_6;
	}
}
