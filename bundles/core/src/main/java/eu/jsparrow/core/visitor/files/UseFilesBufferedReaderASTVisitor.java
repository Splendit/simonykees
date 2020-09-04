package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

public class UseFilesBufferedReaderASTVisitor extends AbstractAddImportASTVisitor {

	private static final String FILES_QUALIFIED_NAME = java.nio.file.Files.class.getName();
	private static final String PATHS_QUALIFIED_NAME = java.nio.file.Paths.class.getName();
	private static final String CHARSET_QUALIFIED_NAME = java.nio.charset.Charset.class.getName();

	private Set<String> safeImports = new HashSet<>();
	private Set<String> typesImportedOnDemand = new HashSet<>();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);

		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);
		
		if (isSafeToAddImport(compilationUnit, PATHS_QUALIFIED_NAME)) {
			safeImports.add(PATHS_QUALIFIED_NAME);
			if (matchesTypeImportOnDemand(importDeclarations, PATHS_QUALIFIED_NAME)) {
				typesImportedOnDemand.add(PATHS_QUALIFIED_NAME);
			}
		}
		
		if(isSafeToAddImport(compilationUnit, FILES_QUALIFIED_NAME)) {
			safeImports.add(FILES_QUALIFIED_NAME);
			if(matchesTypeImportOnDemand(importDeclarations, FILES_QUALIFIED_NAME)) {
				typesImportedOnDemand.add(FILES_QUALIFIED_NAME);
			}
		}
		
		if(isSafeToAddImport(compilationUnit, CHARSET_QUALIFIED_NAME)) {
			safeImports.add(CHARSET_QUALIFIED_NAME);
			if(matchesTypeImportOnDemand(importDeclarations, CHARSET_QUALIFIED_NAME)) {
				typesImportedOnDemand.add(CHARSET_QUALIFIED_NAME);
			}
		}

		return continueVisiting;
	}
	
	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		super.endVisit(compilationUnit);
		safeImports.clear();
		typesImportedOnDemand.clear();
	}

	@Override
	public boolean visit(TryStatement tryStatement) {
		List<VariableDeclarationExpression> resources = ASTNodeUtil.convertToTypedList(tryStatement.resources(),
				VariableDeclarationExpression.class);
		if (resources.size() != 2) {
			return true;
		}

		FileReaderAnalyzer fileReaderAnalyzer = new FileReaderAnalyzer(resources.get(0));
		if (!fileReaderAnalyzer.isFileReaderDeclaration()) {
			return false;
		}
		SimpleName fileReaderName = fileReaderAnalyzer.getFileReaderName();

		NewBufferedReaderAnalyzer bufferedReaderAnalyzer = new NewBufferedReaderAnalyzer(resources.get(1));
		boolean validBufferedReader = bufferedReaderAnalyzer.isInitializedWith(fileReaderName);
		if (!validBufferedReader) {
			return false;
		}

		boolean isUsedInTryBody = hasUsagesOn(tryStatement.getBody(), fileReaderName);
		if (isUsedInTryBody) {
			return false;
		}

		// Now the transformation happens
		AST ast = tryStatement.getAST();
		Expression pathArgument = fileReaderAnalyzer.getPathExpression();
		MethodInvocation pathsGet = ast.newMethodInvocation();
		String pathsIdentifier = findTypeNameForStaticMethodInvocation(PATHS_QUALIFIED_NAME);
		pathsGet.setExpression(ast.newName(pathsIdentifier));
		pathsGet.setName(ast.newSimpleName("get")); //$NON-NLS-1$
		@SuppressWarnings("unchecked")
		List<Expression> pathsGetParameters = pathsGet.arguments();
		pathsGetParameters.add((Expression)astRewrite.createCopyTarget(pathArgument));
		Expression charset = fileReaderAnalyzer.getCharset()
				.map(exp -> (Expression)astRewrite.createCopyTarget(exp))
				.orElse(createDefaultCharsetExpression(ast));
		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathsGet);
		arguments.add(charset);
		Expression filesExpression = ast.newName(findTypeNameForStaticMethodInvocation(FILES_QUALIFIED_NAME));
		MethodInvocation filesNewBufferedReader = NodeBuilder.newMethodInvocation(ast, filesExpression,
				ast.newSimpleName("newBufferedReader"), arguments); //$NON-NLS-1$
		astRewrite.remove(resources.get(0), null);
		Expression initializer = bufferedReaderAnalyzer.getInitializer();
		astRewrite.replace(initializer, filesNewBufferedReader, null);
		return true;
	}

	private Expression createDefaultCharsetExpression(AST ast) {
		MethodInvocation defaultCharset = ast.newMethodInvocation();
		defaultCharset.setExpression(ast.newName(findTypeNameForStaticMethodInvocation(CHARSET_QUALIFIED_NAME)));
		defaultCharset.setName(ast.newSimpleName("defaultCharset")); //$NON-NLS-1$
		return defaultCharset;
	}

	private boolean hasUsagesOn(Block body, SimpleName fileReaderName) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(fileReaderName);
		body.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		return !usages.isEmpty();
	}
	
	private String findTypeNameForStaticMethodInvocation(String qualifiedName) {
		if (!safeImports.contains(qualifiedName)) {
			return qualifiedName;
		}
		if (!typesImportedOnDemand.contains(qualifiedName)) {
			addImports.add(qualifiedName);
		}
		return getSimpleName(qualifiedName);
	}

}
