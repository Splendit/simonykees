package eu.jsparrow.core.visitor.unused.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class MethodReferencesVisitor extends ASTVisitor {

	private Map<String, Boolean> optionsMap;
	private String methodDeclarationIdentifier;
	private IMethodBinding iMethodBinding;

	private List<MethodDeclaration> relatedTestDeclarations = new ArrayList<>();
	private boolean mainSourceReferenceFound = false;
	private boolean unresolvedReferenceFound = false;

	public MethodReferencesVisitor(MethodDeclaration methodDeclaration, Map<String, Boolean> optionsMap) {
		this.optionsMap = optionsMap;
		this.methodDeclarationIdentifier = methodDeclaration.getName()
			.getIdentifier();
		this.iMethodBinding = methodDeclaration.resolveBinding();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !unresolvedReferenceFound && !mainSourceReferenceFound;
	}
	
	@Override
	public boolean visit(ConstructorInvocation constructorInvocation) {
		IMethodBinding constructorBinding = constructorInvocation.resolveConstructorBinding();
		if(constructorBinding == null) {
			this.unresolvedReferenceFound = true;
			return false;
		}
		
		if(constructorBinding.isEqualTo(iMethodBinding)) {
			// FIXME: what if this is only used in a test? reuse the solution from visit(methodInvocation)
			this.mainSourceReferenceFound = true;
		}
		
		return true;
	}
	
	@Override
	public boolean visit(SuperConstructorInvocation superConstructorInvocation) {
		IMethodBinding constructorBinding = superConstructorInvocation.resolveConstructorBinding();
		if(constructorBinding == null) {
			this.unresolvedReferenceFound = true;
			return false;
		}
		
		if(constructorBinding.isEqualTo(iMethodBinding)) {
			// FIXME: what if this is only used in a test? reuse the solution from visit(methodInvocation)
			this.mainSourceReferenceFound = true;
		}
		
		return true;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName name = methodInvocation.getName();
		String identifier = name.getIdentifier();
		if (!identifier.equals(methodDeclarationIdentifier)) {
			return true;
		}

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			this.unresolvedReferenceFound = true;
			return false;
		}
		IMethodBinding declaration = methodBinding.getMethodDeclaration();
		if (!declaration.isEqualTo(iMethodBinding)) {
			return true;
		}

		MethodDeclaration enclosingMethodDeclaration = ASTNodeUtil.getSpecificAncestor(methodInvocation,
				MethodDeclaration.class);
		if (enclosingMethodDeclaration == null) {
			this.mainSourceReferenceFound = true;
			return false;
		}

		if (isRemoveTestsOptionSet() && isTestAnnotatedMethod(enclosingMethodDeclaration)) {
			this.relatedTestDeclarations.add(enclosingMethodDeclaration);
		} else {
			this.mainSourceReferenceFound = true;
			return false;
		}

		return true;
	}

	private boolean isRemoveTestsOptionSet() {
		return optionsMap.getOrDefault(Constants.REMOVE_TEST_CODE, false);
	}

	private boolean isTestAnnotatedMethod(MethodDeclaration methodDeclaration) {
		List<Annotation> annotations = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(), Annotation.class);
		for (Annotation annotation : annotations) {
			ITypeBinding typeBinding = annotation.resolveTypeBinding();

			List<String> supportedTestAnnotations = Arrays.asList("org.junit.Test", "org.junit.jupiter.api.Test");
			if (ClassRelationUtil.isContentOfTypes(typeBinding, supportedTestAnnotations)) {
				return true;
			}
		}
		return false;
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
}
