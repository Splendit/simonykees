package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.rule.RuleApplicationCounter;
import eu.jsparrow.core.util.PropertyUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.core.visitor.impl.DiamondOperatorASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see DiamondOperatorASTVisitor
 * 
 * Minimum java version that supports diamond operator is {@value JavaVersion.JAVA_1_7}
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class DiamondOperatorRule extends AbstractRefactoringRule<DiamondOperatorASTVisitor> {

	private JavaVersion javaVersion;
	
	public DiamondOperatorRule() {
		super();
		this.visitorClass = DiamondOperatorASTVisitor.class;
		this.name = Messages.DiamondOperatorRule_name;
		this.description = Messages.DiamondOperatorRule_description;
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
		visitor.addRewriteListener(RuleApplicationCounter.get(this));
		return visitor;
	}

}
