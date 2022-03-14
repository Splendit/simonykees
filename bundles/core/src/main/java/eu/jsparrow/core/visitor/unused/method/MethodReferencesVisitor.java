package eu.jsparrow.core.visitor.unused.method;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeMethodReference;

import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Finds references of method in a compilation unit. Determines whether the
 * reference occurs in a test case or not.
 * 
 * @since 4.9.0
 *
 */
public class MethodReferencesVisitor extends ASTVisitor {

	private Map<String, Boolean> optionsMap;
	private String methodDeclarationIdentifier;
	private IMethodBinding iMethodBinding;

	private Set<MethodDeclaration> relatedTestDeclarations = new HashSet<>();
	private boolean mainSourceReferenceFound = false;
	private boolean unresolvedReferenceFound = false;

	private static final List<String> supportedTestAnnotations = Collections.unmodifiableList(Arrays.asList(
			"org.junit.Test", //$NON-NLS-1$
			"org.junit.jupiter.api.Test")); //$NON-NLS-1$

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
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName name = methodInvocation.getName();
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		MethodDeclaration enclosingMethodDeclaration = ASTNodeUtil.getSpecificAncestor(methodInvocation,
				MethodDeclaration.class);

		return analyzeMethodInvocation(name, methodBinding, enclosingMethodDeclaration);
	}

	@Override
	public boolean visit(SuperMethodInvocation superInvocation) {
		SimpleName name = superInvocation.getName();
		IMethodBinding methodBinding = superInvocation.resolveMethodBinding();
		MethodDeclaration enclosingMethodDeclaration = ASTNodeUtil.getSpecificAncestor(superInvocation,
				MethodDeclaration.class);
		return analyzeMethodInvocation(name, methodBinding, enclosingMethodDeclaration);
	}

	@Override
	public boolean visit(ExpressionMethodReference methodReference) {
		return analyzeMethodReference(methodReference, methodReference.getName());
	}

	@Override
	public boolean visit(SuperMethodReference methodReference) {
		return analyzeMethodReference(methodReference, methodReference.getName());
	}

	@Override
	public boolean visit(TypeMethodReference methodReference) {
		return analyzeMethodReference(methodReference, methodReference.getName());
	}

	private boolean analyzeMethodReference(MethodReference methodReference, SimpleName methodName) {
		IMethodBinding methodBinding = methodReference.resolveMethodBinding();
		MethodDeclaration enclosingMethodDeclaration = ASTNodeUtil.getSpecificAncestor(methodReference,
				MethodDeclaration.class);
		return analyzeMethodInvocation(methodName, methodBinding, enclosingMethodDeclaration);
	}

	private boolean analyzeMethodInvocation(SimpleName name, IMethodBinding methodBinding,
			MethodDeclaration enclosingMethodDeclaration) {
		String identifier = name.getIdentifier();
		if (!identifier.equals(methodDeclarationIdentifier)) {
			return true;
		}

		if (methodBinding == null) {
			this.unresolvedReferenceFound = true;
			return false;
		}
		IMethodBinding declaration = methodBinding.getMethodDeclaration();
		if (!declaration.isEqualTo(iMethodBinding)) {
			return true;
		}

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
			if (ClassRelationUtil.isContentOfTypes(typeBinding, supportedTestAnnotations)) {
				return true;
			}
		}
		return false;
	}

	public Set<MethodDeclaration> getRelatedTestDeclarations() {
		return relatedTestDeclarations;
	}

	public boolean hasMainSourceReference() {
		return this.mainSourceReferenceFound;
	}

	public boolean hasUnresolvedReference() {
		return this.unresolvedReferenceFound;
	}
}
