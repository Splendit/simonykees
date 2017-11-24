package eu.jsparrow.ui.preview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.visitor.renaming.FieldMetaData;

/**
 * Wrapper class for storing relation between DocumentChange holding origin of
 * the multiple file changing rule, and DocumentChanges that are affected by
 * that change.
 * 
 * @author Andreja Sambolec
 * @since 2.3.0
 *
 */
public class DocumentChangeWrapper {

	private DocumentChange documentChange;
	private boolean isParent;
	private DocumentChangeWrapper parent;
	private String oldIdentifier;
	private String newIdentifier;
	private String compilationUnitName;
	private List<DocumentChangeWrapper> children = new ArrayList<>();
	private FieldMetaData fieldData;
	private Document originalDocument;

	public DocumentChangeWrapper(DocumentChange documentChange, DocumentChangeWrapper parent, Document originalDocument, FieldMetaData fieldData) {
		this.documentChange = documentChange;
		this.parent = parent;
		this.isParent = null == parent;
		this.oldIdentifier = fieldData.getFieldDeclaration()
			.getName()
			.getIdentifier();
		this.newIdentifier = fieldData.getNewIdentifier();
		this.compilationUnitName = fieldData.getClassDeclarationName();
		this.fieldData = fieldData;
		this.originalDocument = originalDocument;
	}

	private DocumentChangeWrapper(DocumentChange documentChange, DocumentChangeWrapper parent, String oldIdentifier,
			String newIdentifier, String compilationUnitName, Document compilationUnitSource, FieldMetaData fieldData) {
		this.documentChange = documentChange;
		this.parent = parent;
		this.isParent = null == parent;
		this.oldIdentifier = oldIdentifier;
		this.newIdentifier = newIdentifier;
		this.compilationUnitName = compilationUnitName;
		this.originalDocument = compilationUnitSource;
		this.fieldData = fieldData;
	}

	public DocumentChange getDocumentChange() {
		return documentChange;
	}

	public boolean isParent() {
		return isParent;
	}

	public void addChild(DocumentChange child, String compilationUnitName, Document document) {
		this.children.add(new DocumentChangeWrapper(child, this, this.oldIdentifier, this.newIdentifier,
				compilationUnitName, document, this.fieldData));
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
	
	public Document getOriginalDocument() {
		return this.originalDocument;
	}

	public FieldMetaData getFieldData() {
		return fieldData;
	}
}
