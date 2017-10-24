package at.splendit.simonykees.core.ui.preview;

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.rule.impl.PublicFieldsRenamingRule;
import at.splendit.simonykees.core.visitor.renaming.FieldMetadata;

public class RenamingRulePreviewWizard extends Wizard {

	private RefactoringPipeline refactoringPipeline;

	private Map<FieldMetadata, Map<ICompilationUnit, DocumentChange>> documentChanges;
	private PublicFieldsRenamingRule rule;

	public RenamingRulePreviewWizard(RefactoringPipeline refactoringPipeline,
			Map<FieldMetadata, Map<ICompilationUnit, DocumentChange>> documentChanges, PublicFieldsRenamingRule rule) {
		this.refactoringPipeline = refactoringPipeline;
		this.documentChanges = documentChanges;
		this.rule = rule;
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		addPage(new RenamingRulePreviewWizardPage(documentChanges, rule));
	}

	@Override
	public boolean performFinish() {
		getPage(rule.getName()).isPageComplete();
		try {
			refactoringPipeline.commitRefactoring();
		} catch (RefactoringException | ReconcileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}
}
