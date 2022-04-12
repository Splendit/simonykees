package eu.jsparrow.core.visitor.unused.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.core.visitor.unused.UnusedClassMemberWrapper;

/**
 * Wraps information about unused types. E.g., the type declaration, the access
 * modifier, etc. Additionally, it maintains the {@link TextEditGroup}s related
 * to the type declaration.
 * 
 * @since 4.10.0
 *
 */
public class UnusedTypeWrapper implements UnusedClassMemberWrapper {

	private AbstractTypeDeclaration typeDeclaration;
	private JavaAccessModifier accessModifier;
	private IPath declarationPath;
	private CompilationUnit compilationUnit;
	private String classDeclarationName;
	private Map<IPath, TextEditGroup> textEditGroups = new HashMap<>();
	private String typeName;
	private boolean mainType;
	private List<TestReferenceOnType> testReferencesOnType;

	public UnusedTypeWrapper(CompilationUnit compilationUnit, JavaAccessModifier modifier,
			AbstractTypeDeclaration typeDeclaration, boolean mainType) {
		this(compilationUnit, modifier, typeDeclaration, mainType, Collections.emptyList());
	}

	public UnusedTypeWrapper(CompilationUnit compilationUnit, JavaAccessModifier modifier,
			AbstractTypeDeclaration typeDeclaration, boolean mainType, List<TestReferenceOnType> testReferencesOnType) {
		this.compilationUnit = compilationUnit;
		IJavaElement javaElement = compilationUnit.getJavaElement();
		this.declarationPath = javaElement.getPath();
		this.classDeclarationName = javaElement.getElementName();
		this.typeDeclaration = typeDeclaration;
		this.accessModifier = modifier;
		SimpleName name = typeDeclaration.getName();
		this.typeName = name.getIdentifier();
		this.mainType = mainType;
		this.testReferencesOnType = testReferencesOnType;
	}

	public AbstractTypeDeclaration getTypeDeclaration() {
		return typeDeclaration;
	}

	@Override
	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	@Override
	public JavaAccessModifier getAccessModifier() {
		return accessModifier;
	}

	@Override
	public IPath getDeclarationPath() {
		return declarationPath;
	}

	@Override
	public String getClassMemberIdentifier() {
		return typeName;
	}

	@Override
	public String getClassDeclarationName() {
		return classDeclarationName;
	}

	public boolean isMainType() {
		return mainType;
	}

	public List<TestReferenceOnType> getTestReferencesOnType() {
		return testReferencesOnType;
	}

	@Override
	public List<ICompilationUnit> getTargetICompilationUnits() {
		// because there are no external references
		// as soon as there is any reference on a type declaration, it cannot be
		// removed.

		return Collections.singletonList((ICompilationUnit) compilationUnit.getJavaElement());
	}

	public TextEditGroup getTextEditGroup(ICompilationUnit iCompilationUnit) {
		IPath path = iCompilationUnit.getPath();
		if (!textEditGroups.containsKey(path)) {
			TextEditGroup textEditGroup = new TextEditGroup(typeName);
			textEditGroups.put(path, textEditGroup);
			return textEditGroup;
		} else {
			return textEditGroups.get(path);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(accessModifier, classDeclarationName, compilationUnit, typeName, typeDeclaration);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UnusedTypeWrapper)) {
			return false;
		}
		UnusedTypeWrapper other = (UnusedTypeWrapper) obj;
		return accessModifier == other.accessModifier
				&& Objects.equals(classDeclarationName, other.classDeclarationName)
				&& Objects.equals(compilationUnit, other.compilationUnit) && Objects.equals(typeName, other.typeName)
				&& Objects.equals(typeDeclaration, other.typeDeclaration);
	}

	@Override
	public String toString() {
		return String.format(
				"UnusedTypeWrapper [typeName=%s, typeDeclaration=%s, accessModifier=%s, declarationPath=%s, classDeclarationName=%s]", //$NON-NLS-1$
				typeName, typeDeclaration, accessModifier, declarationPath, classDeclarationName);
	}
}
