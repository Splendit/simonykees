package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.rule.statistics.RuleApplicationCount;
import eu.jsparrow.core.util.PropertyUtil;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.DiamondOperatorASTVisitor;
import eu.jsparrow.i18n.Messages;

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
public class DiamondOperatorRule extends RefactoringRule<DiamondOperatorASTVisitor> {

	private JavaVersion javaVersion;

	public DiamondOperatorRule() {
		super();
		this.visitorClass = DiamondOperatorASTVisitor.class;
		this.id = "DiamondOperator"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.DiamondOperatorRule_name,
				Messages.DiamondOperatorRule_description, Duration.ofMinutes(1),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_7;
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
	protected DiamondOperatorASTVisitor visitorFactory() {
		DiamondOperatorASTVisitor visitor = new DiamondOperatorASTVisitor(javaVersion);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}

}
