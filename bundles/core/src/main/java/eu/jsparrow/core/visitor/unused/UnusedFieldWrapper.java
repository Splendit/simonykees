package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;

public class UnusedFieldWrapper {

	private VariableDeclarationFragment fragment;
	private List<ExpressionStatement> internalReassignments;
	private JavaAccessModifier accessModifier;
	private List<UnusedExternalReferences> unusedExternalReferences = new ArrayList<>();
	private IPath declarationPath;
	private CompilationUnit compilationUnit;
	private String classDeclarationName;
	private Map<IPath, TextEditGroup> textEditGroups = new HashMap<>();
	private String fieldName;

	public UnusedFieldWrapper(CompilationUnit compilationUnit, JavaAccessModifier modifier,
			VariableDeclarationFragment fragment, List<ExpressionStatement> internalReassignments,
			List<UnusedExternalReferences> unusedExternalReferences) {
		this.compilationUnit = compilationUnit;
		IJavaElement javaElement = compilationUnit.getJavaElement();
		this.declarationPath = javaElement.getPath();
		this.classDeclarationName = javaElement.getElementName();
		this.fragment = fragment;
		this.internalReassignments = internalReassignments;
		this.unusedExternalReferences = unusedExternalReferences;
		this.accessModifier = modifier;
		SimpleName name = fragment.getName();
		this.fieldName = name.getIdentifier();
		
	}

	public VariableDeclarationFragment getFragment() {
		return fragment;
	}

	public List<ExpressionStatement> getUnusedReassignments() {
		return internalReassignments;
	}

	public List<UnusedExternalReferences> getUnusedExternalReferences() {
		return unusedExternalReferences;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public JavaAccessModifier getFieldModifier() {
		return accessModifier;
	}

	public IPath getDeclarationPath() {
		return declarationPath;
	}

	public String getFieldName() {
		return this.fieldName; 
	}

	public String getClassDeclarationName() {
		return classDeclarationName;
	}

	public List<ICompilationUnit> getTargetICompilationUnits() {
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		ICompilationUnit original = (ICompilationUnit) compilationUnit.getJavaElement();
		compilationUnits.add(original);

		for (UnusedExternalReferences externalReferences : unusedExternalReferences) {
			ICompilationUnit iCompilationUnit = externalReferences.getICompilationUnit();
			compilationUnits.add(iCompilationUnit);
		}
		return Collections.unmodifiableList(compilationUnits);
	}

	public TextEditGroup getTextEditGroup(ICompilationUnit iCompilationUnit) {
		IPath path = iCompilationUnit.getPath();
		if (!textEditGroups.containsKey(path)) {
			TextEditGroup textEditGroup = new TextEditGroup(fieldName);
			textEditGroups.put(path, textEditGroup);
			return textEditGroup;
		} else {
			return textEditGroups.get(path);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(accessModifier, classDeclarationName, compilationUnit, fieldName, fragment,
				internalReassignments, unusedExternalReferences);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UnusedFieldWrapper)) {
			return false;
		}
		UnusedFieldWrapper other = (UnusedFieldWrapper) obj;
		return accessModifier == other.accessModifier
				&& Objects.equals(classDeclarationName, other.classDeclarationName)
				&& Objects.equals(compilationUnit, other.compilationUnit) && Objects.equals(fieldName, other.fieldName)
				&& Objects.equals(fragment, other.fragment)
				&& Objects.equals(internalReassignments, other.internalReassignments)
				&& Objects.equals(unusedExternalReferences, other.unusedExternalReferences);
	}

	@Override
	public String toString() {
		return String.format(
				"UnusedFieldWrapper [fieldName=%s, fragment=%s, accessModifier=%s, declarationPath=%s, classDeclarationName=%s]", //$NON-NLS-1$
				fieldName, fragment, accessModifier, declarationPath, classDeclarationName);
	}
	
}
