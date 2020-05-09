package eu.jsparrow.core.visitor.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.visitor.loop.DeclaredTypesASTVisitor;
import eu.jsparrow.core.visitor.sub.VariableDeclarationsVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Proposal for using instead of LiveVariableScope. Stores all simple names of
 * <br>
 * type declarations, <br>
 * field declarations, <br>
 * and local variable declarations <br>
 * which are visible in a given scope and all simple names from the import
 * declarations of the corresponding compilation unit.
 * <p>
 * The intention is to prevent introducing variables which may clash with
 * imports or with already existing declarations of variables, fields and types.
 *
 */
public class UniqueSimpleNamesInScope {

	private Map<CompilationUnit, Set<String>> simpleNamesFromImports = new HashMap<>();
	private Map<AbstractTypeDeclaration, Set<String>> simpleNamesOfFieldsAndTypes = new HashMap<>();
	private Map<ASTNode, Set<String>> localDeclarationNames = new HashMap<>();

	public void loadSimpleNamesFromImports(CompilationUnit compilationUnit) {
		if (simpleNamesFromImports.containsKey(compilationUnit)) {
			return;
		}
		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);

		Set<String> currentNames = new HashSet<>();
		importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(name -> (QualifiedName) name)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.forEach(currentNames::add);

		importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isSimpleName)
			.map(name -> (SimpleName) name)
			.map(SimpleName::getIdentifier)
			.forEach(currentNames::add);

		simpleNamesFromImports.put(compilationUnit, currentNames);
	}

	public void loadSimpleNamesOfFieldsAndTypes(AbstractTypeDeclaration typeDeclaration) {
		if (simpleNamesOfFieldsAndTypes.containsKey(typeDeclaration)) {
			return;
		}
		Set<String> currentNames = new HashSet<>();
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		while (typeBinding != null) {
			currentNames.add(typeBinding.getName());
			Arrays.asList(typeBinding.getDeclaredFields())
				.stream()
				.map(IVariableBinding::getName)
				.forEach(currentNames::add);
			
			// probably not necessary to store the declared types
			Arrays.asList(typeBinding.getDeclaredTypes())
				.stream()
				.map(ITypeBinding::getName)
				.forEach(currentNames::add);
			typeBinding = typeBinding.getDeclaringClass();
		}
		simpleNamesOfFieldsAndTypes.put(typeDeclaration, currentNames);
	}

	public void loadLocalDeclarationNames(ASTNode localScope) {
		if (localDeclarationNames.containsKey(localScope)) {
			return;
		}
		Set<String> currentNames = new HashSet<>();
		VariableDeclarationsVisitor declarationsVisitor = new VariableDeclarationsVisitor();
		localScope.accept(declarationsVisitor);
		declarationsVisitor.getVariableDeclarationNames()
			.stream()
			.map(SimpleName::getIdentifier)
			.forEach(currentNames::add);

		DeclaredTypesASTVisitor visitor = new DeclaredTypesASTVisitor();
		localScope.accept(visitor);
		visitor.getAllTypes()
			.stream()
			.map(ITypeBinding::getName)
			.forEach(currentNames::add);

		localDeclarationNames.put(localScope, currentNames);
	}

	/**
	 * Guarantees that all simple names which exist in the scope are available.
	 * 
	 * @param localScope
	 *            expected to be either </br>
	 *            a method declaration or </br>
	 *            an initializer or </br>
	 *            a field declaration.
	 */
	private void lazyLoadForLocalScope(ASTNode localScope) {
		loadLocalDeclarationNames(localScope);
		loadSimpleNamesOfFieldsAndTypes(ASTNodeUtil.getSpecificAncestor(localScope, AbstractTypeDeclaration.class));
		loadSimpleNamesFromImports(ASTNodeUtil.getSpecificAncestor(localScope, CompilationUnit.class));

	}

	/**
	 * 
	 * @param localScope
	 *            expected to be either </br>
	 *            a method declaration or </br>
	 *            an initializer or </br>
	 *            a field declaration.
	 * @param simpleName
	 */
	public boolean findSimpleName(ASTNode localScope, String simpleName) {
		lazyLoadForLocalScope(localScope);
		if (localDeclarationNames.get(localScope)
			.contains(simpleName)) {
			return true;
		}
		AbstractTypeDeclaration currentTypeDeclaration = ASTNodeUtil.getSpecificAncestor(localScope,
				AbstractTypeDeclaration.class);
		if (simpleNamesOfFieldsAndTypes.get(currentTypeDeclaration)
			.contains(simpleName)) {
			return true;
		}
		CompilationUnit currentCompilationUnit = ASTNodeUtil.getSpecificAncestor(localScope, CompilationUnit.class);
		return simpleNamesFromImports.get(currentCompilationUnit)
			.contains(simpleName);
	}

	public void storeLocalVariableName(ASTNode localScope, String simpleName) {
		lazyLoadForLocalScope(localScope);
		localDeclarationNames.get(localScope)
			.add(simpleName);

	}

	public void removeNamesOfFieldsAndTypes(AbstractTypeDeclaration typeDeclaration) {
		simpleNamesOfFieldsAndTypes.remove(typeDeclaration);
	}

	public void removeNamesFromImports(CompilationUnit compilationUnit) {
		simpleNamesFromImports.remove(compilationUnit);
	}

	public void renameLocalNames(ASTNode typeDeclaration) {
		localDeclarationNames.remove(typeDeclaration);
	}

}
