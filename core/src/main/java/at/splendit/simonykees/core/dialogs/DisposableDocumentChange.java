package at.splendit.simonykees.core.dialogs;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.DocumentChange;

public class DisposableDocumentChange extends DocumentChange {
	
	private boolean disposed = false;

	public DisposableDocumentChange(String name, IDocument document) {
		super(name, document);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		this.disposed = true;
	}

	/**
	 * @return the disposed
	 */
	public boolean isDisposed() {
		return disposed;
	}

}
