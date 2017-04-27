package at.splendit.simonykees.core.ui.preference;

import java.util.List;
import java.util.Set;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.wizard.impl.AbstractSelectRulesWizardModel;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

public class ConfigureProfileSelectRulesWIzardPageModel extends AbstractSelectRulesWizardModel {

	private String name = ""; //$NON-NLS-1$

	public ConfigureProfileSelectRulesWIzardPageModel(
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) {
		super(rules);
	}

	@Override
	public Set<Object> filterPosibilitiesByName() {
		return super.getAllPosibilities();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getNameFilter() {
		return ""; //$NON-NLS-1$
	}

}
