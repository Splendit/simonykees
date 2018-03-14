package eu.jsparrow.rules.api.test.dummies;

import java.util.ArrayList;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

public class DummyRefactoringRule extends RefactoringRule<DummyVisitor> {

	public DummyRefactoringRule() {
		super();
		this.visitorClass = DummyVisitor.class;
		this.ruleDescription = new RuleDescription("dummy", "dummy", null, new ArrayList<>());
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_7;
	}

}