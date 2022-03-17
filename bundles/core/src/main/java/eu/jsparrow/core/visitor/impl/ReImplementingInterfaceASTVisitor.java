package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.ReImplementingInterfaceEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This rule visits all super classes of the current {@link TypeDeclaration} and
 * checks if one of them already implements an interface, which is also
 * implemented by the current {@link TypeDeclaration}. If duplicates are found,
 * they are removed, so that only the super class implements the interface. The
 * current {@link TypeDeclaration} inherits the interfaces anyway. Overridden
 * methods don't change at all
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ReImplementingInterfaceASTVisitor extends AbstractASTRewriteASTVisitor implements ReImplementingInterfaceEvent {

	@Override
	public boolean visit(TypeDeclaration typeDeclarationNode) {
		if (!typeDeclarationNode.isInterface() && !typeDeclarationNode.superInterfaceTypes()
			.isEmpty()) {
			Type superclass = typeDeclarationNode.getSuperclassType();
			if (superclass != null) {
				List<Type> interfaces = ASTNodeUtil.convertToTypedList(typeDeclarationNode.superInterfaceTypes(),
						Type.class);

				ITypeBinding superclassTypeBinding = superclass.resolveBinding();
				if (superclassTypeBinding != null) {
					List<Type> duplicateInterfaces = getDuplicateInterfaces(superclassTypeBinding, interfaces);

					if (!duplicateInterfaces.isEmpty()) {
						ListRewrite interfacesListRewrite = astRewrite.getListRewrite(typeDeclarationNode,
								TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);

						duplicateInterfaces.forEach(duplicateInterface -> {
							interfacesListRewrite.remove(duplicateInterface, null);
							onRewrite();
							addMarkerEvent(duplicateInterface);
						});
					}
				}
			}
		}

		return false;
	}

	/**
	 * this method visits all super classes, checks their interfaces and finds
	 * duplicates
	 * 
	 * @param superclass
	 *            {@link ITypeBinding} from the super class, where the search
	 *            should start
	 * @param interfaces
	 *            a list of interfaces (of type {@link Type}) which the current
	 *            class implements
	 * @return a list of type {@link Type} with all the duplicate interface
	 *         implementations.
	 */
	private List<Type> getDuplicateInterfaces(ITypeBinding superclass, List<Type> interfaces) {
		List<Type> duplicateInterfaces = new LinkedList<>();

		if (superclass != null && interfaces != null && !interfaces.isEmpty()) {
			ITypeBinding superclassTypeBinding = superclass;

			while (superclassTypeBinding != null) {
				ITypeBinding[] superclassInterfaces = superclassTypeBinding.getInterfaces();

				Arrays.stream(superclassInterfaces)
					.forEach(superClassInterface -> interfaces.stream()
						.filter(currentInterface -> !duplicateInterfaces.contains(currentInterface))
						.forEach(currentInterface -> {
							ITypeBinding interfaceTypeBinding = currentInterface.resolveBinding();
							if (ClassRelationUtil.compareITypeBinding(superClassInterface, interfaceTypeBinding)) {
								duplicateInterfaces.add(currentInterface);
							}
						}));

				superclassTypeBinding = superclassTypeBinding.getSuperclass();
			}
		}

		return duplicateInterfaces;
	}
}
