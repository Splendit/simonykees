package eu.jsparrow.ui.preview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.visitor.renaming.FieldMetadata;

/**
 * Wrapper class for storing relation between DocumentChange holding origin of
 * the multiple file changing rule, and DocumentChanges that are affected by
 * that change.
 * 
 * @author Andreja Sambolec
 * 
 * @since 2.3
 *
 */
public class DocumentChangeWrapper {

	private DocumentChange documentChange;
	private boolean isParent;
	private DocumentChangeWrapper parent;
	private String oldIdentifier;
	private String newIdentifier;
	private String compilationUnitName;
	private String compilationUnitSource;
	private List<DocumentChangeWrapper> children = new ArrayList<>();
	private FieldMetadata fieldData;

	public DocumentChangeWrapper(DocumentChange documentChange, DocumentChangeWrapper parent, FieldMetadata fieldData)
			throws JavaModelException {
		this.documentChange = documentChange;
		this.parent = parent;
		this.isParent = null == parent;
		this.oldIdentifier = fieldData.getFieldDeclaration()
			.getName()
			.getIdentifier();
		this.newIdentifier = fieldData.getNewIdentifier();
		this.compilationUnitName = fieldData.getCompilationUnit()
			.getJavaElement()
			.getElementName();
		this.compilationUnitSource = ((ICompilationUnit) fieldData.getCompilationUnit()
			.getJavaElement()).getSource();
		this.fieldData = fieldData;
	}

	private DocumentChangeWrapper(DocumentChange documentChange, DocumentChangeWrapper parent, String oldIdentifier,
			String newIdentifier, String compilationUnitName, String compilationUnitSource, FieldMetadata fieldData) {
		this.documentChange = documentChange;
		this.parent = parent;
		this.isParent = null == parent;
		this.oldIdentifier = oldIdentifier;
		this.newIdentifier = newIdentifier;
		this.compilationUnitName = compilationUnitName;
		this.compilationUnitSource = compilationUnitSource;
		this.fieldData = fieldData;
	}

	public DocumentChange getDocumentChange() {
		return documentChange;
	}

	public boolean isParent() {
		return isParent;
	}

	public void addChild(DocumentChange child, String compilationUnitName, String compilationUnitSource) {
		this.children.add(new DocumentChangeWrapper(child, this, this.oldIdentifier, this.newIdentifier,
				compilationUnitName, compilationUnitSource, this.fieldData));
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

	public FieldMetadata getFieldData() {
		return fieldData;
	}
}
