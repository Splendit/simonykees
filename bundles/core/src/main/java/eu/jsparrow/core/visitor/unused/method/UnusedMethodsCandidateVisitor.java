package eu.jsparrow.core.visitor.unused.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.core.visitor.unused.BodyDeclarationsUtil;
import eu.jsparrow.core.visitor.utils.MethodDeclarationUtils;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Finds the {@link MethodDeclaration} that are not used within a
 * {@link CompilationUnit}. If the method is private, it is immediately
 * classified as an unused method. Otherwise, further actions are needed to
 * determine whether the method is used or not. Therefore, it is only stored as
 * an unused candidate method.
 * 
 * Only the methods defined in the top level classes are analyzed. 
 * 
 * @since 4.9.0
 */
public class UnusedMethodsCandidateVisitor extends ASTVisitor {

	private Map<String, Boolean> options;
	private List<UnusedMethodWrapper> unusedPrivateMethods = new ArrayList<>();
	private List<NonPrivateUnusedMethodCandidate> nonPrivateCandidates = new ArrayList<>();

	private CompilationUnit compilationUnit;

	public UnusedMethodsCandidateVisitor(Map<String, Boolean> options) {
		this.options = options;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration type) {
		ITypeBinding typeBinding = type.resolveBinding();
		final String testCase = "junit.framework.TestCase"; //$NON-NLS-1$
		boolean isTest = ClassRelationUtil.isContentOfType(typeBinding, testCase)
				|| ClassRelationUtil.isInheritingContentOfTypes(typeBinding, Collections.singletonList(testCase));
		return !isTest;

	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		if (!BodyDeclarationsUtil.hasSelectedAccessModifier(methodDeclaration, options)) {
			return false;
		}

		if (methodDeclaration.isConstructor()) {
			return false;
		}

		if (BodyDeclarationsUtil.hasUsefulAnnotations(methodDeclaration)) {
			return false;
		}

		if (MethodDeclarationUtils.isMainMethod(methodDeclaration)) {
			return false;
		}

		ASTNode parent = methodDeclaration.getParent();
		if (!(parent instanceof AbstractTypeDeclaration)) {
			return false;
		}

		int modifiers = methodDeclaration.getModifiers();
		MethodReferencesVisitor visitor = new MethodReferencesVisitor(methodDeclaration, options);
		this.compilationUnit.accept(visitor);
		if (visitor.hasUnresolvedReference()) {
			return false;
		}

		if (visitor.hasMainSourceReference()) {
			return false;
		}
		if (Modifier.isPrivate(modifiers)) {
			UnusedMethodWrapper unusedMethod = new UnusedMethodWrapper(compilationUnit,
					JavaAccessModifier.PRIVATE, methodDeclaration, Collections.emptyList());
			this.unusedPrivateMethods.add(unusedMethod);
		} else {
			AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) methodDeclaration.getParent();
			ITypeBinding parentTypeBinding = typeDeclaration.resolveBinding();

			List<IMethodBinding> inheritedMethods = ClassRelationUtil.findInheretedMethods(parentTypeBinding);
			IMethodBinding methodBinding = methodDeclaration.resolveBinding();
			if (methodBinding == null) {
				return false;
			}
			for (IMethodBinding binding : inheritedMethods) {
				if (methodBinding.overrides(binding)) {
					return false;
				}
			}
			JavaAccessModifier accessModifier = BodyDeclarationsUtil.findAccessModifier(methodDeclaration);
			NonPrivateUnusedMethodCandidate candidate = new NonPrivateUnusedMethodCandidate(methodDeclaration,
					accessModifier);
			this.nonPrivateCandidates.add(candidate);
		}

		return false;
	}

	public List<UnusedMethodWrapper> getUnusedPrivateMethods() {
		return unusedPrivateMethods;
	}

	public List<NonPrivateUnusedMethodCandidate> getNonPrivateCandidates() {
		return nonPrivateCandidates;
	}
}
