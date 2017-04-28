package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

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
		if (hasModifier(fieldDeclaration, modifier -> modifier.isPrivate())
				&& !(hasModifier(fieldDeclaration, modifier -> modifier.isStatic())
						&& hasModifier(fieldDeclaration, modifier -> modifier.isFinal()))) {

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
					if (!isComplyingWithConventions(fragmentIdentifier)) {
						ITypeBinding fieldTypeBinding = fragmentName.resolveTypeBinding();
						if (fieldTypeBinding != null) {
							generateNewIdetifier(fragmentIdentifier)
									// should not clash with existing names
									.filter(newName -> !declaredFieldNames.contains(newName)).ifPresent(newName -> {

										FieldReferencesASTVisitor referencesVisitor = new FieldReferencesASTVisitor(
												fragmentName, parentTypeBidning, fieldTypeBinding);
										/*
										 * Find the references in the outer
										 * class. A private field of an inner
										 * class can be directly accessed from
										 * the outer class!
										 */
										if (type.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
											type.getParent().accept(referencesVisitor);
										}
										// Find the references in the current
										// class.
										type.accept(referencesVisitor);
										List<String> allLocalVarNames = referencesVisitor.getLocalVarNames();

										/*
										 * It is risky to introduce a field name
										 * coinciding with a local variable name
										 */
										if (!allLocalVarNames.contains(newName)) {
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

	private boolean isComplyingWithConventions(String identifier) {
		// the following regex is taken from sonarqube
		return identifier.matches("^[a-z][a-zA-Z0-9]*$"); //$NON-NLS-1$
	}

	/**
	 * Converts the given string to camelCase by removing the non-alphanumeric
	 * symbols '$' and '_' and capitalizing the character which is following
	 * them, unless it is the fist character of the string. Furthermore, checks
	 * whether new string is eligible for being used as a variable name (i.e. it
	 * doesn't start with a digit and is not a java key word).
	 * <p>
	 * For instance, the following string:
	 * <p>
	 * {@code "_MY_var$name"}
	 * <p>
	 * is converted to:
	 * <p>
	 * {@code "myVarName"}
	 * <p>
	 * 
	 * @param identifier
	 *            the string to be converted
	 * @return Optional of a camel-cased string if it is a valid variable name,
	 *         or an empty optional otherwise.
	 */
	private Optional<String> generateNewIdetifier(String identifier) {
		// split by $ or by _ or by upper-case letters
		String[] parts = identifier.split("\\$|_|(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"); //$NON-NLS-1$

		List<String> partsList = Arrays.asList(parts).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());

		String newName = null;
		if (!partsList.isEmpty()) {
			// the prefix has to start with lower case
			String prefix = partsList.remove(0);
			String lowerCasePrefix = prefix.toLowerCase();

			// convert the other parts to Title case.
			String suffix = partsList.stream().filter(s -> !s.isEmpty()).map(String::toLowerCase)
					.map(input -> input.substring(0, 1).toUpperCase() + input.substring(1))
					.collect(Collectors.joining());

			// the final identifier
			String camelCasedIdenitfier = lowerCasePrefix + suffix;

			// check if it is eligible variable name
			if (!ReservedKeyWords.isKeyWord(camelCasedIdenitfier)
					&& !Character.isDigit(camelCasedIdenitfier.charAt(0))) {
				newName = camelCasedIdenitfier;
			}
		}

		return Optional.ofNullable(newName).filter(s -> !s.isEmpty());
	}

	private boolean hasModifier(FieldDeclaration field, Predicate<? super Modifier> predicate) {
		return ASTNodeUtil.convertToTypedList(field.modifiers(), Modifier.class).stream().filter(predicate).findAny()
				.isPresent();
	}
}

/**
 * List of java key words sorted by name. Taken from
 * {@linkplain https://docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html}
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class ReservedKeyWords {
	@SuppressWarnings("nls")
	static final String[] javaKeyWords = { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
			"class", "const",

			"continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",

			"for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",

			"new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",

			"switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while" };

	public static boolean isKeyWord(String keyword) {
		return (Arrays.binarySearch(javaKeyWords, keyword) >= 0);
	}
}

/**
 * A visitor for finding the references of a given field in the body of a class.
 * Does NOT go inside the inner the classes.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class FieldReferencesASTVisitor extends ASTVisitor {
	private SimpleName fieldName;
	private List<SimpleName> fieldReferences;
	private ITypeBinding parentTypeBinding;
	private int nestedTypeDeclarationLevel = 0;
	private List<String> declaredLocalVarNames;
	private ITypeBinding fieldTypeBinding;

	public FieldReferencesASTVisitor(SimpleName fieldName, ITypeBinding parentTypeBinding,
			ITypeBinding fieldTypeBinding) {

		this.fieldName = fieldName;
		this.fieldReferences = new ArrayList<>();
		this.parentTypeBinding = parentTypeBinding;
		this.declaredLocalVarNames = new ArrayList<>();
		this.fieldTypeBinding = fieldTypeBinding;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		visitReferences(fieldDeclaration);
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		visitReferences(methodDeclaration);
		return false;
	}

	@Override
	public boolean visit(Initializer initializer) {
		visitReferences(initializer);
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		nestedTypeDeclarationLevel++;
		visitReferences(typeDeclaration);

		return nestedTypeDeclarationLevel == 1;
	}

	public void endVisit(TypeDeclaration typeDeclaration) {
		nestedTypeDeclarationLevel--;
	}

	@Override
	public boolean visit(EnumDeclaration typeDeclaration) {
		visitReferences(typeDeclaration);
		return false;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration typeDeclaration) {
		visitReferences(typeDeclaration);
		return false;
	}

	/**
	 * Gathers the local variable names in the given bodyDeclaration. Then,
	 * finds the references of the {@link FieldReferencesASTVisitor#fieldName}.
	 * 
	 * @param bodyDeclaration
	 *            an node representing a {@link BodyDeclaration}.
	 */
	private void visitReferences(BodyDeclaration bodyDeclaration) {

		VariableDeclarationsVisitor declarationsVisitor = new VariableDeclarationsVisitor();
		bodyDeclaration.accept(declarationsVisitor);
		List<SimpleName> localDeclarations = declarationsVisitor.getVariableDeclarationNames();
		List<String> localDeclarationNames = localDeclarations.stream().map(SimpleName::getIdentifier)
				.collect(Collectors.toList());
		ReferencesInBlockVisitor visitor = new ReferencesInBlockVisitor(fieldName, parentTypeBinding, fieldTypeBinding,
				localDeclarationNames);
		bodyDeclaration.accept(visitor);
		fieldReferences.addAll(visitor.getReferences());
		declaredLocalVarNames.addAll(localDeclarationNames);
	}

	public List<SimpleName> getReferences() {
		return fieldReferences;
	}

	public List<String> getLocalVarNames() {
		return declaredLocalVarNames;
	}
}

/**
 * Finds the references of the given field in a block. Requires the names of the
 * local variables to be provided. Distinguishes between local variables and
 * fields with the same name.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class ReferencesInBlockVisitor extends ASTVisitor {
	private String targetNameIdentifier;
	private ITypeBinding targetTypeBinding;
	private List<String> declaredLocalVarName;
	private List<SimpleName> references;
	private ITypeBinding parentTypeBinding;

	public ReferencesInBlockVisitor(SimpleName targetNode, ITypeBinding parentTypeBinding,
			ITypeBinding targetTypeBinding, List<String> declaredLocalVarNames) {
		this.targetNameIdentifier = targetNode.getIdentifier();
		this.declaredLocalVarName = declaredLocalVarNames;
		this.references = new ArrayList<>();
		this.parentTypeBinding = parentTypeBinding;
		this.targetTypeBinding = targetTypeBinding;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		String identifier = simpleName.getIdentifier();
		IBinding resolvedBinding = simpleName.resolveBinding();
		if (resolvedBinding != null && resolvedBinding.getKind() == IBinding.VARIABLE) {

			if (!simpleName.isDeclaration() && identifier.equals(targetNameIdentifier)
					&& ClassRelationUtil.compareITypeBinding(simpleName.resolveTypeBinding().getErasure(),
							targetTypeBinding.getErasure())) {
				ASTNode parent = simpleName.getParent();
				boolean isReference = false;

				if (parent.getNodeType() == ASTNode.QUALIFIED_NAME) {
					// the simpleName is part of a qualified name
					QualifiedName qualifiedName = ((QualifiedName) parent);
					if (simpleName == qualifiedName.getName()) {
						// if the simpleName stands at the tail of the
						// qualifedName
						ITypeBinding qualifierTypeBinding = ((QualifiedName) parent).getQualifier()
								.resolveTypeBinding();
						if (ClassRelationUtil.compareITypeBinding(parentTypeBinding, qualifierTypeBinding)) {
							isReference = true;
						}
					} else if (!declaredLocalVarName.contains(identifier)) {
						// the simpleName is the qualifier itself
						isReference = true;
					}

				} else if (parent.getNodeType() == ASTNode.FIELD_ACCESS) {
					// a 'field access' is an expression of the form:
					// this.[field_name]
					ITypeBinding expressionTypeBidnign = ((FieldAccess) parent).getExpression().resolveTypeBinding();
					if (ClassRelationUtil.compareITypeBinding(parentTypeBinding, expressionTypeBidnign)) {
						isReference = true;
					}
				} else if (!declaredLocalVarName.contains(identifier)) {
					/*
					 * If not a field access and not a qualified name, then the
					 * simpleName which is not a local variable, is field
					 * access.
					 */
					isReference = true;
				}

				if (isReference) {
					references.add(simpleName);
				}
			}
		}
		return true;
	}

	public List<SimpleName> getReferences() {
		return references;
	}
}

/**
 * Gathers the names of the declared variables.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class VariableDeclarationsVisitor extends ASTVisitor {
	private List<SimpleName> variableDelcarations;

	public VariableDeclarationsVisitor() {
		variableDelcarations = new ArrayList<>();
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding resolvedBinding = simpleName.resolveBinding();
		if (resolvedBinding != null && resolvedBinding.getKind() == IBinding.VARIABLE && simpleName.isDeclaration()) {
			variableDelcarations.add(simpleName);
		}
		return true;
	}

	public List<SimpleName> getVariableDeclarationNames() {
		return variableDelcarations;
	}
}
