package eu.jsparrow.core.visitor.impl;

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

import eu.jsparrow.core.markers.common.OverrideAnnotationEvent;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Adds the missing @{@link Override} annotation when overriding a method from a
 * parent class or interface.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class OverrideAnnotationRuleASTVisitor extends AbstractASTRewriteASTVisitor implements OverrideAnnotationEvent{

	private static final String OVERRIDE_SIMPLE_NAME = java.lang.Override.class.getSimpleName();
	private static final String JAVA_LANG_OVERRIDE = java.lang.Override.class.getName();

	@Override
	public boolean visit(TypeDeclaration node) {

		List<MethodDeclaration> methods = Arrays.asList(node.getMethods());
		methods = filterMethodDeclarations(methods);
		ITypeBinding typeBinding = node.resolveBinding();
		addOverrideAnnotation(node, methods, typeBinding);
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		List<MethodDeclaration> methods = ASTNodeUtil.convertToTypedList(node.bodyDeclarations(),
				MethodDeclaration.class);
		methods = filterMethodDeclarations(methods);
		ITypeBinding typeBinding = node.resolveBinding();
		addOverrideAnnotation(node, methods, typeBinding);
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		List<MethodDeclaration> methods = ASTNodeUtil.convertToTypedList(node.bodyDeclarations(),
				MethodDeclaration.class);
		methods = filterMethodDeclarations(methods);
		ITypeBinding typeBinding = node.resolveBinding();
		addOverrideAnnotation(node, methods, typeBinding);
		return true;
	}
	
	protected List<MethodDeclaration> filterMethodDeclarations(List<MethodDeclaration> methodDeclarations) { 
		return methodDeclarations;
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
			// skip constructors, private methods and methods that have the
			// @Override annotation
			if (!method.isConstructor() && !ASTNodeUtil.hasModifier(method.modifiers(), Modifier::isPrivate)
					&& !isOverrideAnnotated(method)) {

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
			.forEach(method -> {
				astRewrite.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY)
					.insertFirst(NodeBuilder.newMarkerAnnotation(node.getAST(), node.getAST()
						.newName(OVERRIDE_SIMPLE_NAME)), null);
				onRewrite();
				addMarkerEvent(method);
			});
	}

	/**
	 * Checks whether the given method is annotated as {@code @Override}.
	 * 
	 * @param method
	 *            method to be checked.
	 * @return true if the given method is annotated with {@code @Override}
	 */
	private boolean isOverrideAnnotated(MethodDeclaration method) {

		return ASTNodeUtil.convertToTypedList(method.modifiers(), MarkerAnnotation.class)
			.stream()
			.map(MarkerAnnotation::getTypeName)
			.anyMatch(typeName -> OVERRIDE_SIMPLE_NAME.equals(typeName.getFullyQualifiedName())
					|| JAVA_LANG_OVERRIDE.equals(typeName.getFullyQualifiedName()));
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
			List<IMethodBinding> overridableMethods = Arrays.asList(ancestor.getDeclaredMethods())
				.stream()
				.filter(method -> !Modifier.isPrivate(method.getModifiers()) && !method.isConstructor())
				.collect(Collectors.toList());

			allMethods.addAll(overridableMethods);
		});

		return allMethods;
	}

}
