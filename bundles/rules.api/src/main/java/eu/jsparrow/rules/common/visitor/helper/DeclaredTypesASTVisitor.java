package eu.jsparrow.rules.common.visitor.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * A visitor for collecting the types declared in a compilation unit, including
 * the inner-types.
 * 
 * @author Ardit Ymeri
 * @since 2.0
 *
 */
public class DeclaredTypesASTVisitor extends ASTVisitor {

	private Map<String, List<ITypeBinding>> typesMap;
	private List<ITypeBinding> allTypes;

	public DeclaredTypesASTVisitor() {
		typesMap = new HashMap<>();
		allTypes = new ArrayList<>();
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		storeDeclaredTypes(typeDeclaration);
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration typeDeclaration) {
		storeDeclaredTypes(typeDeclaration);
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration typeDeclaration) {
		storeDeclaredTypes(typeDeclaration);
		return true;
	}

	private void storeDeclaredTypes(AbstractTypeDeclaration typeDeclaration) {
		ITypeBinding binding = typeDeclaration.resolveBinding();
		allTypes.add(binding);
		List<ITypeBinding> innerTypes = Arrays.asList(binding.getDeclaredTypes());
		typesMap.put(binding.getQualifiedName(), innerTypes);
	}

	/**
	 * Returns a map having the qualified names of the declared types as keys,
	 * and a list of the type-bindings of the inner-types as values.
	 * 
	 * @return a map containing all of the declared types.
	 */
	public Map<String, List<ITypeBinding>> getDeclaredTypes() {
		return this.typesMap;
	}

	/**
	 * @return the list of the type bindings top level types.
	 */
	public List<ITypeBinding> getTopLevelTypes() {
		return allTypes.stream()
			.filter(ITypeBinding::isTopLevel)
			.collect(Collectors.toList());
	}

	/**
	 * 
	 * @return a list containing all type bindings, including inner types and
	 *         also local types.
	 */
	public List<ITypeBinding> getAllTypes() {
		return allTypes;
	}

}
