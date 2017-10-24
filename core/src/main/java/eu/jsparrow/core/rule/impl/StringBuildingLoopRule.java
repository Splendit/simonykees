package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleApplicationCount;
import eu.jsparrow.core.util.PropertyUtil;
import eu.jsparrow.core.visitor.impl.StringBuildingLoopASTVisitor;
import eu.jsparrow.i18n.Messages;

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
		this.name = Messages.StringBuildingLoopRule_name;
		this.description = Messages.StringBuildingLoopRule_description;
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
		visitor.addRewriteListener(RuleApplicationCount.get(this));
		return visitor;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}

}
