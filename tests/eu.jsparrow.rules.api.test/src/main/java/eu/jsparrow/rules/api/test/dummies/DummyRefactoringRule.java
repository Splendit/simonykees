package eu.jsparrow.rules.api.test.dummies;

import java.util.ArrayList;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * 
 * @author Hans-Jörg Schnedlitz
 * @since 2.5.0
 */
@SuppressWarnings("nls")
public class DummyRefactoringRule extends RefactoringRuleImpl<DummyVisitor> {

	public DummyRefactoringRule() {
		super();
		this.visitorClass = DummyVisitor.class;
		this.ruleDescription = new RuleDescription("dummy", "dummy", null, new ArrayList<>());
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_7;
	}

}