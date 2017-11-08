package eu.jsparrow.core.visitor.renaming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEditGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A type for storing information about a field to be renamed and all its
 * references.
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class FieldMetadata {

	private static final Logger logger = LoggerFactory.getLogger(FieldMetadata.class);
	private CompilationUnit compilationUnit;
	private List<ReferenceSearchMatch> references;
	private VariableDeclarationFragment declarationFragment;
	private String newIdentifier;
	private Map<ICompilationUnit, TextEditGroup> textEditGroups;
	private Map<String, Document> documentMap;

	public FieldMetadata(CompilationUnit cu, List<ReferenceSearchMatch> references,
			VariableDeclarationFragment fragment, String newIdentifier) {
		this.compilationUnit = cu;
		this.references = references;
		this.declarationFragment = fragment;
		this.newIdentifier = newIdentifier;
		this.textEditGroups = new HashMap<>();
		this.documentMap = new HashMap<>();
		try {
			createDocument((ICompilationUnit) cu.getJavaElement());
		} catch (JavaModelException e) {
			logger.error("Cannot create document for displaying changes - " + e.getMessage(), e); //$NON-NLS-1$
		}
		references.forEach(referece -> {
			referece.setMetadata(this);
			try {
				createDocument(referece.getICompilationUnit());
			} catch (JavaModelException e1) {
				logger.error("Cannot create document for displaying changes - " + e1.getMessage(), e1); //$NON-NLS-1$
			}
		});

	}

	/**
	 * 
	 * @return the compilation unit where the field was declared.
	 */
	public CompilationUnit getCompilationUnit() {
		return this.compilationUnit;
	}

	/**
	 * 
	 * @return the declaration fragment of the field.
	 */
	public VariableDeclarationFragment getFieldDeclaration() {
		return this.declarationFragment;
	}

	/**
	 * 
	 * @return the references of the field in in the project.
	 */
	public List<ReferenceSearchMatch> getReferences() {
		return this.references;
	}

	/**
	 * 
	 * @return the new identifier complying with the naming conventions
	 */
	public String getNewIdentifier() {
		return newIdentifier;
	}

	/**
	 * 
	 * @return a {@link TextEditGroup} for keeping the text changes related to
	 *         the field and all its references in the given compilation unit.
	 * @throws JavaModelException
	 */
	public TextEditGroup getTextEditGroup(ICompilationUnit iCompilationUnit) {
		if (!textEditGroups.containsKey(iCompilationUnit)) {
			TextEditGroup textEditGroup = new TextEditGroup(newIdentifier);
			textEditGroups.put(iCompilationUnit, textEditGroup);
			return textEditGroup;
		} else {
			return textEditGroups.get(iCompilationUnit);
		}
	}

	private void createDocument(ICompilationUnit iCompilationUnit) throws JavaModelException {
		Document document = new Document(iCompilationUnit.getSource());
		documentMap.put(iCompilationUnit.getPath()
			.toString(), document);
	}

	/**
	 * 
	 * @return a {@link Document} before changes were applied to compilation
	 *         unit.
	 * @throws JavaModelException
	 */
	public Document getDocument(ICompilationUnit iCompilationUnit) {
		return documentMap.get(iCompilationUnit.getPath()
			.toString());
	}

	/**
	 * 
	 * @return the list of all {@link TextEditGroup} related to the changes of
	 *         the field.
	 */
	public List<TextEditGroup> getAllTexEditGroups() {
		return new ArrayList<>(textEditGroups.values());
	}

	/**
	 * 
	 * @return the list of the compilation unit having at least one reference of
	 *         the field being renamed.
	 */
	public List<ICompilationUnit> getTargetICompilationUnits() {
		return new ArrayList<>(textEditGroups.keySet());
	}
}
