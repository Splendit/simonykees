package at.splendit.simonykees.core.ui.preview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.TextEditGroup;

public class DocumentChangeWrapper {

	private DocumentChange documentChange;
	private boolean isParent;
	private DocumentChange parent;
	private TextEditGroup parentEditGroup;
	private List<DocumentChangeWrapper> children = new ArrayList<>();

	public DocumentChangeWrapper(DocumentChange documentChange, TextEditGroup parentEditGroup, DocumentChange parent) {
		this.documentChange = documentChange;
		this.parent = parent;
		this.isParent = null == parent;
		this.parentEditGroup = parentEditGroup;
	}

	public DocumentChange getDocumentChange() {
		return documentChange;
	}

	public boolean isParent() {
		return isParent;
	}

	public void addChild(DocumentChange child, TextEditGroup editGroup) {
		this.children.add(new DocumentChangeWrapper(child, editGroup, documentChange));
	}

	public DocumentChangeWrapper[] getChildren() {
		return children.toArray(new DocumentChangeWrapper[] {});
	}

	public DocumentChange getParent() {
		return parent;
	}
	
	public TextEditGroup getEditGroup() {
		return this.parentEditGroup;
	}
}
