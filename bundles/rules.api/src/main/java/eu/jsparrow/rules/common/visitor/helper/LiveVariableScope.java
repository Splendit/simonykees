package eu.jsparrow.rules.common.visitor.helper;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.getSpecificAncestor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
		Initializer initalizer = getSpecificAncestor(node, Initializer.class);
		if (initalizer != null) {
			return Optional.of(initalizer);
		}

		MethodDeclaration methodDeclaration = getSpecificAncestor(node, MethodDeclaration.class);
		if (methodDeclaration != null) {
			return Optional.of(methodDeclaration);
		}

		FieldDeclaration fieldDeclaration = getSpecificAncestor(node, FieldDeclaration.class);
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

		loadImportedStaticFieldNames(getSpecificAncestor(scope, CompilationUnit.class));

		if (ASTNode.TYPE_DECLARATION == scope.getNodeType()) {
			Map<AbstractTypeDeclaration, List<String>> fields = findFieldNames((TypeDeclaration) scope);
			this.fieldNames.putAll(fields);
			return;
		}

		if (localVariableNames.containsKey(scope)) {
			return;
		}
		List<String> declaredInScope = new ArrayList<>();
		VariableDeclarationsVisitor declarationsVisitor = new VariableDeclarationsVisitor();
		scope.accept(declarationsVisitor);
		declarationsVisitor.getVariableDeclarationNames()
			.stream()
			.map(SimpleName::getIdentifier)
			.forEach(declaredInScope::add);
		SimpleNamesAsQualifierVisitor simpleNamesAsQualifierVisitor = new SimpleNamesAsQualifierVisitor();
		scope.accept(simpleNamesAsQualifierVisitor);
		simpleNamesAsQualifierVisitor.getVariableDeclarationNames()
			.stream()
			.map(SimpleName::getIdentifier)
			.forEach(declaredInScope::add);

		this.localVariableNames.put(scope, declaredInScope);
		Map<AbstractTypeDeclaration, List<String>> fields = findFieldNames(
				getSpecificAncestor(scope, TypeDeclaration.class));
		this.fieldNames.putAll(fields);

	}

	private Map<AbstractTypeDeclaration, List<String>> findFieldNames(TypeDeclaration typeDeclaration) {
		Map<AbstractTypeDeclaration, List<String>> map = new HashMap<>();
		if (typeDeclaration == null || fieldNames.containsKey(typeDeclaration)) {
			return Collections.emptyMap();
		}
		List<String> names = ASTNodeUtil.findFieldNames(typeDeclaration);
		map.put(typeDeclaration, names);
		Map<AbstractTypeDeclaration, List<String>> outerFieldNames = findFieldNames(
				getSpecificAncestor(typeDeclaration, TypeDeclaration.class));
		map.putAll(outerFieldNames);
		return map;
	}

	private void loadImportedStaticFieldNames(CompilationUnit compilationUnit) {
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
	 * {@link #fieldNames}, {@link #localVariableNames}, or
	 * {@link #importedStaticFieldNames}.
	 * 
	 * @param name
	 *            identifier to be checked
	 * @return if a match is found.
	 */
	public boolean isInScope(String name) {
		boolean matchesStaticImport = importedStaticFieldNames.values()
			.stream()
			.anyMatch(names -> names.contains(name));
		if (matchesStaticImport) {
			return true;
		}

		boolean matchesField = fieldNames.values()
			.stream()
			.anyMatch(names -> names.contains(name));
		if (matchesField) {
			return true;
		}

		return localVariableNames.values()
			.stream()
			.anyMatch(names -> names.contains(name));
	}

	/**
	 * Checks whether the given identifier matches any of the values stored for
	 * the given scope {@link #fieldNames}, {@link #localVariableNames}, or
	 * {@link #importedStaticFieldNames}.
	 * 
	 * @param name
	 *            name to search for
	 * @param scope
	 *            the scope to search in
	 * @return if a value is found
	 */
	public boolean isInScope(String name, ASTNode scope) {
		List<String> values = importedStaticFieldNames.getOrDefault(scope, Collections.emptyList());
		boolean matchesStaticImport = values.contains(name);
		if (matchesStaticImport) {
			return true;
		}

		boolean matchesField = fieldNames.getOrDefault(scope, Collections.emptyList())
			.stream()
			.anyMatch(name::equals);
		if (matchesField) {
			return true;
		}

		boolean matchesLocalVariable = localVariableNames.getOrDefault(scope, Collections.emptyList())
			.stream()
			.anyMatch(name::equals);
		if (matchesLocalVariable) {
			return true;
		}

		ASTNode parent = scope.getParent();
		if (parent != null) {
			/*
			 * Field names and outer class fields are stored in parent enclosing
			 * scopes.
			 */
			return isInScope(name, parent);
		}
		return false;
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
