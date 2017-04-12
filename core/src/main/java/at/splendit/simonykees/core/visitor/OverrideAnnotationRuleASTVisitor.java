package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * 
 * @author Ardit Ymeri
 *
 */
public class OverrideAnnotationRuleASTVisitor extends AbstractASTRewriteASTVisitor {
	
	private static final String OVERRIDE = "Override"; //$NON-NLS-1$
	
	@Override
	public boolean visit(TypeDeclaration node) {

		List<MethodDeclaration> methods = Arrays.asList(node.getMethods());
		
		ITypeBinding typeBinding = node.resolveBinding();
		List<ITypeBinding> ancestors = findAncestors(typeBinding);
		List<IMethodBinding> ancestorMethods = findAncestorMethods(ancestors);
		//FIXME: filter out private methods (for better performance)
//		List<IMethodBinding> ancestorOverridableMethods = 
//				ancestorMethods
//				.stream()
//				.filter(this::isAnnotatable)
//				.collect(Collectors.toList());
		List<MethodDeclaration> toBeAnnotated = new ArrayList<>();
		
		for(MethodDeclaration method : methods) {
			if(!method.isConstructor() 
					&& !isPrivate(method) && 
					!isOverrideAnnotated(method)) {
				IMethodBinding methodBinding = method.resolveBinding();
				
				for(IMethodBinding ancestorMember : ancestorMethods) {
					//FIXME: a null pointer exception is thrown here in weka project
					if(methodBinding.overrides(ancestorMember)) {
						toBeAnnotated.add(method);
						break;
					}
				}
			}
		}

		toBeAnnotated.stream().forEach(method -> {
			AST ast = method.getAST();
			ListRewrite listRewrite = getAstRewrite().getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY);
			MarkerAnnotation markerAnnotation = ast.newMarkerAnnotation();
			Name overrideName = ast.newName(OVERRIDE);
			markerAnnotation.setTypeName(overrideName);
			listRewrite.insertFirst(markerAnnotation, null);
			
		});
		
		return true;
	}
	
	private boolean isOverrideAnnotated(MethodDeclaration method) {
		//FIXME: use ASTNodeUtils
		List<MarkerAnnotation> annotations = 
				((List<Object>) method.modifiers())
				.stream()
				.filter(MarkerAnnotation.class::isInstance)
				.map(MarkerAnnotation.class::cast)
				.collect(Collectors.toList());
		
		return 
				annotations
				.stream()
				.filter(annotation -> annotation.getTypeName().getFullyQualifiedName().equals(OVERRIDE))
				.findAny()
				.isPresent();
	}
	
	private boolean isPrivate(MethodDeclaration method) {
		//FIXME: use ASTNodeUtils
		List<Modifier> modifiers = 
				((List<Object>) method.modifiers())
				.stream()
				.filter(Modifier.class::isInstance)
				.map(Modifier.class::cast)
				.collect(Collectors.toList());
		
		return 
				modifiers
				.stream()
				.filter(Modifier::isPrivate)
				.findAny()
				.isPresent();
	}

	private List<IMethodBinding> findAncestorMethods(List<ITypeBinding> ancestors) {
		List<IMethodBinding> allMethods = new ArrayList<>();
		ancestors
		.forEach(ancestor -> {
			List<IMethodBinding> ancestorMethods = Arrays.asList(ancestor.getDeclaredMethods());
			allMethods.addAll(ancestorMethods);
		});
		
		return allMethods;
	}

	private List<ITypeBinding> findAncestors(ITypeBinding typeBinding) {
		List<ITypeBinding> ancesotrs = new ArrayList<>();
		ITypeBinding parentClass = typeBinding.getSuperclass();
		if(parentClass != null) {
			ancesotrs.add(parentClass);
			ancesotrs.addAll(findAncestors(parentClass));
		}
		
		for(ITypeBinding iTypeBinding : typeBinding.getInterfaces()) {
			ancesotrs.add(iTypeBinding);
			ancesotrs.addAll(findAncestors(iTypeBinding));
		}
		
		return ancesotrs;
	}

}
