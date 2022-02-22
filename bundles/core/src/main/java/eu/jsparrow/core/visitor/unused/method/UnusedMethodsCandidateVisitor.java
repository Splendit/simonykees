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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.core.visitor.unused.BodyDeclarationsUtil;
import eu.jsparrow.core.visitor.utils.MethodDeclarationUtils;

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
		if(!BodyDeclarationsUtil.hasSelectedAccessModifier(methodDeclaration, options)) {
			return false;
		}
		
		if(BodyDeclarationsUtil.hasUsefulAnnotations(methodDeclaration)) {
			return false;
		}
		
		if(MethodDeclarationUtils.isJavaApplicationMainMethod(compilationUnit, methodDeclaration)) {
			return false;
		}
		
		int modifiers = methodDeclaration.getModifiers();
		if (Modifier.isPrivate(modifiers)) {
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
	


	public List<UnusedMethodWrapper> getUnusedPrivateMethods() {
		return unusedPrivateMethods;
	}

	public List<NonPrivateUnusedMethodCandidate> getNonPrivateCandidates() {
		return nonPrivateCandidates;
	}
}
