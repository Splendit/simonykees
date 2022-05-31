package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.spring.ReplaceRequestMappingAnnotationASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * 
 * @see ReplaceRequestMappingAnnotationASTVisitor
 * 
 * @since 4.12.0
 */
public class ReplaceRequestMappingAnnotationRule
		extends RefactoringRuleImpl<ReplaceRequestMappingAnnotationASTVisitor> {

	private static final String SPRING_MIN_VERSION = "4.3.0"; //$NON-NLS-1$
	private static final String REQUEST_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.RequestMapping"; //$NON-NLS-1$
	public static final String RULE_ID = "ReplaceRequestMappingAnnotation"; //$NON-NLS-1$

	public ReplaceRequestMappingAnnotationRule() {
		this.visitorClass = ReplaceRequestMappingAnnotationASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(
				Messages.ReplaceRequestMappingAnnotationRule_name,
				Messages.ReplaceRequestMappingAnnotationRule_description,
				Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_5, Tag.SPRING, Tag.CODING_CONVENTIONS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {

		return JavaCore.VERSION_1_5;
	}

	@Override
	public String requiredLibraries() {
		return "Spring 4.3 or later"; //$NON-NLS-1$
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		Predicate<Version> versionComparator = version -> version
			.compareTo(Version.parseVersion(SPRING_MIN_VERSION)) >= 0;

		return isInProjectLibraries(project, REQUEST_MAPPING_ANNOTATION, versionComparator);
	}

}
