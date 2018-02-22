package org.eu.jsparrow.rules.api.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import project.JavaProjectBuilder;

public class RefactoringRuleTest {

	private RefactoringRule<?> rule;
	
	private IJavaProject project;

	@Before
	public void setUp() throws Exception {
		rule = new RefactoringRuleImpl();
		project = new JavaProjectBuilder().build();
	}
	
	@After
	public void tearDown() throws Exception {
		project.getProject().delete(true, null);
	}

	@Test
	public void enabledForProject_projectOnJava7_isEnabled() {
		project.setOption(JavaCore.COMPILER_COMPLIANCE, "1.7");
		
		rule.calculateEnabledForProject(project);

		assertTrue(rule.isEnabled());
	}
	

	@Test
	public void enabledForProject_projectOnJava5_isDisabled() throws Exception {
		project.setOption(JavaCore.COMPILER_COMPLIANCE, "1.5");
		
		rule.calculateEnabledForProject(project);

		assertFalse(rule.isEnabled());
	}

	class RefactoringRuleImpl extends RefactoringRule<AbstractASTRewriteASTVisitor> {

		RefactoringRuleImpl() {
			super();
		}

		@Override
		protected JavaVersion provideRequiredJavaVersion() {
			return JavaVersion.JAVA_1_7;
		}

	}
}
