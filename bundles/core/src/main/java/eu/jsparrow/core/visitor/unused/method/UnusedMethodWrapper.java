package eu.jsparrow.core.visitor.unused.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.core.visitor.unused.UnusedClassMemberWrapper;

/**
 * Wraps information about an unused method. Additionally, provides the relevant
 * {@link TextEdit}s for removing an unused method declaration.
 * 
 * @since 4.9.0
 */
public class UnusedMethodWrapper implements UnusedClassMemberWrapper {

	private CompilationUnit compilationUnit;
	private JavaAccessModifier accessModifier;
	private MethodDeclaration methodDeclaration;
	private List<TestSourceReference> testReferences;
	private IPath declarationPath;
	private String classMemberIdentifier;
	private String classDeclarationName;
	private Map<IPath, TextEditGroup> textEditGroups = new HashMap<>();

	public UnusedMethodWrapper(CompilationUnit compilationUnit, JavaAccessModifier accessModifier,
			MethodDeclaration methodDeclaration, List<TestSourceReference> testReferences) {
		this.compilationUnit = compilationUnit;
		this.accessModifier = accessModifier;
		this.methodDeclaration = methodDeclaration;
		this.testReferences = testReferences;
		IJavaElement javaElement = compilationUnit.getJavaElement();
		this.declarationPath = javaElement.getPath();
		this.classDeclarationName = javaElement.getElementName();
		SimpleName name = methodDeclaration.getName();
		classMemberIdentifier = name.getIdentifier();

	}

	public List<TestSourceReference> getTestReferences() {
		return testReferences;
	}

	@Override
	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	@Override
	public JavaAccessModifier getAccessModifier() {
		return accessModifier;
	}

	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	@Override
	public IPath getDeclarationPath() {
		return declarationPath;
	}

	@Override
	public String getClassMemberIdentifier() {
		return classMemberIdentifier;
	}

	@Override
	public String getClassDeclarationName() {
		return classDeclarationName;
	}

	@Override
	public List<ICompilationUnit> getTargetICompilationUnits() {
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		ICompilationUnit original = (ICompilationUnit) compilationUnit.getJavaElement();
		compilationUnits.add(original);

		for (TestSourceReference externalReferences : testReferences) {
			ICompilationUnit iCompilationUnit = externalReferences.getICompilationUnit();
			compilationUnits.add(iCompilationUnit);
		}
		return Collections.unmodifiableList(compilationUnits);
	}

	public TextEditGroup getTextEditGroup(ICompilationUnit iCompilationUnit) {
		IPath path = iCompilationUnit.getPath();
		if (!textEditGroups.containsKey(path)) {
			TextEditGroup textEditGroup = new TextEditGroup(classMemberIdentifier);
			textEditGroups.put(path, textEditGroup);
			return textEditGroup;
		} else {
			return textEditGroups.get(path);
		}
	}

	@Override
	public String toString() {
		String compilationUnitName = compilationUnit.getJavaElement()
			.getElementName();
		return String.format(
				"UnusedMethodWrapper [compilationUnit=%s, accessModifier=%s, methodDeclaration=%s, testReferences=%s]", //$NON-NLS-1$
				compilationUnitName, accessModifier, methodDeclaration, testReferences);
	}
}
