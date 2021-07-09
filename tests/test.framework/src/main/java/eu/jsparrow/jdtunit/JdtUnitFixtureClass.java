package eu.jsparrow.jdtunit;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.jdtunit.util.CompilationUnitBuilder;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * <p>
 * Fixture class that stubs a JDT compilation unit. Within that compilation unit
 * ASTNodes can be inserted and deleted.
 * 
 * In order to get working type bindings for any AST created within the stubbed
 * compilation unit a full java project is created in code. This is done in
 * {@link JdtUnitFixtureProject}.
 * 
 * {@link JdtUnitFixtureClass} can only be created using the
 * {@link JdtUnitFixtureProject#addCompilationUnit()} method.
 * </p>
 * 
 * @author Hans-Jörg Schrödl
 *
 */
@SuppressWarnings({ "unchecked" })
public class JdtUnitFixtureClass {

	private static final String DEFAULT_METHOD_FIXTURE_NAME = "FixtureMethod";

	private JdtUnitFixtureProject fixtureProject;
	private ICompilationUnit compilationUnit;
	private IPackageFragment packageFragment;

	private CompilationUnit astRoot;
	private AST ast;
	private ASTRewrite astRewrite;

	private String className;
	private TypeDeclaration typeDeclaration;

	private HashMap<String, MethodDeclaration> methods = new HashMap<>();

	private boolean hasChanged = false;

	JdtUnitFixtureClass(JdtUnitFixtureProject fixtureProject, IPackageFragment packageFragment, String className)
			throws JdtUnitException {
		this.packageFragment = packageFragment;
		this.className = className;
		this.fixtureProject = fixtureProject;

		createCompilationUnit();
	}

	/**
	 * Adds a normal single import statement to the stub file.
	 * 
	 * @param name
	 *            the import as fully qualified string, e.g. at.splendit.MyClass
	 * @throws BadLocationException
	 * @throws JavaModelException
	 * @throws Exception
	 */
	public void addImport(String name) throws JavaModelException, BadLocationException {
		addImport(name, false, false);
	}

	/**
	 * Adds an import statement to the stub file.
	 * 
	 * @param name
	 *            the import as fully qualified string, e.g. at.splendit.MyClass
	 * @param isStatic
	 *            whether this import is a static import
	 * @param isOnDemand
	 *            whether this import is an on demand import
	 * @throws JavaModelException
	 * @throws BadLocationException
	 */
	public void addImport(String name, boolean isStatic, boolean isOnDemand)
			throws JavaModelException, BadLocationException {
		ImportDeclaration im = ast.newImportDeclaration();
		im.setName(ast.newName(name));
		im.setOnDemand(isOnDemand);
		im.setStatic(isStatic);
		astRoot.imports()
			.add(im);
		this.astRoot = this.saveChanges();
	}

	/**
	 * Returns the list of the defined imports
	 */
	public List<ImportDeclaration> getImports() {
		return ASTNodeUtil.convertToTypedList(astRoot.imports(), ImportDeclaration.class);
	}

	/**
	 * Adds a new empty method to the compilation unit. This method's contents
	 * can then be changed by invoking
	 * {@link JdtUnitFixtureClass#addMethodBlock(MethodDeclaration, String).
	 * 
	 * @param methodName
	 * @return the newly created {@link MethodDeclaration}
	 * @throws JavaModelException
	 * @throws BadLocationException
	 * @throws JdtUnitException
	 */
	public MethodDeclaration addMethod(String methodName)
			throws JavaModelException, BadLocationException, JdtUnitException {
		return addMethod(methodName, null, null);
	}

	/**
	 * Adds a new method with default modifiers and the given statements to the
	 * compilation unit
	 * 
	 * @param methodName
	 * @param statements
	 * @return the newly created {@link MethodDeclaration}
	 * @throws JavaModelException
	 * @throws BadLocationException
	 * @throws JdtUnitException
	 */
	public MethodDeclaration addMethod(String methodName, String statements)
			throws JavaModelException, BadLocationException, JdtUnitException {
		return addMethod(methodName, statements, null);
	}

	/**
	 * Adds a new empty method with the specified modifiers to the compilation
	 * unit
	 * 
	 * @param methodName
	 * @param modifiers
	 * @return
	 * @throws JavaModelException
	 * @throws BadLocationException
	 * @throws JdtUnitException
	 */
	public MethodDeclaration addMethod(String methodName, List<ModifierKeyword> modifiers)
			throws JavaModelException, BadLocationException, JdtUnitException {
		return addMethod(methodName, null, modifiers);
	}

	/**
	 * Adds a new method with the given statements and modifiers to the
	 * compilation unit
	 * 
	 * @param methodName
	 * @param statements
	 * @param modifiers
	 * @return
	 * @throws JavaModelException
	 * @throws BadLocationException
	 * @throws JdtUnitException
	 */
	public MethodDeclaration addMethod(String methodName, String statements, List<ModifierKeyword> modifiers)
			throws JavaModelException, BadLocationException, JdtUnitException {
		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setName(ast.newSimpleName(methodName));

		if (modifiers != null && !modifiers.isEmpty()) {
			modifiers.stream()
				.map(ast::newModifier)
				.forEach(modifier -> methodDeclaration.modifiers()
					.add(modifier));
		}

		typeDeclaration.bodyDeclarations()
			.add(methodDeclaration);

		if (statements != null && !statements.isEmpty()) {
			addMethodBlock(methodDeclaration, statements);
		}

		methods.put(methodName, methodDeclaration);

		return methodDeclaration;
	}

	/**
	 * Parses an entire {@link MethodDeclaration} from the given method
	 * declaration source and adds it to the root type declaration.
	 * 
	 * @param methodDeclarationSource
	 * @throws JdtUnitException
	 * @throws JavaModelException
	 * @throws BadLocationException
	 */
	public void addMethodDeclarationFromString(String methodDeclarationSource)
			throws JdtUnitException, JavaModelException, BadLocationException {
		TypeDeclaration tempType = ASTNodeBuilder.createTypeDeclarationFromString("TempType", methodDeclarationSource);
		ASTNode methodDeclarationCopy = ASTNode.copySubtree(ast,
				(ASTNode) tempType.bodyDeclarations()
					.get(0));
		MethodDeclaration methodDeclaration = (MethodDeclaration) methodDeclarationCopy;
		typeDeclaration.bodyDeclarations()
			.add(methodDeclaration);
		this.astRoot = this.saveChanges();

	}

	public void setSuperClassType(String simpleName) throws JavaModelException, BadLocationException {
		SimpleName typeName = ast.newSimpleName(simpleName);
		SimpleType type = ast.newSimpleType(typeName);

		typeDeclaration.setSuperclassType(type);
		this.astRoot = saveChanges();

	}

	public void setSuperInterfaceType(String... simpleNames) throws JavaModelException, BadLocationException {
		for (String simpleName : simpleNames) {
			SimpleName typeName = ast.newSimpleName(simpleName);
			SimpleType type = ast.newSimpleType(typeName);
			typeDeclaration.superInterfaceTypes()
				.add(type);
		}
		this.astRoot = saveChanges();

	}

	/**
	 * Returns the method with the specified name
	 * 
	 * @param name
	 *            name of the method to return
	 * @return
	 */
	public Optional<MethodDeclaration> getMethod(String name) {
		return Optional.ofNullable(methods.get(name));
	}

	/**
	 * Adds statements to the stub method and saves the compilation unit with
	 * the changes.
	 * 
	 * @param statements
	 *            the statements to add separated by semicolons
	 * @throws JavaModelException
	 * @throws BadLocationException
	 * @throws JdtUnitException
	 */
	public void addMethodBlock(String statements) throws JavaModelException, BadLocationException, JdtUnitException {
		MethodDeclaration methodDeclaration = methods.get(DEFAULT_METHOD_FIXTURE_NAME);
		addMethodBlock(methodDeclaration, statements);
	}

	/**
	 * Adds statements to the stub method and saves the compilation unit with
	 * the changes.
	 * 
	 * @param methodDeclaration
	 *            method declaration to which the statements should be added to
	 * @param statements
	 *            the statements to add separated by semicolons
	 * @throws BadLocationException
	 * @throws JavaModelException
	 * @throws JdtUnitException
	 * @throws Exception
	 */
	public void addMethodBlock(MethodDeclaration methodDeclaration, String statements)
			throws JavaModelException, BadLocationException, JdtUnitException {
		ASTNode convertedAstNodeWithMethodBody = ASTNode.copySubtree(ast,
				ASTNodeBuilder.createBlockFromString(statements));
		Block block = (Block) convertedAstNodeWithMethodBody;

		methodDeclaration.setBody(block);
		this.astRoot = this.saveChanges();
	}

	/**
	 * Returns the body of the default stub method.
	 * 
	 * @return
	 */
	public Block getMethodBlock() {
		return getMethodBlock(DEFAULT_METHOD_FIXTURE_NAME);
	}

	/**
	 * Returns the body of the sub method
	 * 
	 * @param methodDeclaration
	 *            method declaration from which to get the body
	 * @return
	 */
	public Block getMethodBlock(MethodDeclaration methodDeclaration) {
		return getMethodBlock(methodDeclaration.getName()
			.getIdentifier());
	}

	/**
	 * Returns the body of the stub method.
	 * 
	 * @param methodName
	 *            name of the method, from which to get the body
	 * @return
	 */
	public Block getMethodBlock(String methodName) {
		MethodDeclaration methodDeclaration = methods.get(methodName);
		return methodDeclaration.getBody();
	}

	/**
	 * Adds the body of a {@link TypeDeclaration}. This can be a string with
	 * field and/or method declarations.
	 * 
	 * @param typeDeclarationName
	 * @param typeDeclarationString
	 * @throws JdtUnitException
	 * @throws JavaModelException
	 * @throws BadLocationException
	 */
	public void addTypeDeclarationFromString(String typeDeclarationName, String typeDeclarationString)
			throws JdtUnitException, JavaModelException, BadLocationException {
		ASTNode convertedAstNodeWithMethodBody = ASTNode.copySubtree(ast,
				ASTNodeBuilder.createTypeDeclarationFromString(typeDeclarationName, typeDeclarationString));
		typeDeclaration = (TypeDeclaration) convertedAstNodeWithMethodBody;

		astRoot.types()
			.clear();
		methods.clear();
		astRoot.types()
			.add(typeDeclaration);

		this.astRoot = this.saveChanges();
	}

	public TypeDeclaration getTypeDeclaration() {
		return typeDeclaration;
	}

	/**
	 * Accepts an ASTVisitor at the root of the stub file. If the visitor makes
	 * changes to the AST these changes are saved.
	 * 
	 * @param visitor
	 *            The visitor to accept
	 * @throws IllegalArgumentException
	 * @throws JavaModelException
	 * @throws BadLocationException
	 * @throws Exception
	 */
	public void accept(ASTVisitor visitor) throws JavaModelException, BadLocationException {
		astRoot.accept(visitor);
		TextEdit edit = astRewrite.rewriteAST();
		if (edit.hasChildren()) {
			hasChanged = true;
		}
		astRoot = saveChanges(edit);
	}

	/**
	 * Resets the Fixture to its default state
	 * 
	 * @param keepDefaultMethod
	 *            when true, the default method is kept. When false, the default
	 *            method is removed.
	 * @throws BadLocationException
	 * @throws JavaModelException
	 * 
	 */
	public void clear(boolean keepDefaultMethod) throws JavaModelException, BadLocationException {
		astRoot.imports()
			.clear();
		methods.values()
			.stream()
			.filter(keepDefaultMethod ? method -> !method.getName()
				.getIdentifier()
				.equals(DEFAULT_METHOD_FIXTURE_NAME) : method -> false)
			.forEach(MethodDeclaration::delete);
		methods.clear();

		saveChanges();
	}

	/**
	 * Getter for the ASTRewrite for the stub AST
	 * 
	 * @return
	 */
	public ASTRewrite getAstRewrite() {
		return astRewrite;
	}

	/**
	 * Convenience method to check if any edits happened on the stub AST.
	 * 
	 * @return True if the AST was changed since setup, false otherwise
	 */
	public boolean hasChanged() {
		return hasChanged;
	}

	/**
	 * Deletes this compilation unit
	 * 
	 * @throws JavaModelException
	 */
	void delete() throws JavaModelException {
		compilationUnit.delete(true, new NullProgressMonitor());
	}

	private void createCompilationUnit() throws JdtUnitException {
		compilationUnit = new CompilationUnitBuilder(packageFragment).setName(className + ".java")
			.build();

		ASTParser parser = ASTParser.newParser(AST.JLS11);
		parser.setSource(compilationUnit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		astRoot = (CompilationUnit) parser.createAST(null);
		astRoot.recordModifications();

		ast = astRoot.getAST();

		PackageDeclaration pd = ast.newPackageDeclaration();
		Name astName = ast.newName(packageFragment.getElementName());
		pd.setName(astName);
		astRoot.setPackage(pd);

		typeDeclaration = ast.newTypeDeclaration();
		typeDeclaration.setInterface(false);
		typeDeclaration.modifiers()
			.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		typeDeclaration.setName(ast.newSimpleName(className));
		astRoot.types()
			.add(typeDeclaration);
	}

	private CompilationUnit saveChanges() throws JavaModelException, BadLocationException {
		Document document = new Document(compilationUnit.getSource());
		TextEdit res = astRoot.rewrite(document, fixtureProject.getOptions());
		return saveChanges(res);
	}

	private CompilationUnit saveChanges(TextEdit textEdit) throws JavaModelException, BadLocationException {
		Document document = new Document(compilationUnit.getSource());
		textEdit.apply(document);
		compilationUnit.getBuffer()
			.setContents(document.get());

		refreshFixtures();
		return astRoot;
	}

	private void refreshFixtures() {
		ASTParser parser = ASTParser.newParser(AST.JLS11);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);

		astRoot = (CompilationUnit) parser.createAST(null);
		astRoot.recordModifications();
		ast = astRoot.getAST();
		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types()
			.get(0);
		methods = convertMethodDeclarationArrayToMap(typeDecl.getMethods());
		astRewrite = ASTRewrite.create(astRoot.getAST());
		typeDeclaration = (TypeDeclaration) astRoot.types()
			.get(0);
		hasChanged = false;
	}

	private HashMap<String, MethodDeclaration> convertMethodDeclarationArrayToMap(
			MethodDeclaration[] methodDeclarationArray) {
		HashMap<String, MethodDeclaration> methodDeclarationMap = new HashMap<>();

		for (MethodDeclaration methodDeclaration : methodDeclarationArray) {
			String methodName = methodDeclaration.getName()
				.getIdentifier();
			methodDeclarationMap.put(methodName, methodDeclaration);
		}

		return methodDeclarationMap;
	}

	public ICompilationUnit getICompilationUnit() {
		return compilationUnit;
	}

	public void addDefaultFixtureMethod() throws JavaModelException, BadLocationException, JdtUnitException {
		addMethod(DEFAULT_METHOD_FIXTURE_NAME);
	}

	public void addDefaultMethodGenericTypeParameter(List<String> typeNames) {
		MethodDeclaration methodDeclaration = methods.get(DEFAULT_METHOD_FIXTURE_NAME);
		for (String typeName : typeNames) {
			TypeParameter typeParam = ast.newTypeParameter();
			typeParam.setName(ast.newSimpleName(typeName));
			methodDeclaration.typeParameters()
				.add(typeParam);
		}
	}

	public void addDefaultMethodFormalParameter(String simpleTypeName, String name) {
		MethodDeclaration methodDeclaration = methods.get(DEFAULT_METHOD_FIXTURE_NAME);
		SingleVariableDeclaration parameterDeclaration = NodeBuilder.newSingleVariableDeclaration(ast,
				ast.newSimpleName(name), ast.newSimpleType(ast.newSimpleName(simpleTypeName)));
		methodDeclaration.parameters()
			.add(parameterDeclaration);
	}

	public void addDefaultMethodFormalGenericParameters(String type, List<String> typeArguments, String name) {
		MethodDeclaration methodDeclaration = methods.get(DEFAULT_METHOD_FIXTURE_NAME);
		ParameterizedType parameterizedType = ast
			.newParameterizedType(ast.newSimpleType(ast.newSimpleName(type)));

		for (String typeArgumentNane : typeArguments) {
			Type typeArgument = (ast.newSimpleType(ast.newSimpleName(typeArgumentNane)));
			parameterizedType.typeArguments()
				.add(typeArgument);
		}

		SingleVariableDeclaration parameterDeclaration = NodeBuilder.newSingleVariableDeclaration(ast,
				ast.newSimpleName(name), parameterizedType);
		methodDeclaration.parameters()
			.add(parameterDeclaration);
	}

	public CompilationUnit getRootNode() {
		return astRoot;
	}
}
