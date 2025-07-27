package eu.jsparrow.rules.common.visitor.helper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class LocalVariableReferencesCollectorVisitor extends AbstractLocalVariableReferencesVisitor {

	protected final List<SimpleName> references = new ArrayList<>();

	public LocalVariableReferencesCollectorVisitor(CompilationUnit compilationUnit,
			VariableDeclarationFragment targetDeclarationFragment) {
		super(compilationUnit, targetDeclarationFragment);
	}

	@Override
	protected void referenceFound(SimpleName simpleName) {
		references.add(simpleName);
	}

	public List<SimpleName> getReferences() {
		return references;
	}

}
