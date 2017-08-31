package at.splendit.simonykees.core.visitor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ReImplementingInterfaceASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(TypeDeclaration typeDeclarationNode) {
		if (!typeDeclarationNode.isInterface()) {
			if (!typeDeclarationNode.superInterfaceTypes().isEmpty()) {
				Type superclass = typeDeclarationNode.getSuperclassType();
				if (superclass != null) {
					List<Type> interfaces = ASTNodeUtil.convertToTypedList(typeDeclarationNode.superInterfaceTypes(),
							Type.class);

					ITypeBinding superclassTypeBinding = superclass.resolveBinding();
					if (superclassTypeBinding != null) {
						List<Type> duplicateInterfaces = getDuplicateInterfaces(superclassTypeBinding, interfaces);

						if (duplicateInterfaces != null) {
							ListRewrite interfacesListRewrite = astRewrite.getListRewrite(typeDeclarationNode,
									TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);

							duplicateInterfaces.forEach(
									duplicateInterface -> interfacesListRewrite.remove(duplicateInterface, null));
						}
					}
				}
			}
		}

		return false;
	}

	private List<Type> getDuplicateInterfaces(ITypeBinding superclass, List<Type> interfaces) {
		if (superclass != null && interfaces != null && !interfaces.isEmpty()) {

			List<Type> duplicateInterfaces = new LinkedList<>();

			while (superclass != null) {
				ITypeBinding[] superclassInterfaces = superclass.getInterfaces();

				Arrays.stream(superclassInterfaces).forEach(superClassInterface -> {
					interfaces.stream().filter(currentInterface -> !duplicateInterfaces.contains(currentInterface))
							.forEach(currentInterface -> {
								ITypeBinding interfaceTypeBinding = currentInterface.resolveBinding();
								if (ClassRelationUtil.compareITypeBinding(superClassInterface, interfaceTypeBinding)) {
									duplicateInterfaces.add(currentInterface);
								}
							});
				});

				superclass = superclass.getSuperclass();
			}

			return duplicateInterfaces;
		}

		return null;
	}
}
