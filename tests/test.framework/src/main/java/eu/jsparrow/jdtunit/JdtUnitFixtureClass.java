package eu.jsparrow.jdtunit;

import java.util.HashMap;
import java.util.List;

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
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import eu.jsparrow.jdtunit.util.CompilationUnitBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

@SuppressWarnings("unchecked")
public class JdtUnitFixtureClass {

	private static final String DEFAULT_METHOD_FIXTURE_NAME = "FixtureMethod";

	private ICompilationUnit compilationUnit;

	private CompilationUnit astRoot;

	private AST ast;

	private ASTRewrite astRewrite;

	private boolean hasChanged = false;

	private HashMap<String, MethodDeclaration> methods = new HashMap<>();

	private IPackageFragment packageFragment;
	private String className;

	private TypeDeclaration typeDeclaration;
	private JdtUnitFixture fixtureProject;

	public JdtUnitFixtureClass(JdtUnitFixture fixtureProject, IPackageFragment packageFragment, String className)
			throws JdtUnitException {
		this.packageFragment = packageFragment;
		this.className = className;
		this.fixtureProject = fixtureProject;

		createCompilationUnit();
	}

	private void createCompilationUnit() throws JdtUnitException {
		compilationUnit = new CompilationUnitBuilder(packageFragment).setName(className + ".java") //$NON-NLS-1$
			.build();

		ASTParser parser = ASTParser.newParser(AST.JLS10);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(false);
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

	public MethodDeclaration addMethod(String methodName)
			throws JavaModelException, BadLocationException, JdtUnitException {
		return addMethod(methodName, null);
	}

	public MethodDeclaration addMethod(String methodName, String statements)
			throws JavaModelException, BadLocationException, JdtUnitException {
		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setName(ast.newSimpleName(methodName));
		typeDeclaration.bodyDeclarations()
			.add(methodDeclaration);

		if (statements != null && !statements.isEmpty()) {
			addMethodBlock(methodDeclaration, statements);
		}

		methods.put(methodName, methodDeclaration);

		return methodDeclaration;
	}

	public void addMethodBlock(String statements) throws JavaModelException, BadLocationException, JdtUnitException {
		MethodDeclaration methodDeclaration = methods.get(DEFAULT_METHOD_FIXTURE_NAME);
		addMethodBlock(methodDeclaration, statements);
	}

	/**
	 * Adds statements to the stub method and saves the compilation unit with
	 * the changes.
	 * 
	 * @param statements
	 *            the statements to add separated by semicolons
	 * @throws BadLocationException
	 * @throws JavaModelException
	 * @throws JdtUnitException
	 * @throws Exception
	 */
	public void addMethodBlock(MethodDeclaration methodDeclaration, String statements)
			throws JavaModelException, BadLocationException, JdtUnitException {
		ASTNode convertedAstNodeWithMethodBody = ASTNode.copySubtree(ast, createBlockFromString(statements));
		Block block = (Block) convertedAstNodeWithMethodBody;

		methodDeclaration.setBody(block);
		this.astRoot = this.saveChanges();
	}

	private CompilationUnit saveChanges() throws JavaModelException, BadLocationException {
		Document document = new Document(compilationUnit.getSource());
		TextEdit res = astRoot.rewrite(document, fixtureProject.getOptions());
		res.apply(document);
		compilationUnit.getBuffer()
			.setContents(document.get());

		refreshFixtures();
		return astRoot;
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

	private CompilationUnit saveChanges(TextEdit textEdit) throws JavaModelException, BadLocationException {
		Document document = new Document(compilationUnit.getSource());
		textEdit.apply(document);
		compilationUnit.getBuffer()
			.setContents(document.get());

		refreshFixtures();
		return astRoot;
	}

	private void refreshFixtures() {
		ASTParser parser = ASTParser.newParser(AST.JLS10);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);

		astRoot = (CompilationUnit) parser.createAST(null);
		astRoot.recordModifications();
		ast = astRoot.getAST();
		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types()
			.get(0);
		methods = convertMethodDeclarationArrayToMap(typeDecl.getMethods());
		astRewrite = ASTRewrite.create(astRoot.getAST());
		hasChanged = false;
	}

	public Block getMethodBlock() {
		return getMethodBlock(DEFAULT_METHOD_FIXTURE_NAME);
	}

	/**
	 * Returns the body of the stub method.
	 * 
	 * @return
	 */
	public Block getMethodBlock(MethodDeclaration methodDeclaration) {
		return getMethodBlock(methodDeclaration.getName()
			.getIdentifier());
	}

	/**
	 * Returns the body of the stub method.
	 * 
	 * @return
	 */
	public Block getMethodBlock(String methodName) {
		MethodDeclaration methodDeclaration = methods.get(methodName);
		return methodDeclaration.getBody();
	}

	private Block createBlockFromString(String string) throws JdtUnitException {
		ASTParser astParser = ASTParser.newParser(AST.JLS10);
		astParser.setSource(string.toCharArray());
		astParser.setKind(ASTParser.K_STATEMENTS);
		ASTNode result = astParser.createAST(null);
		if ((result.getFlags() & ASTNode.MALFORMED) == ASTNode.MALFORMED) {
			throw new JdtUnitException(String.format("Malformed statements. Failed to parse '%s'.", string));
		}
		Block block = (Block) result;
		if (block.statements()
			.isEmpty()) {
			throw new JdtUnitException("Can not create an empty block. There might be syntax errors");
		}
		return block;
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

	/**
	 * Resets the Fixture to its default state
	 * 
	 * @throws BadLocationException
	 * @throws JavaModelException
	 * 
	 * @throws Exception
	 */
	public void clear() throws JavaModelException, BadLocationException {
		astRoot.imports()
			.clear();
		methods.values()
			.stream()
			.filter(method -> !method.getName()
				.getIdentifier()
				.contentEquals(DEFAULT_METHOD_FIXTURE_NAME))
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
}
