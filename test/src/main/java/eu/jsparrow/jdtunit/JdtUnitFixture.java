package eu.jsparrow.jdtunit;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;


/**
 * 
 * 
 * @author Hans-Jörg Schrödl
 *
 */
@SuppressWarnings({ "nls", "unchecked" })
public class JdtUnitFixture {

	private static final String PROJECT_FIXTURE_NAME = "FxitureProject";

	private static final String PACKAGE_FIXTURE_NAME = "fixturepackage";

	private static final String FILE_FIXTURE_NAME = "FixtureClass.java";

	private static final String CLASS_FIXTURE_NAME = "FixtureClass";

	private static final String METHOD_FIXTURE_NAME = "FixtureMethod";

	private IProject project;

	private IJavaProject javaProject;

	private IPackageFragment packageFragment;

	private ICompilationUnit compilationUnit;

	private final HashMap<String, String> options = new HashMap<>();

	private CompilationUnit astRoot;

	private AST ast;

	private MethodDeclaration methodDeclaration;
	
	public JdtUnitFixture() {
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
	}

	public void setUp() throws Exception {
		createJavaProject();

		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(project);
		packageFragment = root.createPackageFragment(PACKAGE_FIXTURE_NAME, false, null);

		compilationUnit = packageFragment.createCompilationUnit(FILE_FIXTURE_NAME, "", false, null);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
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
		td.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		td.setName(ast.newSimpleName(CLASS_FIXTURE_NAME));
		astRoot.types().add(td);

		methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setName(ast.newSimpleName(METHOD_FIXTURE_NAME));
		td.bodyDeclarations().add(methodDeclaration);
	}

	public void clear() throws Exception {
		astRoot.imports().clear();
		methodDeclaration.getBody().delete();

		saveChanges();
	}

	public void tearDown() throws CoreException {
		project.delete(true, null);
	}

	public void addImport(String name) {
		ImportDeclaration im = ast.newImportDeclaration();
		im.setName(ast.newName(name));
		astRoot.imports().add(im);
	}
	
	public void addMethodBlock(String statements) {
		ASTNode convertedAstNodeWithMethodBody = ASTNode.copySubtree(ast, createBlockFromString(statements));
		Block block = (Block) convertedAstNodeWithMethodBody;

		methodDeclaration.setBody(block);
	}
	
	public Block getMethodBlock(){
		return methodDeclaration.getBody();
	}

	public CompilationUnit saveChanges() throws Exception {
		Document document = new Document(compilationUnit.getSource());
		TextEdit res = astRoot.rewrite(document, options);
		res.apply(document);
		compilationUnit.getBuffer().setContents(document.get());

		refreshFixtures();
		return astRoot;
	}

	public CompilationUnit saveChanges(TextEdit textEdit) throws Exception {
		Document document = new Document(compilationUnit.getSource());
		textEdit.apply(document);
		compilationUnit.getBuffer().setContents(document.get());

		refreshFixtures();
		return astRoot;
	}

	private void createJavaProject() throws CoreException, JavaModelException {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_FIXTURE_NAME);
		project.create(null);
		project.open(null);

		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.setDescription(description, null);

		javaProject = JavaCore.create(project);

		// build path is: project as source folder and JRE container
		IClasspathEntry[] cpentry = new IClasspathEntry[] { JavaCore.newSourceEntry(javaProject.getPath()),
				JavaRuntime.getDefaultJREContainerEntry() };
		javaProject.setRawClasspath(cpentry, javaProject.getPath(), null);
		javaProject.setOptions(options);
	}

	private Block createBlockFromString(String string) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setSource(string.toCharArray());
		astParser.setKind(ASTParser.K_STATEMENTS);
		return (Block) astParser.createAST(null);
	}

	private void refreshFixtures() {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);

		astRoot = (CompilationUnit) parser.createAST(null);
		astRoot.recordModifications();
		ast = astRoot.getAST();
		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
		methodDeclaration = typeDecl.getMethods()[0];
	}

}
