package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
 * 
 * @author Ardit Ymeri
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
		// check if it is not a final field
		if (isPrivate(fieldDeclaration) && !isFinal(fieldDeclaration)) {
			ASTNode parent = fieldDeclaration.getParent();
			if (parent != null && parent.getNodeType() == ASTNode.TYPE_DECLARATION) {
				TypeDeclaration type = (TypeDeclaration) parent;
				ITypeBinding parentTypeBidning = type.resolveBinding();
				List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(fieldDeclaration.fragments(),
						VariableDeclarationFragment.class);
				for (VariableDeclarationFragment fragment : fragments) {
					SimpleName fragmentName = fragment.getName();
					String fragmentIdentifier = fragmentName.getIdentifier();
					if (!isComplyingWithConventionsIdentifier(fragmentIdentifier)) {

						generateNewIdetifier(fragmentIdentifier)
								.filter(newName -> !declaredFieldNames.contains(newName)).ifPresent(newName -> {
									FieldReferencesASTVisitor referencesVisitor = new FieldReferencesASTVisitor(
											fragmentName, parentTypeBidning);
									type.accept(referencesVisitor);
									List<String> allLocalVarNames = referencesVisitor.getLocalVarNames();
									if(!allLocalVarNames.contains(newName)) {
										List<SimpleName> references = referencesVisitor.getReferences();
										declaredFieldNames.add(newName);
										astRewrite.set(fragmentName, SimpleName.IDENTIFIER_PROPERTY, newName, null);
										references.forEach(referene -> astRewrite.set(referene,
												SimpleName.IDENTIFIER_PROPERTY, newName, null));
									}
								});
					}
				}
			}
		}

		return true;
	}

	private boolean isComplyingWithConventionsIdentifier(String identifier) {
		// the regex is taken from sonarqube
		return identifier.matches("^[a-z][a-zA-Z0-9]*$"); //$NON-NLS-1$
	}

	private Optional<String> generateNewIdetifier(String identifier) {
		String[] parts = identifier.split("\\$|_|(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"); //$NON-NLS-1$

		List<String> partsList = Arrays.asList(parts).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());

		String newName = null;
		if (!partsList.isEmpty()) {
			String prefix = partsList.remove(0);

			String cammelCasePrefix = prefix.toLowerCase();

			String suffix = partsList.stream().filter(s -> !s.isEmpty()).map(String::toLowerCase)
					.map(input -> input.substring(0, 1).toUpperCase() + input.substring(1))
					.collect(Collectors.joining());

			String camelCasedIdenitfier = cammelCasePrefix + suffix;

			if (!ReservedKeyWords.isKeyWord(camelCasedIdenitfier)
					&& !Character.isDigit(camelCasedIdenitfier.charAt(0))) {
				newName = camelCasedIdenitfier;
			}
		}

		return Optional.ofNullable(newName).filter(s -> !s.isEmpty());
	}

	private boolean isPrivate(FieldDeclaration field) {

		return ASTNodeUtil.convertToTypedList(field.modifiers(), Modifier.class).stream()
				.filter(modifier -> modifier.isPrivate()).findAny().isPresent();
	}
	
	private boolean isFinal(FieldDeclaration field) {

		return ASTNodeUtil.convertToTypedList(field.modifiers(), Modifier.class).stream()
				.filter(modifier -> modifier.isFinal()).findAny().isPresent();
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

class FieldReferencesASTVisitor extends ASTVisitor {
	private String fieldNameIdentifier;
	private List<SimpleName> fieldReferences;
	private ITypeBinding parentTypeBinding;
	private int nestedTypeDeclarationLevel = 0;
	private List<String> declaredLocalVarNames;

	public FieldReferencesASTVisitor(SimpleName fieldName, ITypeBinding parentTypeBinding) {
		this.fieldNameIdentifier = fieldName.getIdentifier();
		this.fieldReferences = new ArrayList<>();
		this.parentTypeBinding = parentTypeBinding;
		this.declaredLocalVarNames = new ArrayList<>();
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

	private void visitReferences(BodyDeclaration bodyDeclaration) {
		ReferencesVisitor visitor = new ReferencesVisitor(fieldNameIdentifier, parentTypeBinding);
		bodyDeclaration.accept(visitor);
		fieldReferences.addAll(visitor.getReferences());
		declaredLocalVarNames.addAll(visitor.getDeclaredLocalVarNames());
	}

	public List<SimpleName> getReferences() {
		return fieldReferences;
	}
	
	public List<String> getLocalVarNames() {
		return declaredLocalVarNames;
	}
}

class ReferencesVisitor extends ASTVisitor { // FIXME: rename this visitor
	private String targetNameIdentifier;
	private List<String> declaredLocalVarName;
	private List<SimpleName> references;
	private ITypeBinding parentTypeBinding;

	public ReferencesVisitor(String targetIdentifier, ITypeBinding parentTypeBinding) {
		this.targetNameIdentifier = targetIdentifier;
		declaredLocalVarName = new ArrayList<>();
		references = new ArrayList<>();
		this.parentTypeBinding = parentTypeBinding;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		String identifier = simpleName.getIdentifier();
		IBinding resolvedBinding = simpleName.resolveBinding();
		if (resolvedBinding.getKind() == IBinding.VARIABLE) {
			if (simpleName.isDeclaration()) {
				declaredLocalVarName.add(identifier);
			} else if (identifier.equals(targetNameIdentifier)) {
				ASTNode parent = simpleName.getParent();
				boolean isReference = false;

				if (parent.getNodeType() == ASTNode.QUALIFIED_NAME) {
					ITypeBinding qualifierTypeBinding = ((QualifiedName) parent).getQualifier().resolveTypeBinding();
					if (ClassRelationUtil.compareITypeBinding(parentTypeBinding, qualifierTypeBinding)) {
						isReference = true;
					}
				} else if (parent.getNodeType() == ASTNode.FIELD_ACCESS) {
					ITypeBinding expressionTypeBidnign = ((FieldAccess) parent).getExpression().resolveTypeBinding();
					if (ClassRelationUtil.compareITypeBinding(parentTypeBinding, expressionTypeBidnign)) {
						isReference = true;
					}
				} else if (!declaredLocalVarName.contains(identifier)) {
					isReference = true;
				}

				if (isReference) {
					references.add(simpleName);
				}
			}
		}
		return false;
	}

	public List<SimpleName> getReferences() {
		return references;
	}
	
	public List<String> getDeclaredLocalVarNames() {
		return declaredLocalVarName;
	}
}
