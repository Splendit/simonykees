package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Keeps the names of the variables declared in a scope.
 * 
 * @author Ardit Ymeri
 * @since 2.5
 *
 */
public class LiveVariableScope {
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
	 *         optional otherwise
	 */
	public Optional<ASTNode> findEnclosingScope(ClassInstanceCreation node) {
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
	 * Populates {@link #localVariableNames} and {@link #fieldNames} with the
	 * variable declarations occurring in the given scope. The scope node is
	 * used as a key in both cases. Nothing happens if the maps already contain
	 * the scope key. If the scope represents a {@link TypeDeclaration} then
	 * only the declared fields are considered. Otherwise, the
	 * {@link VariableDeclarationsVisitor} visitor is used to find all variables
	 * declared inside the scope.
	 * 
	 * @param scope
	 *            the node to check (if necessary) for variable declaration.
	 */
	public void lazyLoadScopeNames(ASTNode scope) {

		if (localVariableNames.containsKey(scope)) {
			return;
		}

		List<String> declaredInScope;
		if (ASTNode.TYPE_DECLARATION == scope.getNodeType()) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) scope;
			declaredInScope = ASTNodeUtil.findFieldNames(typeDeclaration);

		} else {
			VariableDeclarationsVisitor declarationsVisitor = new VariableDeclarationsVisitor();
			scope.accept(declarationsVisitor);
			declaredInScope = declarationsVisitor.getVariableDeclarationNames()
				.stream()
				.map(SimpleName::getIdentifier)
				.collect(Collectors.toList());
		}
		this.localVariableNames.put(scope, declaredInScope);

		if (TypeDeclaration.BODY_DECLARATIONS_PROPERTY != scope.getLocationInParent()) {
			return;
		}

		TypeDeclaration typeDeclaration = (TypeDeclaration) scope.getParent();
		if (fieldNames.containsKey(typeDeclaration)) {
			return;
		}

		List<String> names = ASTNodeUtil.findFieldNames(typeDeclaration);
		fieldNames.put(typeDeclaration, names);
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
		return fieldNames.values()
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
	 * @param calendarName
	 *            value
	 */
	public void storeIntroducedName(ASTNode scope, String calendarName) {
		List<String> storedLocalNames = localVariableNames.get(scope);
		if (storedLocalNames == null) {
			storedLocalNames = new ArrayList<>();
		}
		storedLocalNames.add(calendarName);
		localVariableNames.put(scope, storedLocalNames);
	}

	public void clearLocalVariablesScope(ASTNode scope) {
		localVariableNames.remove(scope);
	}

	public void clearFieldScope(AbstractTypeDeclaration node) {
		fieldNames.remove(node);
	}

}
