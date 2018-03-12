package org.eu.jsparrow.rules.api.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.jsparrow.jdtunit.util.CompilationUnitBuilder;
import eu.jsparrow.jdtunit.util.JavaProjectBuilder;
import eu.jsparrow.jdtunit.util.PackageFragmentBuilder;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

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
		project.getProject()
			.delete(true, null);
	}

	@Test
	public void enabledForProject_projectOnJava7_isEnabled() {
		project.setOption(JavaCore.COMPILER_COMPLIANCE, "1.7");

		rule.calculateEnabledForProject(project);

		assertTrue(rule.isEnabled());
	}

	@Test
	public void enabledForProject_projectOnJava5_isDisabled() {
		project.setOption(JavaCore.COMPILER_COMPLIANCE, "1.5");

		rule.calculateEnabledForProject(project);

		assertFalse(rule.isEnabled());
	}


	class RefactoringRuleImpl extends RefactoringRule<AbstractASTRewriteASTVisitor> {

		RefactoringRuleImpl() {
			super();
			this.ruleDescription = new RuleDescription("dummy", "dummy", null, new ArrayList<>());
		}

		@Override
		protected JavaVersion provideRequiredJavaVersion() {
			return JavaVersion.JAVA_1_7;
		}

	}

}
