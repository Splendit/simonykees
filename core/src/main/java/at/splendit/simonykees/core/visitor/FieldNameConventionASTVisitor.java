package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import at.splendit.simonykees.core.util.ASTNodeUtil;

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
		
		//TODO: nested types should be considered
		declaredFieldNames = fields.stream()
				.flatMap(fieldDecl -> ASTNodeUtil
						.convertToTypedList(fieldDecl.fragments(), VariableDeclarationFragment.class).stream())
				.map(VariableDeclarationFragment::getName).map(SimpleName::getIdentifier).collect(Collectors.toList());

		return true;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		// check if it is not a final field
		// check if the parent is an interface
		ASTNode parent = fieldDeclaration.getParent();
		if(parent!= null && parent.getNodeType() == ASTNode.TYPE_DECLARATION) {
			TypeDeclaration type = (TypeDeclaration) parent;
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(fieldDeclaration.fragments(),
					VariableDeclarationFragment.class);
			for (VariableDeclarationFragment fragment : fragments) {
				SimpleName fragmentName = fragment.getName();
				String fragmentIdentifier = fragmentName.getIdentifier();
				if (!isComplyingWithConventionsIdentifier(fragmentIdentifier)) {

					// check if newName is not the identifier of an existing field
					// in the current class or in any parent
					// if not, rename the field

					// this does not search for all references of the field!!! only
					// for temporarily testing
					generateNewIdetifier(fragmentIdentifier).filter(newName -> !declaredFieldNames.contains(newName))
							.ifPresent(newName -> {
								FieldReferencesASTVisitor referencesVisitor = new FieldReferencesASTVisitor(fragmentName);
								type.accept(referencesVisitor);
								List<SimpleName> references = referencesVisitor.getReferences();
								declaredFieldNames.add(newName);
								astRewrite.set(fragmentName, SimpleName.IDENTIFIER_PROPERTY, newName, null);
								references.forEach(referene -> astRewrite.set(referene, SimpleName.IDENTIFIER_PROPERTY, newName, null));
							});
				}

			}
		}

		return true;
	}

	private boolean isComplyingWithConventionsIdentifier(String identifier) {
		// the regex is taken from sonarqube
		return identifier.matches("^[a-z][a-zA-Z0-9]*$");
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
}

/**
 * List of java key words taken from
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
	
	public FieldReferencesASTVisitor(SimpleName fieldName) {
		this.fieldNameIdentifier = fieldName.getIdentifier();
		fieldReferences = new ArrayList<>();
	}
	
	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		ReferencesVisitor visitor = new ReferencesVisitor(fieldNameIdentifier);
		methodDeclaration.accept(visitor);
		fieldReferences.addAll(visitor.getReferences());
		return true;
	}
	
	public List<SimpleName> getReferences() {
		return fieldReferences;
	}
}

class ReferencesVisitor extends ASTVisitor {
	private String targetNameIdentifier;
	private List<String> declaredLocalVarName;
	private List<SimpleName> references;
	
	public ReferencesVisitor(String targetIdentifier) {
		this.targetNameIdentifier = targetIdentifier;
		declaredLocalVarName = new ArrayList<>();
		references = new ArrayList<>();
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		String identifier = simpleName.getIdentifier();
		IBinding resolvedBinding = simpleName.resolveBinding();
		if(resolvedBinding.getKind() == IBinding.VARIABLE) {
			if(simpleName.isDeclaration()) {
				declaredLocalVarName.add(identifier);
			} else if (identifier.equals(targetNameIdentifier)){				
				ASTNode parent = simpleName.getParent();
				if(parent.getNodeType() == ASTNode.FIELD_ACCESS || !declaredLocalVarName.contains(identifier)) {
					references.add(simpleName);
				}
			}
		}
		return false;
	}
	
	public List<SimpleName> getReferences() {
		return references;
	}
}
