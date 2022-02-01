package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;

public class UnusedFieldWrapper {

	private VariableDeclarationFragment fragment;
	private List<SimpleName> internalReassignments;
	private JavaAccessModifier accessModifier;
	private List<UnusedExternalReferences> unusedExternalReferences = new ArrayList<>();
	private IPath declarationPath;
	private CompilationUnit compilationUnit;
	private String classDeclarationName;
	private Map<ICompilationUnit, TextEditGroup> textEditGroups = new HashMap<>();
	private String fieldName;

	public UnusedFieldWrapper(CompilationUnit compilationUnit, JavaAccessModifier modifier,
			VariableDeclarationFragment fragment, List<SimpleName> internalReassignments,
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

	public List<SimpleName> getUnusedReassignments() {
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
		if (!textEditGroups.containsKey(iCompilationUnit)) {
			TextEditGroup textEditGroup = new TextEditGroup(fieldName);
			textEditGroups.put(iCompilationUnit, textEditGroup);
			return textEditGroup;
		} else {
			return textEditGroups.get(iCompilationUnit);
		}
	}
}
