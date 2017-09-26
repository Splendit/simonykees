package eu.jsparrow.ui.dialog;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.i18n.Messages;

/**
 * Dialog for displaying list of compilation units that contain compilation
 * errors
 * 
 * @author andreja.sambolec
 * @since 1.2
 */
public class CompilationErrorsMessageDialog extends TitleAreaDialog {

	private TableViewer tableViewer;

	public CompilationErrorsMessageDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.CompilationErrorsMessageDialog_compilationErrorDialogTitle);
		setMessage(Messages.CompilationErrorsMessageDialog_compilationErrorDialogMessage);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		/*
		 * Setting help listener to question mark help button Open default help
		 * dialog
		 */
		area.addHelpListener(new HelpListener() {
			@Override
			public void helpRequested(HelpEvent e) {
				SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell());
			}
		});

		tableViewer = new TableViewer(area, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableViewer.getControl().setLayoutData(gd);
		tableViewer.setUseHashlookup(true);

		tableViewer.setContentProvider(new IStructuredContentProvider() {

			@SuppressWarnings("unchecked")
			public Object[] getElements(Object inputElement) {
				List<ICompilationUnit> list = (List<ICompilationUnit>) inputElement;
				return list.toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		});

		/*
		 * LabelProvider is configured to display Class name and path for every
		 * file that contains compilation error
		 */
		tableViewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ICompilationUnit compUnit = (ICompilationUnit) element;
				return String.format("%s - %s", getClassNameString(compUnit), getPathString(compUnit)); //$NON-NLS-1$
			}
		});

		return area;
	}

	/**
	 * Returns the class name of an {@link ICompilationUnit}, including ".java"
	 * 
	 * @param compilationUnit
	 * @return
	 */
	private String getClassNameString(ICompilationUnit compilationUnit) {
		return compilationUnit.getElementName();
	}

	/**
	 * Returns the path of an {@link ICompilationUnit} without leading slash
	 * (the same as in the Externalize Strings refactoring view).
	 * 
	 * @param compilationUnit
	 * @return
	 */
	private String getPathString(ICompilationUnit compilationUnit) {
		String temp = compilationUnit.getParent().getPath().toString();
		return temp.startsWith("/") ? temp.substring(1) : temp; //$NON-NLS-1$
	}

	/**
	 * Buttons bar contains buttons Cancel if user wants to terminate process
	 * and Proceed to proceed with refactoring files that do not contain
	 * compilation errors.
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.PROCEED_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Sets the input to TableViewer with files that contain compilation errors
	 * 
	 * @param filesWithCompilationError
	 */
	public void setTableViewerInput(List<ICompilationUnit> filesWithCompilationError) {
		tableViewer.setInput(filesWithCompilationError);
	}
}
