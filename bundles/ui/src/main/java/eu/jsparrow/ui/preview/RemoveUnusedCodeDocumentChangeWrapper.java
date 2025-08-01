package eu.jsparrow.ui.preview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.visitor.unused.UnusedClassMemberWrapper;

/**
 * A wrapper class for storing the relation between document changes and the
 * rules that remove unused code.
 * 
 * @since 4.8.0
 *
 */
public class RemoveUnusedCodeDocumentChangeWrapper {

	private DocumentChange documentChange;
	private boolean isParent;
	private RemoveUnusedCodeDocumentChangeWrapper parent;
	private String identifier;
	private String compilationUnitName;
	private List<RemoveUnusedCodeDocumentChangeWrapper> children = new ArrayList<>();
	private UnusedClassMemberWrapper classMemberdData;
	private Document originalDocument;

	public RemoveUnusedCodeDocumentChangeWrapper(DocumentChange documentChange,
			RemoveUnusedCodeDocumentChangeWrapper parent, Document originalDocument, UnusedClassMemberWrapper classMemberData) {
		this.documentChange = documentChange;
		this.parent = parent;
		this.isParent = null == parent;
		this.identifier = classMemberData.getClassMemberIdentifier();
		this.compilationUnitName = classMemberData.getClassDeclarationName();
		this.classMemberdData = classMemberData;
		this.originalDocument = originalDocument;
	}

	private RemoveUnusedCodeDocumentChangeWrapper(DocumentChange documentChange,
			RemoveUnusedCodeDocumentChangeWrapper parent, String identifier,
			String compilationUnitName, Document compilationUnitSource, UnusedClassMemberWrapper classMemberDataData) {
		this.documentChange = documentChange;
		this.parent = parent;
		this.isParent = null == parent;
		this.identifier = identifier;
		this.compilationUnitName = compilationUnitName;
		this.originalDocument = compilationUnitSource;
		this.classMemberdData = classMemberDataData;
	}

	public DocumentChange getDocumentChange() {
		return documentChange;
	}

	public boolean isParent() {
		return isParent;
	}

	public void addChild(DocumentChange child, String compilationUnitName, Document document) {
		this.children.add(new RemoveUnusedCodeDocumentChangeWrapper(child, this, this.identifier,
				compilationUnitName, document, this.classMemberdData));
	}

	public RemoveUnusedCodeDocumentChangeWrapper[] getChildren() {
		return children.toArray(new RemoveUnusedCodeDocumentChangeWrapper[] {});
	}

	public RemoveUnusedCodeDocumentChangeWrapper getParent() {
		return parent;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getCompilationUnitName() {
		return compilationUnitName;
	}

	public Document getOriginalDocument() {
		return this.originalDocument;
	}

	public UnusedClassMemberWrapper getFieldData() {
		return classMemberdData;
	}

}
