package eu.jsparrow.core.visitor.unused.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class OverridenMethodsVisitor extends ASTVisitor {
	
	private List<UnusedMethodWrapper> unusedMethods;
	private List<UnusedMethodWrapper> overriden = new ArrayList<>();
	private List<UnusedMethodWrapper> implicitlyOverrides = new ArrayList<>();

	public OverridenMethodsVisitor(List<UnusedMethodWrapper>unusedMethods) {
		this.unusedMethods = unusedMethods;
	}
	
	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		ITypeBinding iTypeBinding = typeDeclaration.resolveBinding();
		List<ITypeBinding> superClasses = findSuperClasses(iTypeBinding);

		
		List<ITypeBinding> superInterfaces = findSuperInterfaces(iTypeBinding);
		
		List<String> superClassNames = superClasses.stream()
				.map(ITypeBinding::getErasure)
				.map(ITypeBinding::getQualifiedName)
				.collect(Collectors.toList());
		List<String> superInterfaceNames = superInterfaces.stream()
				.map(ITypeBinding::getQualifiedName)
				.collect(Collectors.toList());
		
		// 1. Find all unused methods in super classes 
		List<UnusedMethodWrapper> relevantSuperClassesMethods = unusedMethods.stream()
		.filter(unused -> {
			MethodDeclaration decl = unused.getMethodDeclaration();
			AbstractTypeDeclaration type = (AbstractTypeDeclaration) decl.getParent();
			ITypeBinding unusedDeclTypeBidning = type.resolveBinding();
			return ClassRelationUtil.isContentOfTypes(unusedDeclTypeBidning, superClassNames);
		})
		.collect(Collectors.toList());
		
		List<UnusedMethodWrapper> relevantSuperInterfaceMethods = unusedMethods.stream()
				.filter(unused -> {
					MethodDeclaration decl = unused.getMethodDeclaration();
					AbstractTypeDeclaration type = (AbstractTypeDeclaration) decl.getParent();
					ITypeBinding unusedDeclTypeBidning = type.resolveBinding();
					return ClassRelationUtil.isContentOfTypes(unusedDeclTypeBidning, superInterfaceNames);
				})
		.collect(Collectors.toList());
		
		// 2. Find all inherited methods from interfaces.
		List<IMethodBinding> superInterfaceMethodBindings = superInterfaces.stream()
				.flatMap(superType -> Arrays.stream(superType.getDeclaredMethods()))
				.collect(Collectors.toList());
		List<IMethodBinding> superClassMethodBindings = superClasses.stream()
				.flatMap(superType -> Arrays.stream(superType.getDeclaredMethods()))
				.collect(Collectors.toList());

		
		// 3. Check if any method defined in the typeDeclaration is overriding any of the relevant methods 
		for(IMethodBinding declaredMethod : iTypeBinding.getDeclaredMethods()) {
			for(UnusedMethodWrapper unusedMethod : relevantSuperClassesMethods) {
				MethodDeclaration unusedDecl = unusedMethod.getMethodDeclaration();
				IMethodBinding unusedBinding = unusedDecl.resolveBinding();
				if(matchesNameAndParameters(declaredMethod, unusedBinding)) {
					this.overriden.add(unusedMethod);
					break;
				}
			}
		}
		
		for(IMethodBinding declaredMethod : iTypeBinding.getDeclaredMethods()) {
			for(UnusedMethodWrapper unusedMethod : relevantSuperInterfaceMethods) {
				MethodDeclaration unusedDecl = unusedMethod.getMethodDeclaration();
				IMethodBinding unusedBinding = unusedDecl.resolveBinding();
				if(matchesNameAndParameters(declaredMethod, unusedBinding)) {
					this.overriden.add(unusedMethod);
					break;
				}
			}
		}
		
		// 4. For each relevant unused methods in super classes, find any implicitly overriden method defined in the inherited interfaces. 
		if(superClasses.isEmpty() || superInterfaces.isEmpty()) {
			return true;
		}

		removeImplicitOverridedMethods(relevantSuperClassesMethods, superInterfaceMethodBindings,
				superClassMethodBindings);
		
		return true;
	}

	private void removeImplicitOverridedMethods(List<UnusedMethodWrapper> relevantSuperClassesMethods,
			List<IMethodBinding> superInterfaceMethodBindings, List<IMethodBinding> superClassMethodBindings) {
		for (IMethodBinding superClassMethod : superClassMethodBindings) {
			for(UnusedMethodWrapper unusedMethod : relevantSuperClassesMethods) {
				MethodDeclaration unusedDecl = unusedMethod.getMethodDeclaration();
				IMethodBinding unusedBinding = unusedDecl.resolveBinding();
				if(superClassMethod.isEqualTo(unusedBinding)) {
					for(IMethodBinding superInterfaceMethod : superInterfaceMethodBindings) {
						if(matchesNameAndParameters(superClassMethod, superInterfaceMethod)) {
							this.implicitlyOverrides.add(unusedMethod);
						}
					}
				}
			}
		}
	}
	
	private boolean matchesNameAndParameters(IMethodBinding current, IMethodBinding target) {
		String currentName = current.getName();
		String targetName = target.getName();
		if(!currentName.equals(targetName)) {
			return false;
		}
		
		ITypeBinding[] currentParamTypes = current.getParameterTypes();
		ITypeBinding[] otherParamTypes = target.getParameterTypes();
		if(currentParamTypes.length != otherParamTypes.length) {
			return false;
		}
		for(int i = 0; i<currentParamTypes.length; i++) {
			ITypeBinding currentParamType = currentParamTypes[i];
			ITypeBinding otherParamType = otherParamTypes[i];
			boolean sameTypes = ClassRelationUtil.compareITypeBinding(currentParamType, otherParamType);
			if(!sameTypes) {
				return false;
			}
		}
		return true;
	}
	
	private List<ITypeBinding> findSuperClasses(ITypeBinding type) {
		ITypeBinding iTypeBinding = type;
		List<ITypeBinding> superClasses = new ArrayList<>();
		while(true) {
			ITypeBinding superClass = iTypeBinding.getSuperclass();
			if(superClass != null) {
				superClasses.add(superClass);
			} else {
				break;
				
			}
			
			iTypeBinding = superClass;
			
		}
		return superClasses;
	}
	
	private List<ITypeBinding> findSuperInterfaces(ITypeBinding type) {
		List<ITypeBinding> interfaces = new ArrayList<>();
		for(ITypeBinding interfaceType :  type.getInterfaces()) {
			interfaces.add(interfaceType);
			List<ITypeBinding> grandParents = findSuperInterfaces(interfaceType);
			interfaces.addAll(grandParents);
		}
		ITypeBinding superClass = type.getSuperclass();
		if(superClass != null) {
			List<ITypeBinding> superClassInterfaces = findSuperInterfaces(superClass);
			interfaces.addAll(superClassInterfaces);
		}
		
		return interfaces;
	}
	
	public List<UnusedMethodWrapper> getOverriden() {
		return this.overriden;
	}
	
	public List<UnusedMethodWrapper> getImplicitOverrides() {
		return this.implicitlyOverrides;
	}
}
