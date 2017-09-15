package at.splendit.simonykees.ui.preview.dialog;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.IProgressMonitor;

import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.ui.preview.RefactoringSummaryWizardPage;

/**
 * This class is used to create input for compare preview on
 * {@link RefactoringSummaryWizardPage}
 * 
 * @author Andreja Sambolec
 * @since 2.1
 *
 */
public class CompareInput extends CompareEditorInput {

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

		Object fRoot;

		final CompareItem ancestor = new CompareItem(Messages.CompareInput_ancestorName, ""); //$NON-NLS-1$
		final CompareItem left = new CompareItem(Messages.CompareInput_leftName, leftSide);
		final CompareItem right = new CompareItem(Messages.CompareInput_rightName, rightSide);

		setTitle(title);

		Differencer d = new Differencer();

		fRoot = d.findDifferences(false, pm, null, ancestor, left, right);
		if (null == fRoot) {
			return new DiffNode(null, Differencer.CONFLICTING, ancestor, left, right);
		}
		return fRoot;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getName() {
		return title;
	}

}
