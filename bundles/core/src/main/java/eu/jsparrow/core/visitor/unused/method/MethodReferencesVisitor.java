package eu.jsparrow.core.visitor.unused.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class MethodReferencesVisitor extends ASTVisitor {

	private MethodDeclaration methodDeclaration;
	private AbstractTypeDeclaration typeDeclaration;
	private Map<String, Boolean> optionsMap;
	private String methodDeclarationIdentifier;
	private IMethodBinding iMethodBinding;
	
	private List<MethodDeclaration> relatedTestDeclarations = new ArrayList<>();
	private boolean mainSourceReferenceFound = false;
	private boolean unresolvedReferenceFound = false;
	
	public MethodReferencesVisitor(MethodDeclaration methodDeclaration, AbstractTypeDeclaration typeDeclaration,
			Map<String, Boolean> optionsMap) {
		this.methodDeclaration = methodDeclaration;
		this.typeDeclaration = typeDeclaration;
		this.optionsMap = optionsMap;
		this.methodDeclarationIdentifier = methodDeclaration.getName().getIdentifier();
		this.iMethodBinding = methodDeclaration.resolveBinding();
	}

	public List<MethodDeclaration> getRelatedTestDeclarations() {
		return relatedTestDeclarations;
	}

	public boolean hasMainSourceReference() {
		return this.mainSourceReferenceFound;
	}

	public boolean hasUnresolvedReference() {
		return this.unresolvedReferenceFound;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName name = methodInvocation.getName();
		String identifier = name.getIdentifier();
		if (!identifier.equals(methodDeclarationIdentifier)) {
			return true;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		IMethodBinding declaration = methodBinding.getMethodDeclaration();
		if(declaration.isEqualTo(iMethodBinding)) {
			MethodDeclaration enclosingMethodDeclaration = ASTNodeUtil.getSpecificAncestor(methodInvocation, MethodDeclaration.class);
			if(enclosingMethodDeclaration == null) {
				this.mainSourceReferenceFound = true;
				return true;
			}
			if(isTestAnnotatedMethod(enclosingMethodDeclaration)) {
				this.relatedTestDeclarations.add(enclosingMethodDeclaration);
			} else {
				this.mainSourceReferenceFound = true;
			}

		}
		return true;
	}
	
	private boolean isTestAnnotatedMethod(MethodDeclaration methodDeclaration) {
		List<Annotation> annotations = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(), Annotation.class);
		for(Annotation annotation : annotations) {
			ITypeBinding typeBinding = annotation.resolveTypeBinding();

			List<String> supportedTestAnnotations = Arrays.asList("org.junt.Test", "org.junt.jupiter.api.Test");
			if (ClassRelationUtil.isContentOfTypes(typeBinding, supportedTestAnnotations)) {
				return true;
			}
		}
		return false;
	}
}
