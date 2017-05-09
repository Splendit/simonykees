package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.wizard.Wizard;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerRule;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * Wizard for configuring logger rule when applying to selected resources
 * 
 * @author andreja.sambolec
 * @since 1.2
 *
 */
public class LoggerRuleWizard extends Wizard {

	private LoggerRuleWizardPage page;
	private LoggerRuleWizardPageModel model;
	private LoggerRuleWizardPageControler controler;

	private final List<IJavaElement> javaElements;
	private final StandardLoggerRule rule;

	public LoggerRuleWizard(List<IJavaElement> javaElements,
			RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		super();
		this.javaElements = javaElements;
		this.rule = (StandardLoggerRule) rule;
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return Messages.LoggerRuleWizard_title;
	}

	@Override
	public void addPages() {
		model = new LoggerRuleWizardPageModel(rule);
		controler = new LoggerRuleWizardPageControler(model);
		page = new LoggerRuleWizardPage(model, controler);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

}
