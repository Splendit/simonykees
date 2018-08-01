package eu.jsparrow.rules.java10;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class LocalVariableTypeInferenceRule extends RefactoringRuleImpl<LocalVariableTypeInferenceASTVisitor> {

	public LocalVariableTypeInferenceRule() {
		super();
		this.visitorClass = LocalVariableTypeInferenceASTVisitor.class;
		this.id = "LocalVariableTypeInference"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("LocalVariableTypeInferance-WorkingTitle",
				"LocalVariableTypeInferance-WorkingDescription", Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_10, Tag.FORMATTING, Tag.READABILITY));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		String compiler_source = project != null ? project.getOption(JavaCore.COMPILER_SOURCE, true)
				: JavaCore.getOption(JavaCore.COMPILER_SOURCE);
		boolean java10plus = JavaCore.compareJavaVersions(compiler_source, JavaCore.VERSION_10) >= 0;
		return java10plus;
	}

}
