package eu.jsparrow.rules.java10;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

@SuppressWarnings("restriction")
public class LocalVariableTypeInferenceRule extends RefactoringRuleImpl<LocalVariableTypeInferenceASTVisitor> {

	public LocalVariableTypeInferenceRule() {
		super();
		this.visitorClass = LocalVariableTypeInferenceASTVisitor.class;
		this.id = "OrganiseImports"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("",
				"", Duration.ofMinutes(1),
				Arrays.asList(Tag.JAVA_1_10, Tag.FORMATTING, Tag.READABILITY));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
	
	
	public boolean ruleSpecificImplementation(IJavaProject project) {
		return JavaModelUtil.is10OrHigher(project);		
	}
	
}
