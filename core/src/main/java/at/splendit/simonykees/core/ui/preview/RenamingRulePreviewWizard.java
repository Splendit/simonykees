package at.splendit.simonykees.core.ui.preview;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.rule.impl.PublicFieldsRenamingRule;
import at.splendit.simonykees.core.visitor.renaming.FieldMetadata;

public class RenamingRulePreviewWizard extends Wizard {

	private Map<String, List<DocumentChange>> documentChanges;
	private PublicFieldsRenamingRule rule;
	private Shell shell;
	Map<String, FieldMetadata> metadataMap;

	public RenamingRulePreviewWizard(Map<String, List<DocumentChange>> documentChanges, Map<String, FieldMetadata> dataMap, PublicFieldsRenamingRule rule) {
		this.documentChanges = documentChanges;
		this.rule = rule;
		this.shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		this.metadataMap = dataMap;
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		addPage(new RenamingRulePreviewWizardPage(documentChanges, metadataMap, rule));
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
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
