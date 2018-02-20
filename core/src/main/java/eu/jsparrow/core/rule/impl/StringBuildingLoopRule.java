package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.StringBuildingLoopASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.util.PropertyUtil;

/**
 * @see StringBuildingLoopASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class StringBuildingLoopRule extends RefactoringRule<StringBuildingLoopASTVisitor> {

	private JavaVersion javaVersion;

	public StringBuildingLoopRule() {
		super();
		this.visitorClass = StringBuildingLoopASTVisitor.class;
		this.id = "StringBuildingLoop"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.StringBuildingLoopRule_name,
				Messages.StringBuildingLoopRule_description, Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_5, Tag.JAVA_1_8, Tag.LAMBDA, Tag.LOOP));
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
	protected StringBuildingLoopASTVisitor visitorFactory() {
		StringBuildingLoopASTVisitor visitor = new StringBuildingLoopASTVisitor(javaVersion);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}

}
