package eu.jsparrow.core.visitor.unused.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

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
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		if(!hasSelectedAccessModifier(methodDeclaration)) {
			return false;
		}
		
		if(hasUsefulAnnotations(methodDeclaration)) {
			return false;
		}
		
		/*
		 * Check if it is an entry point. I.e., public static void main(String []args)
		 */
		
		int modifiers = methodDeclaration.getModifiers();
		if (Modifier.isPrivate(modifiers)) {
			/*
			 * search only inside the compilation unit
			 * Use the MethodsReferencesVisitor
			 */
			ASTNode parent = methodDeclaration.getParent();
			if(parent instanceof AbstractTypeDeclaration) {
				AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) methodDeclaration.getParent();
				MethodReferencesVisitor visitor = new MethodReferencesVisitor(methodDeclaration, typeDeclaration, options);
				this.compilationUnit.accept(visitor);
				if (!visitor.hasMainSourceReference() && !visitor.hasUnresolvedReference()) {
					
					UnusedMethodWrapper unusedMethod = new UnusedMethodWrapper(compilationUnit, 
							JavaAccessModifier.PRIVATE, methodDeclaration, Collections.emptyList());
					this.unusedPrivateMethods.add(unusedMethod);
				}
			}
		} else {
			
			/*
			 * Search in the compilation unit. 
			 * If no references are found, check if this method is overriding any parent method.
			 * If not, then
			 * use the 'UnusedMethodsEngine' to search for external references.
			 */
		}

		return false;
	}
	
	private boolean hasSelectedAccessModifier(MethodDeclaration methodDeclaration) {
		// FIXME copied
		int modifierFlags = methodDeclaration.getModifiers();
		if (Modifier.isPublic(modifierFlags)) {
			return options.getOrDefault(Constants.PUBLIC_FIELDS, false);
		} else if (Modifier.isProtected(modifierFlags)) {
			return options.getOrDefault(Constants.PROTECTED_FIELDS, false);
		} else if (Modifier.isPrivate(modifierFlags)) {
			return options.getOrDefault(Constants.PRIVATE_FIELDS, false);
		} else {
			return options.getOrDefault(Constants.PACKAGE_PRIVATE_FIELDS, false);
		}
	}
	
	private boolean hasUsefulAnnotations(MethodDeclaration methodDeclaration) {
		//FIXME copied
		List<Annotation> annotations = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(), Annotation.class);
		for (Annotation annotation : annotations) {
			ITypeBinding typeBinding = annotation.resolveTypeBinding();
			if (!ClassRelationUtil.isContentOfTypes(typeBinding,
					Arrays.asList(java.lang.Deprecated.class.getName(), java.lang.SuppressWarnings.class.getName()))) {
				return true;
			}
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
