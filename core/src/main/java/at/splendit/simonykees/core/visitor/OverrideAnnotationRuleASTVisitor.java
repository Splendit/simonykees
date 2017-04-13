package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ASTNodeUtil;

/**
 * Adds the missing @{@link Override} annotation when overriding a method from a
 * parent class or interface.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class OverrideAnnotationRuleASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String OVERRIDE = Override.class.getSimpleName();

	@Override
	public boolean visit(TypeDeclaration node) {

		List<MethodDeclaration> methods = Arrays.asList(node.getMethods());
		ITypeBinding typeBinding = node.resolveBinding();
		List<ITypeBinding> ancestors = findAncestors(typeBinding);
		List<IMethodBinding> ancestorMethods = findOverridableAncestorMethods(ancestors);
		List<MethodDeclaration> toBeAnnotated = new ArrayList<>();

		for (MethodDeclaration method : methods) {
			// skip constructors and private methods
			if (!method.isConstructor() && !isPrivate(method) && !isOverrideAnnotated(method)) {

				IMethodBinding methodBinding = method.resolveBinding();
				if (methodBinding != null) {
					for (IMethodBinding ancestorMember : ancestorMethods) {
						// IMethodBinding::overrides is cool ;)
						if (methodBinding.overrides(ancestorMember)) {
							toBeAnnotated.add(method);
							// should not be marked for annotation more than
							// once
							break;
						}
					}
				}
			}
		}

		// add @Override to methods marked for annotation
		toBeAnnotated.stream()
				.forEach(method -> astRewrite.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY).insertFirst(
						NodeBuilder.newMarkerAnnotation(node.getAST(), node.getAST().newName(OVERRIDE)), null));
		return true;
	}

	/**
	 * Checks whether the given method is annotated as {@code @Override}.
	 * 
	 * @param method
	 *            method to be checked.
	 * @return true if the given method is annotated with {@code @Override}
	 */
	private boolean isOverrideAnnotated(MethodDeclaration method) {

		return ASTNodeUtil.convertToTypedList(method.modifiers(), MarkerAnnotation.class).stream()
				.filter(annotation -> {
					boolean isAnnotated = false;
					Name typeName = annotation.getTypeName();
					if (typeName != null) {
						isAnnotated = OVERRIDE.equals(typeName.getFullyQualifiedName());
					}
					return isAnnotated;
				}).findAny().isPresent();
	}

	/**
	 * Checks whether the given method has a private access modifier.
	 * 
	 * @param method
	 *            method to be checked.
	 * @return true if the given method is {@code private}
	 */
	private boolean isPrivate(MethodDeclaration method) {

		return ASTNodeUtil.convertToTypedList(method.modifiers(), Modifier.class).stream().filter(Modifier::isPrivate)
				.findAny().isPresent();
	}

	/**
	 * Finds the list of methods that are overridable from the given list of
	 * type bindings i.e. private methods and constructors are filtered out.
	 * 
	 * @param ancestors
	 * @return list of overridable methods
	 */
	private List<IMethodBinding> findOverridableAncestorMethods(List<ITypeBinding> ancestors) {
		List<IMethodBinding> allMethods = new ArrayList<>();
		ancestors.forEach(ancestor -> {
			List<IMethodBinding> overridableMethods = Arrays.asList(ancestor.getDeclaredMethods()).stream()
					.filter(method -> method != null).filter(method -> !Modifier.isPrivate(method.getModifiers()))
					.filter(method -> !method.isConstructor()).collect(Collectors.toList());

			allMethods.addAll(overridableMethods);
		});

		return allMethods;
	}

	/**
	 * Finds the list of type bindings of the supper classes and interfaces
	 * inherited by the given type binding.
	 * 
	 * @param typeBinding
	 * @return list of type bindings of all ancestors
	 */
	private List<ITypeBinding> findAncestors(ITypeBinding typeBinding) {
		List<ITypeBinding> ancesotrs = new ArrayList<>();

		if (typeBinding != null) {
			// get the type binding of super class
			ITypeBinding parentClass = typeBinding.getSuperclass();
			if (parentClass != null) {
				ancesotrs.add(parentClass);
				ancesotrs.addAll(findAncestors(parentClass));
			}

			// get type bindings of the implemented interfaces
			for (ITypeBinding iTypeBinding : typeBinding.getInterfaces()) {
				if (iTypeBinding != null) {
					ancesotrs.add(iTypeBinding);
					ancesotrs.addAll(findAncestors(iTypeBinding));
				}
			}
		}

		return ancesotrs;
	}

}
