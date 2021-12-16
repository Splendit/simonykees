package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.DiamondOperatorASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

/**
 * @see DiamondOperatorASTVisitor
 * 
 *      Minimum java version that supports diamond operator is
 *      {@value JavaVersion.JAVA_1_7}
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class DiamondOperatorRule extends RefactoringRuleImpl<DiamondOperatorASTVisitor> {

	public static final String RULE_ID = "DiamondOperator";//$NON-NLS-1$
	private String javaVersion;

	public DiamondOperatorRule() {
		super();
		this.visitorClass = DiamondOperatorASTVisitor.class;
		this.id = RULE_ID; 
		this.ruleDescription = new RuleDescription(Messages.DiamondOperatorRule_name,
				Messages.DiamondOperatorRule_description, Duration.ofMinutes(1),
				Arrays.asList(Tag.JAVA_1_7, Tag.OLD_LANGUAGE_CONSTRUCTS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_7;
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
	protected DiamondOperatorASTVisitor visitorFactory() {
		DiamondOperatorASTVisitor visitor = new DiamondOperatorASTVisitor(javaVersion);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}

}
