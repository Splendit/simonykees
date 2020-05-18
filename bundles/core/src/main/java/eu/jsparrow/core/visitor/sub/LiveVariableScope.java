package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Stores the names of local variables and fields declared in a scope and,
 * additionally, the names of imported static fields.
 * 
 * @author Ardit Ymeri
 * @since 2.5
 *
 */
public class LiveVariableScope {
	private Map<CompilationUnit, List<String>> importedStaticFieldNames = new HashMap<>();
	private Map<AbstractTypeDeclaration, List<String>> fieldNames = new HashMap<>();
	private Map<ASTNode, List<String>> localVariableNames = new HashMap<>();

	/**
	 * The scope is either of the:
	 * 
	 * <ul>
	 * <li>enclosing {@link Initializer}</li>
	 * <li>enclosing {@link MethodDeclaration}</li>
	 * <li>enclosing {@link TypeDeclaration}</li>
	 * </ul>
	 * 
	 * @param node
	 *            a node to check the scope for.
	 * @return an optional of any of the nodes mentioned above, or an empty
	 *         optional otherwise.
	 */
	public Optional<ASTNode> findEnclosingScope(ASTNode node) {
		Initializer initalizer = ASTNodeUtil.getSpecificAncestor(node, Initializer.class);
		if (initalizer != null) {
			return Optional.of(initalizer);
		}

		MethodDeclaration methodDeclaration = ASTNodeUtil.getSpecificAncestor(node, MethodDeclaration.class);
		if (methodDeclaration != null) {
			return Optional.of(methodDeclaration);
		}

		FieldDeclaration fieldDeclaration = ASTNodeUtil.getSpecificAncestor(node, FieldDeclaration.class);
		if (fieldDeclaration != null) {
			return Optional.of(fieldDeclaration.getParent());
		}

		return Optional.empty();
	}

	/**
	 * Populates the following maps:
	 * <ul>
	 * <li>{@link #localVariableNames} using the local scope as key</li>
	 * <li>{@link #fieldNames} using the current type as key, and, if the type
	 * is an inner type, each surrounding type will be an additional key</li>
	 * <li>{@link #importedStaticFieldNames} using the current compilation unit
	 * as key</li>
	 * </ul>
	 * 
	 * Nothing happens if the maps already contains the scope key. If the scope
	 * represents a {@link TypeDeclaration} then only the declared fields are
	 * considered. Otherwise, the {@link VariableDeclarationsVisitor} visitor is
	 * used to find all variables declared inside the scope.
	 * 
	 * @param scope
	 *            the node to check (if necessary) for variable declaration.
	 *            Expected to be an instance of one of the following classes:
	 *            <ul>
	 *            <li>{@link Initializer}</li>
	 *            <li>{@link MethodDeclaration}</li>
	 *            <li>{@link TypeDeclaration}</li>
	 *            </ul>
	 */
	public void lazyLoadScopeNames(ASTNode scope) {

		if (ASTNode.TYPE_DECLARATION == scope.getNodeType()) {
			loadFieldNames((TypeDeclaration) scope);
		} else {
			if (localVariableNames.containsKey(scope)) {
				return;
			}
			List<String> declaredInScope;
			SimpleNamesAsVariableOrQualifierVisitor declarationsVisitor = new SimpleNamesAsVariableOrQualifierVisitor();
			scope.accept(declarationsVisitor);
			declaredInScope = declarationsVisitor.getVariableDeclarationNames()
				.stream()
				.map(SimpleName::getIdentifier)
				.collect(Collectors.toList());

			this.localVariableNames.put(scope, declaredInScope);

			if (TypeDeclaration.BODY_DECLARATIONS_PROPERTY != scope.getLocationInParent()) {
				return;
			}
			loadFieldNames((TypeDeclaration) scope.getParent());
		}
	}

	private void loadFieldNames(TypeDeclaration typeDeclaration) {
		if (fieldNames.containsKey(typeDeclaration)) {
			return;
		}
		List<String> names = ASTNodeUtil.findFieldNames(typeDeclaration);
		fieldNames.put(typeDeclaration, names);
		TypeDeclaration enclosingType = ASTNodeUtil.getSpecificAncestor(typeDeclaration, TypeDeclaration.class);
		if (enclosingType != null) {
			loadFieldNames(enclosingType);
		} else {
			loadImportedStarticFieldNames(ASTNodeUtil.getSpecificAncestor(typeDeclaration, CompilationUnit.class));
		}
	}

	private void loadImportedStarticFieldNames(CompilationUnit compilationUnit) {
		if (importedStaticFieldNames.containsKey(compilationUnit)) {
			return;
		}

		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);

		List<String> importedStaticFieldNamesList = new ArrayList<>();

		importDeclarations.stream()
			.filter(ImportDeclaration::isStatic)
			.map(ImportDeclaration::resolveBinding)
			.filter(Objects::nonNull)
			.filter(binding -> binding.getKind() == IBinding.VARIABLE)
			.map(binding -> (IVariableBinding) binding)
			.map(IVariableBinding::getName)
			.forEach(importedStaticFieldNamesList::add);

		importedStaticFieldNames.put(compilationUnit, importedStaticFieldNamesList);
	}

	/**
	 * Checks whether the given identifier matches the values in
	 * {@link #fieldNames} or {@link #localVariableNames}.
	 * 
	 * @param name
	 *            identifier to be checked
	 * @return if a match is found.
	 */
	public boolean isInScope(String name) {
		return importedStaticFieldNames.values()
			.stream()
			.anyMatch(names -> names.contains(name)) ||
				fieldNames.values()
					.stream()
					.anyMatch(names -> names.contains(name))
				|| localVariableNames.values()
					.stream()
					.anyMatch(names -> names.contains(name));
	}

	/**
	 * Adds the given variable name in the {@link #localVariableNames}
	 * 
	 * @param scope
	 *            key
	 * 
	 * @param name
	 *            value
	 */
	public void addName(ASTNode scope, String name) {
		if (ASTNode.TYPE_DECLARATION == scope.getNodeType()) {
			List<String> storedFieldNames = fieldNames.get(scope);
			if (storedFieldNames == null) {
				storedFieldNames = new ArrayList<>();
			}
			storedFieldNames.add(name);
			fieldNames.put((TypeDeclaration) scope, storedFieldNames);
		} else {
			List<String> storedLocalNames = localVariableNames.get(scope);
			if (storedLocalNames == null) {
				storedLocalNames = new ArrayList<>();
			}
			storedLocalNames.add(name);
			localVariableNames.put(scope, storedLocalNames);
		}
	}

	public void clearLocalVariablesScope(ASTNode scope) {
		localVariableNames.remove(scope);
	}

	public void clearFieldScope(AbstractTypeDeclaration node) {
		fieldNames.remove(node);
	}

	public void clearCompilationUnitScope(CompilationUnit node) {
		importedStaticFieldNames.remove(node);
		fieldNames.clear();
		localVariableNames.clear();
	}
}
