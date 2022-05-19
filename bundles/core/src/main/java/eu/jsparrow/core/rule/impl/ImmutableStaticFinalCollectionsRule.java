package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.ImmutableStaticFinalCollectionsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ImmutableStaticFinalCollectionsRule
		extends RefactoringRuleImpl<ImmutableStaticFinalCollectionsASTVisitor> {

	public static final String RULE_ID = "ImmutableStaticFinalCollections"; //$NON-NLS-1$
	private String javaVersion;

	public ImmutableStaticFinalCollectionsRule() {
		super();
		this.visitorClass = ImmutableStaticFinalCollectionsASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.ImmutableStaticFinalCollectionsRule_name,
				Messages.ImmutableStaticFinalCollectionsRule_description, Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_2, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_2;
	}

	/**
	 * Stores java compiler compliance level.
	 */
	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		javaVersion = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		return true;
	}

	@Override
	protected ImmutableStaticFinalCollectionsASTVisitor visitorFactory() {
		ImmutableStaticFinalCollectionsASTVisitor visitor = new ImmutableStaticFinalCollectionsASTVisitor(javaVersion);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}
}
