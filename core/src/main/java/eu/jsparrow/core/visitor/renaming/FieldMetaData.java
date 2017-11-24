package eu.jsparrow.core.visitor.renaming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.text.edits.TextEditGroup;

/**
 * A type for storing information about a field to be renamed and all its
 * references.
 * 
 * @author Ardit Ymeri
 * @since 2.3.0
 *
 */
public class FieldMetaData {

	private List<ReferenceSearchMatch> references;
	private VariableDeclarationFragment declarationFragment;
	private String newIdentifier;
	private Map<ICompilationUnit, TextEditGroup> textEditGroups;
	private IPath declarationPath;
	private String classDeclarationName;

	public FieldMetaData(CompilationUnit cu, List<ReferenceSearchMatch> references,
			VariableDeclarationFragment fragment, String newIdentifier) {
		IJavaElement javaElement = cu.getJavaElement();
		IPath path = javaElement.getPath();
		String name = javaElement.getElementName();
		setDeclarationPath(path);
		setClassDeclarationName(name);
		this.references = references;
		this.declarationFragment = fragment;
		this.newIdentifier = newIdentifier;
		this.textEditGroups = new HashMap<>();

	}

	private void setClassDeclarationName(String name) {
		this.classDeclarationName = name;
	}
	
	public String getClassDeclarationName() {
		return this.classDeclarationName;
	}

	private void setDeclarationPath(IPath path) {
		this.declarationPath = path;
	}
	
	public IPath getDeclarationPath() {
		return this.declarationPath;
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


	/**
	 * 
	 * @return the list of the compilation unit having at least one reference of
	 *         the field being renamed.
	 */
	public List<ICompilationUnit> getTargetICompilationUnits() {
		return new ArrayList<>(textEditGroups.keySet());
	}
}
