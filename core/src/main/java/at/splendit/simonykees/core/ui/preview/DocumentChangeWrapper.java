package at.splendit.simonykees.core.ui.preview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ltk.core.refactoring.DocumentChange;

public class DocumentChangeWrapper {

	private DocumentChange documentChange;
	private boolean isParent;
	private DocumentChange parent;
	private List<DocumentChangeWrapper> children = new ArrayList<>();

	public DocumentChangeWrapper(DocumentChange documentChange, DocumentChange parent) {
		this.documentChange = documentChange;
		this.parent = parent;
		this.isParent = null == parent;
	}

	public DocumentChange getDocumentChange() {
		return documentChange;
	}

	public boolean isParent() {
		return isParent;
	}

	public void addChild(DocumentChange child) {
		this.children.add(new DocumentChangeWrapper(child, documentChange));
	}

	public DocumentChangeWrapper[] getChildren() {
		return children.toArray(new DocumentChangeWrapper[] {});
	}

	public DocumentChange getParent() {
		return parent;
	}
}
