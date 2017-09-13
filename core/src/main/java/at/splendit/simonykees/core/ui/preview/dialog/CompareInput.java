package at.splendit.simonykees.core.ui.preview.dialog;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.runtime.IProgressMonitor;

import at.splendit.simonykees.core.ui.preview.RefactoringSummaryWizardPage;
import at.splendit.simonykees.i18n.Messages;

/**
 * This class is used to create input for compare preview on
 * {@link RefactoringSummaryWizardPage}
 * 
 * @author Andreja Sambolec
 * @since 2.1
 *
 */
public class CompareInput extends CompareEditorInput {

	private Object fRoot;
	private String title;
	private String leftSide;
	private String rightSide;

	public CompareInput(String title, String leftSide, String rightSide) {
		super(new CompareConfiguration());
		this.title = title;
		this.leftSide = leftSide;
		this.rightSide = rightSide;
	}

	/**
	 * Runs the compare operation and returns the compare result. 
	 */
	@Override
	protected Object prepareInput(final IProgressMonitor pm) {

		final CompareItem ancestor = new CompareItem(Messages.CompareInput_ancestorName, "", 1L); //$NON-NLS-1$
		final CompareItem left = new CompareItem(Messages.CompareInput_leftName, leftSide, 1L);
		final CompareItem right = new CompareItem(Messages.CompareInput_rightName, rightSide, 1L);

		setTitle(title);

		Differencer d = new Differencer() {
			protected Object visit(Object parent, int description, Object ancestor, Object left, Object right) {
				return new DiffNode((IDiffContainer) parent, description, (CompareItem) ancestor, (CompareItem) left,
						(CompareItem) right);
			}
		};

		fRoot = d.findDifferences(false, pm, null, ancestor, left, right);
		return fRoot;
	}
}
