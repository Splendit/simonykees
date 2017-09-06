package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerRule;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

public class RenameFieldsRuleWizard extends Wizard {

	private static final Logger logger = LoggerFactory.getLogger(RenameFieldsRuleWizard.class);

	private RenameFieldsRuleWizardPage page;
	private RenameFieldsRuleWizardPageModel model;
	private RenameFieldsRuleWizardPageControler controler;

	private IJavaProject selectedJavaProjekt;
	// TODO change to RenameFieldsRule
	private final StandardLoggerRule rule;

	private RefactoringPipeline refactoringPipeline;

	public RenameFieldsRuleWizard(IJavaProject selectedJavaProjekt,
			RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule, RefactoringPipeline refactoringPipeline) {
		super();
		this.selectedJavaProjekt = selectedJavaProjekt;
		this.refactoringPipeline = refactoringPipeline;
		this.rule = (StandardLoggerRule) rule;
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return Messages.LoggerRuleWizard_title;
	}

	@Override
	public void addPages() {
		model = new RenameFieldsRuleWizardPageModel(rule);
		controler = new RenameFieldsRuleWizardPageControler(model);
		page = new RenameFieldsRuleWizardPage(model, controler);
		addPage(page);
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (model.getFieldTypes().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

}
