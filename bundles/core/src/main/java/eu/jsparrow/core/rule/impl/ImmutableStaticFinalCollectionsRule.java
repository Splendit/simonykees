package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.ImmutableStaticFinalCollectionsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.util.PropertyUtil;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ImmutableStaticFinalCollectionsRule extends RefactoringRuleImpl<ImmutableStaticFinalCollectionsASTVisitor> {

	private JavaVersion javaVersion;

	public ImmutableStaticFinalCollectionsRule() {
		super();
		this.visitorClass = ImmutableStaticFinalCollectionsASTVisitor.class;
		this.id = "ImmutableStaticFinalCollections"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ImmutableStaticFinalCollectionsRule_name,
				Messages.ImmutableStaticFinalCollectionsRule_description, Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_2, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_2;
	}
	
	/**
	 * Stores java compiler compliance level.
	 */
	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		String compilerCompliance = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		javaVersion = PropertyUtil.stringToJavaVersion(compilerCompliance);
		return true;
	}

	@Override
	protected ImmutableStaticFinalCollectionsASTVisitor visitorFactory() {
		ImmutableStaticFinalCollectionsASTVisitor visitor = new ImmutableStaticFinalCollectionsASTVisitor(javaVersion);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}
}
