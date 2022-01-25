package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;

public class RemoveDeadCodeWizard extends AbstractRuleWizard {
	
	private static final Logger logger = LoggerFactory.getLogger(RemoveDeadCodeWizard.class);
	
	private RemoveDeadCodeWizardPageModel model;

	private IJavaProject selectedJavaProjekt;
	private List<ICompilationUnit> selectedJavaElements;
	
	public RemoveDeadCodeWizard(List<ICompilationUnit> selectedJavaElements) {
		
	}
	
	@Override
	public String getWindowTitle() {
		return "Remove Redundant Code";
	}

	@Override
	public void addPages() {
		model = new RemoveDeadCodeWizardPageModel();
		RemoveDeadCodeWizardPage page = new RemoveDeadCodeWizardPage(model);
		addPage(page);
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (model.getClassMemberTypes()
			.isEmpty()) {
			return false;
		}
		return super.canFinish();
	}


	@Override
	public boolean performFinish() {
		return false;
	}

}
