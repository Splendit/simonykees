package eu.jsparrow.core.visitor.renaming;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.visitor.sub.VariableDeclarationsVisitor;

/**
 * A visitor for finding the references of a given field in the members
 * of a class. Collects all references of each member.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class BodyDeclarationsVisitor extends ASTVisitor {
	private SimpleName fieldName;
	private List<SimpleName> fieldReferences;
	private ITypeBinding parentTypeBinding;
	private int nestedTypeDeclarationLevel = 0;
	private List<String> declaredLocalVarNames;
	private ITypeBinding fieldTypeBinding;

	public BodyDeclarationsVisitor(SimpleName fieldName, ITypeBinding parentTypeBinding,
			ITypeBinding fieldTypeBinding) {

		this.fieldName = fieldName;
		this.fieldReferences = new ArrayList<>();
		this.parentTypeBinding = parentTypeBinding;
		this.declaredLocalVarNames = new ArrayList<>();
		this.fieldTypeBinding = fieldTypeBinding;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		visitReferences(fieldDeclaration);
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		visitReferences(methodDeclaration);
		return false;
	}

	@Override
	public boolean visit(Initializer initializer) {
		visitReferences(initializer);
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		nestedTypeDeclarationLevel++;
		visitReferences(typeDeclaration);

		return nestedTypeDeclarationLevel == 1;
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		nestedTypeDeclarationLevel--;
	}

	@Override
	public boolean visit(EnumDeclaration typeDeclaration) {
		visitReferences(typeDeclaration);
		return false;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration typeDeclaration) {
		visitReferences(typeDeclaration);
		return false;
	}

	/**
	 * Gathers the local variable names in the given bodyDeclaration. Then,
	 * finds the references of the {@link BodyDeclarationsVisitor#fieldName}.
	 * 
	 * @param bodyDeclaration
	 *            an node representing a {@link BodyDeclaration}.
	 */
	private void visitReferences(BodyDeclaration bodyDeclaration) {

		VariableDeclarationsVisitor declarationsVisitor = new VariableDeclarationsVisitor();
		bodyDeclaration.accept(declarationsVisitor);
		List<SimpleName> localDeclarations = declarationsVisitor.getVariableDeclarationNames();
		List<String> localDeclarationNames = localDeclarations.stream().map(SimpleName::getIdentifier)
				.collect(Collectors.toList());
		FieldReferencesVisitor visitor = new FieldReferencesVisitor(fieldName, parentTypeBinding, fieldTypeBinding,
				localDeclarationNames);
		bodyDeclaration.accept(visitor);
		fieldReferences.addAll(visitor.getReferences());
		declaredLocalVarNames.addAll(localDeclarationNames);
	}

	public List<SimpleName> getReferences() {
		return fieldReferences;
	}

	public List<String> getLocalVarNames() {
		return declaredLocalVarNames;
	}
}