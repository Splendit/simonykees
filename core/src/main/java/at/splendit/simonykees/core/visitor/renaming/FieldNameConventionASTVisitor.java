package at.splendit.simonykees.core.visitor.renaming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Renames the private fields (except for static final ones) to comply with 
 * the naming convention: "^[a-z][a-zA-Z0-9]*$" i.e. a lower case prefix followed
 * by any sequence of alpha-numeric characters.
 * For example, the fields of the the following class:
 * <pre>
 * 
 * {@code 
 * 		class Foo {
 * 			private String Improper_NAME;
 * 			private String NON$JAVA_style;
 * 			private int Number;
 * 			private int _int;
 * 		}
 * }
 * 
 * </pre>
 * would be renamed to:
 * <pre>
 * 
 * {@code 
 * 		class Foo {
 * 			private String improperName;
 * 			private String nonJavaStyle;
 * 			private int number;
 * 			private int _int;
 * 		}
 * }
 * </pre>
 * 
 * Note that the field {@code _int} is not renamed because the resulting 
 * new name would be {@code int} which is not eligible for a variable name.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class FieldNameConventionASTVisitor extends AbstractASTRewriteASTVisitor {

	private List<String> declaredFieldNames = new ArrayList<>();

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {

		List<FieldDeclaration> fields = Arrays.asList(typeDeclaration.getFields());

		declaredFieldNames = fields.stream()
				.flatMap(fieldDecl -> ASTNodeUtil
						.convertToTypedList(fieldDecl.fragments(), VariableDeclarationFragment.class).stream())
				.map(VariableDeclarationFragment::getName).map(SimpleName::getIdentifier).collect(Collectors.toList());

		return true;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		/**
		 * Only private fields can be renamed, unless they are static final.
		 */
		if (ASTNodeUtil.hasModifier(fieldDeclaration.modifiers(), modifier -> modifier.isPrivate())
				&& !(ASTNodeUtil.hasModifier(fieldDeclaration.modifiers(), modifier -> modifier.isStatic())
						&& ASTNodeUtil.hasModifier(fieldDeclaration.modifiers(), modifier -> modifier.isFinal()))) {

			ASTNode parent = fieldDeclaration.getParent();
			if (parent != null && parent.getNodeType() == ASTNode.TYPE_DECLARATION) {
				// find the type binding of the parent
				TypeDeclaration type = (TypeDeclaration) parent;
				ITypeBinding parentTypeBidning = type.resolveBinding();
				List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(fieldDeclaration.fragments(),
						VariableDeclarationFragment.class);
				// iterate through the fragments...
				for (VariableDeclarationFragment fragment : fragments) {
					SimpleName fragmentName = fragment.getName();
					String fragmentIdentifier = fragmentName.getIdentifier();
					// check if the field name complies with the convention
					if (!NamingConventionUtil.isComplyingWithConventions(fragmentIdentifier)) {
						ITypeBinding fieldTypeBinding = fragmentName.resolveTypeBinding();
						if (fieldTypeBinding != null) {
							NamingConventionUtil.generateNewIdetifier(fragmentIdentifier)
									// should not clash with existing names
									.filter(newName -> !declaredFieldNames.contains(newName)).ifPresent(newName -> {

										BodyDeclarationsVisitor referencesVisitor = new BodyDeclarationsVisitor(
												fragmentName, parentTypeBidning, fieldTypeBinding);
										/*
										 * Find the references in the outer
										 * class. A private field of an inner
										 * class can be directly accessed from
										 * the outer class!
										 */
										ASTNode typeParent = type.getParent();
										boolean collidingWithOuterTypeField = false;
										if (typeParent != null
												&& typeParent.getNodeType() == ASTNode.TYPE_DECLARATION) {
											TypeDeclaration typeDeeclarationParent = (TypeDeclaration) typeParent;
											/*
											 * FIXME: SIM-511 - distinguish
											 * between fields of inner type from
											 * fields of outer type
											 */
											collidingWithOuterTypeField = NamingConventionUtil.hasField(typeDeeclarationParent,
													fragmentName);

											typeDeeclarationParent.accept(referencesVisitor);
										}
										// Find the references in the current
										// class.
										type.accept(referencesVisitor);
										List<String> allLocalVarNames = referencesVisitor.getLocalVarNames();

										/*
										 * It is risky to introduce a field name
										 * coinciding with a local variable name
										 */
										if (!allLocalVarNames.contains(newName) && !collidingWithOuterTypeField) {
											List<SimpleName> references = referencesVisitor.getReferences();
											declaredFieldNames.add(newName);
											// rename the declaration and all
											// the references...
											astRewrite.set(fragmentName, SimpleName.IDENTIFIER_PROPERTY, newName, null);
											references.forEach(referene -> astRewrite.set(referene,
													SimpleName.IDENTIFIER_PROPERTY, newName, null));
										}
									});
						}
					}
				}
			}
		}

		return true;
	}
}
