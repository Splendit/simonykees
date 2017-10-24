package at.splendit.simonykees.core.ui.preview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ltk.core.refactoring.DocumentChange;

public class DocumentChangeWrapper {

	private DocumentChange documentChange;
	private boolean isParent;
	private DocumentChangeWrapper parent;
	private String oldIdentifier;
	private String newIdentifier;
	private String compilationUnitName;
	private String compilationUnitSource;
	private List<DocumentChangeWrapper> children = new ArrayList<>();

	public DocumentChangeWrapper(DocumentChange documentChange, DocumentChangeWrapper parent, String oldIdentifier,
			String newIdentifier, String compilationUnitName, String compilationUnitSource) {
		this.documentChange = documentChange;
		this.parent = parent;
		this.isParent = null == parent;
		this.oldIdentifier = oldIdentifier;
		this.newIdentifier = newIdentifier;
		this.compilationUnitName = compilationUnitName;
		this.compilationUnitSource = compilationUnitSource;
	}

	public DocumentChange getDocumentChange() {
		return documentChange;
	}

	public boolean isParent() {
		return isParent;
	}

	public void addChild(DocumentChange child, String compilationUnitName, String compilationUnitSource) {
		this.children.add(new DocumentChangeWrapper(child, this, this.oldIdentifier, this.newIdentifier,
				compilationUnitName, compilationUnitSource));
	}

	public DocumentChangeWrapper[] getChildren() {
		return children.toArray(new DocumentChangeWrapper[] {});
	}

	public DocumentChangeWrapper getParent() {
		return parent;
	}

	public String getOldIdentifier() {
		return oldIdentifier;
	}

	public String getNewIdentifier() {
		return newIdentifier;
	}

	public String getCompilationUnitName() {
		return compilationUnitName;
	}
	
	public String getCompilationUnitSource() {
		return compilationUnitSource;
	}
}
