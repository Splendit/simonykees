package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

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
		addOverrideAnnotation(node, methods, typeBinding);
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		List<MethodDeclaration> methods = ASTNodeUtil.convertToTypedList(node.bodyDeclarations(),
				MethodDeclaration.class);
		ITypeBinding typeBinding = node.resolveBinding();
		addOverrideAnnotation(node, methods, typeBinding);
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		List<MethodDeclaration> methods = ASTNodeUtil.convertToTypedList(node.bodyDeclarations(),
				MethodDeclaration.class);
		ITypeBinding typeBinding = node.resolveBinding();
		addOverrideAnnotation(node, methods, typeBinding);
		return true;
	}

	/**
	 * Implements the functionality of inserting the @{@link Override}
	 * annotation above the methods that are overriding a parent method.
	 * 
	 * @param node
	 *            parent node having method declarations.
	 * @param methods
	 *            list of method declarations to be checked if the annotation is
	 *            needed.
	 * @param typeBinding
	 *            type binding of the parent node.
	 */
	private void addOverrideAnnotation(ASTNode node, List<MethodDeclaration> methods, ITypeBinding typeBinding) {
		List<ITypeBinding> ancestors = ClassRelationUtil.findAncestors(typeBinding);
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
				.map(MarkerAnnotation::getTypeName)
				.filter(typeName -> OVERRIDE.equals(typeName.getFullyQualifiedName())).findAny().isPresent();
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
					.filter(method -> !Modifier.isPrivate(method.getModifiers()) && !method.isConstructor())
					.collect(Collectors.toList());

			allMethods.addAll(overridableMethods);
		});

		return allMethods;
	}

}
