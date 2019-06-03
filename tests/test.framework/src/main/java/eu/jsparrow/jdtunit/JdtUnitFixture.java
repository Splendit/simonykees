package eu.jsparrow.jdtunit;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
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
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import eu.jsparrow.jdtunit.util.CompilationUnitBuilder;
import eu.jsparrow.jdtunit.util.JavaProjectBuilder;
import eu.jsparrow.jdtunit.util.PackageFragmentBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * <p>
 * Fixture class that stubs a JDT compilation unit. Within that compilation unit
 * ASTNodes can be inserted and deleted. In order to get working type bindings
 * for any AST created within the stubbed compilation unit a full java project
 * is created in code.
 * </p>
 * 
 * @author Hans-Jörg Schrödl
 *
 */
@SuppressWarnings({ "nls", "unchecked" })
public class JdtUnitFixture {

	private static final String PROJECT_FIXTURE_NAME = "FixtureProject";

	private static final String PACKAGE_FIXTURE_NAME = "fixturepackage";

	private static final String FILE_FIXTURE_NAME = "FixtureClass.java";

	private static final String CLASS_FIXTURE_NAME = "FixtureClass";

	private static final String METHOD_FIXTURE_NAME = "FixtureMethod";

	private IJavaProject javaProject;

	private ICompilationUnit compilationUnit;

	private final HashMap<String, String> options = new HashMap<>();

	private CompilationUnit astRoot;

	private AST ast;

	private ASTRewrite astRewrite;

	private boolean hasChanged = false;

	private MethodDeclaration methodDeclaration;

	public JdtUnitFixture() {
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
	}

	/**
	 * Creates the fixture. Elements set up are:
	 * <ul>
	 * <li>A stub java project
	 * <li>A stub package within that project
	 * <li>A stub file within that package
	 * <li>A class containing a single method within that file
	 * </ul>
	 * 
	 * @throws JdtUnitException
	 * 
	 * @throws Exception
	 */
	public void setUp() throws JdtUnitException {
		javaProject = new JavaProjectBuilder().name(PROJECT_FIXTURE_NAME)
			.options(options)
			.build();

		IPackageFragment packageFragment = addPackageFragment(PACKAGE_FIXTURE_NAME);

		compilationUnit = addCompilationUnit(packageFragment, FILE_FIXTURE_NAME);

		ASTParser parser = ASTParser.newParser(AST.JLS10);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(false);
		astRoot = (CompilationUnit) parser.createAST(null);
		astRoot.recordModifications();

		ast = astRoot.getAST();

		PackageDeclaration pd = ast.newPackageDeclaration();
		Name astName = ast.newName(PACKAGE_FIXTURE_NAME);
		pd.setName(astName);
		astRoot.setPackage(pd);

		TypeDeclaration td = ast.newTypeDeclaration();
		td.setInterface(false);
		td.modifiers()
			.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		td.setName(ast.newSimpleName(CLASS_FIXTURE_NAME));
		astRoot.types()
			.add(td);

		methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setName(ast.newSimpleName(METHOD_FIXTURE_NAME));
		td.bodyDeclarations()
			.add(methodDeclaration);
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
		methodDeclaration.getBody()
			.delete();

		saveChanges();
	}

	/**
	 * Removes the fixture by deleting the stubbed elements.
	 * 
	 * @throws CoreException
	 */
	public void tearDown() throws CoreException {
		javaProject.getProject()
			.delete(true, null);
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
	public void addMethodBlock(String statements) throws JavaModelException, BadLocationException, JdtUnitException {
		ASTNode convertedAstNodeWithMethodBody = ASTNode.copySubtree(ast, createBlockFromString(statements));
		Block block = (Block) convertedAstNodeWithMethodBody;

		methodDeclaration.setBody(block);
		this.astRoot = this.saveChanges();
	}

	/**
	 * Returns the list of the defined imports
	 */
	public List<ImportDeclaration> getImports() {
		return ASTNodeUtil.convertToTypedList(astRoot.imports(), ImportDeclaration.class);
	}

	/**
	 * Returns the body of the stub method.
	 * 
	 * @return
	 */
	public Block getMethodBlock() {
		return methodDeclaration.getBody();
	}

	private CompilationUnit saveChanges() throws JavaModelException, BadLocationException {
		Document document = new Document(compilationUnit.getSource());
		TextEdit res = astRoot.rewrite(document, options);
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

	private void refreshFixtures() {
		ASTParser parser = ASTParser.newParser(AST.JLS10);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);

		astRoot = (CompilationUnit) parser.createAST(null);
		astRoot.recordModifications();
		ast = astRoot.getAST();
		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types()
			.get(0);
		methodDeclaration = typeDecl.getMethods()[0];
		astRewrite = ASTRewrite.create(astRoot.getAST());
		hasChanged = false;
	}

	public IPackageFragment addPackageFragment(String name) throws JdtUnitException {
		return new PackageFragmentBuilder(javaProject).setName(name)
			.build();
	}

	public ICompilationUnit addCompilationUnit(IPackageFragment packageFragment, String name) throws JdtUnitException {
		return new CompilationUnitBuilder(packageFragment).setName(name)
			.build();
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
