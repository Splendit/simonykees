package eu.jsparrow.core.visitor.unused.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class OverridenMethodsVisitor extends ASTVisitor {

	private List<UnusedMethodWrapper> unusedMethods;
	private List<UnusedMethodWrapper> overriden = new ArrayList<>();
	private List<UnusedMethodWrapper> implicitlyOverrides = new ArrayList<>();

	public OverridenMethodsVisitor(List<UnusedMethodWrapper> unusedMethods) {
		this.unusedMethods = unusedMethods;
	}

	@Override
	public boolean visit(EnumDeclaration enumDeclaration) {

		List<EnumConstantDeclaration> enumConstantDeclarations = ASTNodeUtil
			.convertToTypedList(enumDeclaration.enumConstants(), EnumConstantDeclaration.class);
		for (EnumConstantDeclaration enumConstantDeclaration : enumConstantDeclarations) {
			AnonymousClassDeclaration anonymousClass = enumConstantDeclaration.getAnonymousClassDeclaration();
			if (anonymousClass != null) {
				List<MethodDeclaration> methodDeclarations = ASTNodeUtil
					.convertToTypedList(anonymousClass.bodyDeclarations(), MethodDeclaration.class);
				CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(enumDeclaration, CompilationUnit.class);
				IPath enumDeclcarationPath = compilationUnit.getJavaElement().getPath();
				List<UnusedMethodWrapper> relevantUnusedMethods = unusedMethods.stream()
					.filter(unused -> enumDeclcarationPath.equals(unused.getDeclarationPath()))
					.collect(Collectors.toList());
				for (MethodDeclaration methodDeclaration : methodDeclarations) {
					IMethodBinding methodBinding = methodDeclaration.resolveBinding();
					for (UnusedMethodWrapper unusedMethod : relevantUnusedMethods) {
						MethodDeclaration unusedDecl = unusedMethod.getMethodDeclaration();
						IMethodBinding unusedBinding = unusedDecl.resolveBinding();
						if (matchesNameAndParameters(methodBinding, unusedBinding)) {
							this.overriden.add(unusedMethod);
							break;
						}
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		
		ITypeBinding iTypeBinding = typeDeclaration.resolveBinding();
		List<ITypeBinding> superClasses = ClassRelationUtil.findSuperClasses(iTypeBinding);

		List<ITypeBinding> superInterfaces = ClassRelationUtil.findSuperInterfaces(iTypeBinding);

		List<String> superClassNames = superClasses.stream()
			.map(ITypeBinding::getErasure)
			.map(ITypeBinding::getQualifiedName)
			.collect(Collectors.toList());
		List<String> superInterfaceNames = superInterfaces.stream()
			.map(ITypeBinding::getErasure)
			.map(ITypeBinding::getQualifiedName)
			.collect(Collectors.toList());

		// 1. Find all unused methods in super classes and interfaces
		List<UnusedMethodWrapper> relevantSuperClassesMethods = unusedMethods.stream()
			.filter(unused -> isDeclaredIn(unused, superClassNames))
			.collect(Collectors.toList());
		List<UnusedMethodWrapper> relevantSuperInterfaceMethods = unusedMethods.stream()
			.filter(unused -> isDeclaredIn(unused, superInterfaceNames))
			.collect(Collectors.toList());

		// 2. Find all inherited methods from interfaces.
		List<IMethodBinding> superInterfaceMethodBindings = superInterfaces.stream()
			.flatMap(superType -> Arrays.stream(superType.getDeclaredMethods()))
			.collect(Collectors.toList());
		List<IMethodBinding> superClassMethodBindings = superClasses.stream()
			.flatMap(superType -> Arrays.stream(superType.getDeclaredMethods()))
			.collect(Collectors.toList());

		dropOverridenMethods(iTypeBinding, relevantSuperClassesMethods);

		dropOverridenMethods(iTypeBinding, relevantSuperInterfaceMethods);

		/*
		 * 4. For each relevant unused methods in super classes, find any
		 * implicitly overriden method defined in the inherited interfaces.
		 */

		if (!superInterfaces.isEmpty()) {
			dropImplicitOverridedMethods(relevantSuperClassesMethods, superInterfaceMethodBindings,
					superClassMethodBindings);
		}

		return true;
	}

	private boolean isDeclaredIn(UnusedMethodWrapper unused, List<String> superClassNames) {
		MethodDeclaration decl = unused.getMethodDeclaration();
		AbstractTypeDeclaration type = (AbstractTypeDeclaration) decl.getParent();
		ITypeBinding unusedDeclTypeBidning = type.resolveBinding();
		if (unusedDeclTypeBidning.isGenericType()) {
			unusedDeclTypeBidning = unusedDeclTypeBidning.getErasure();
		}
		return ClassRelationUtil.isContentOfTypes(unusedDeclTypeBidning, superClassNames);
	}

	private void dropOverridenMethods(ITypeBinding iTypeBinding,
			List<UnusedMethodWrapper> relevantSuperInterfaceMethods) {
		for (IMethodBinding declaredMethod : iTypeBinding.getDeclaredMethods()) {
			for (UnusedMethodWrapper unusedMethod : relevantSuperInterfaceMethods) {
				MethodDeclaration unusedDecl = unusedMethod.getMethodDeclaration();
				IMethodBinding unusedBinding = unusedDecl.resolveBinding();
				if (matchesNameAndParameters(declaredMethod, unusedBinding)) {
					this.overriden.add(unusedMethod);
					break;
				}
			}
		}
	}

	private void dropImplicitOverridedMethods(List<UnusedMethodWrapper> relevantSuperClassesMethods,
			List<IMethodBinding> superInterfaceMethodBindings, List<IMethodBinding> superClassMethodBindings) {
		for (IMethodBinding superClassMethod : superClassMethodBindings) {
			for (UnusedMethodWrapper unusedMethod : relevantSuperClassesMethods) {
				MethodDeclaration unusedDecl = unusedMethod.getMethodDeclaration();
				IMethodBinding unusedBinding = unusedDecl.resolveBinding();
				if (matchesNameAndParameters(superClassMethod, unusedBinding)) {
					for (IMethodBinding superInterfaceMethod : superInterfaceMethodBindings) {
						if (matchesNameAndParameters(superClassMethod, superInterfaceMethod)) {
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
		if (!currentName.equals(targetName)) {
			return false;
		}

		ITypeBinding[] currentParamTypes = current.getParameterTypes();
		ITypeBinding[] otherParamTypes = target.getParameterTypes();
		if (currentParamTypes.length != otherParamTypes.length) {
			return false;
		}
		for (int i = 0; i < currentParamTypes.length; i++) {
			ITypeBinding currentParamType = currentParamTypes[i];
			if (currentParamType.isParameterizedType()) {
				currentParamType = currentParamType.getErasure();
			}
			ITypeBinding otherParamType = otherParamTypes[i];
			if (otherParamType.isParameterizedType()) {
				otherParamType = otherParamType.getErasure();
			}
			if (currentParamType.isTypeVariable() || otherParamType.isTypeVariable()) {
				return true;
			}
			boolean sameTypes = ClassRelationUtil.compareITypeBinding(currentParamType, otherParamType);
			if (!sameTypes) {
				return false;
			}
		}
		return true;
	}

	public List<UnusedMethodWrapper> getOverriden() {
		return this.overriden;
	}

	public List<UnusedMethodWrapper> getImplicitOverrides() {
		return this.implicitlyOverrides;
	}
}
