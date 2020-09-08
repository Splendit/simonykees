package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

public class UseFilesBufferedReaderASTVisitor extends AbstractAddImportASTVisitor {

	private static final String FILES_QUALIFIED_NAME = java.nio.file.Files.class.getName();
	private static final String PATHS_QUALIFIED_NAME = java.nio.file.Paths.class.getName();
	private static final String CHARSET_QUALIFIED_NAME = java.nio.charset.Charset.class.getName();
	private static final String BUFFERED_READER_QUALIFIED_NAME = java.io.BufferedReader.class.getName();

	private Set<String> safeImports = new HashSet<>();
	private Set<String> typesImportedOnDemand = new HashSet<>();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);

		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);
		//TODO: push this functionality up.
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
	public boolean  visit(VariableDeclarationFragment fragment) {
		
		SimpleName name = fragment.getName();
		ITypeBinding typeBinding = name.resolveTypeBinding();
		if(!ClassRelationUtil.isContentOfType(typeBinding, BUFFERED_READER_QUALIFIED_NAME)) {
			return true;
		}
		
		Expression initializer = fragment.getInitializer();
		if(initializer == null || initializer.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return true;
		}
		ITypeBinding initializerType = initializer.resolveTypeBinding();
		if(!ClassRelationUtil.isContentOfType(initializerType, BUFFERED_READER_QUALIFIED_NAME)) {
			return true;
		}
		ClassInstanceCreation newBufferedReader = (ClassInstanceCreation)initializer;
		List<Expression> newBufferedReaderArgs = ASTNodeUtil.convertToTypedList(newBufferedReader.arguments(), Expression.class);
		if(newBufferedReaderArgs.size() != 1) {
			return true;
		}
		
		Expression bufferedReaderArg = newBufferedReaderArgs.get(0);
		ITypeBinding firstArgType = bufferedReaderArg.resolveTypeBinding();
		if(!ClassRelationUtil.isContentOfType(firstArgType, java.io.FileReader.class.getName())) {
			return true;
		}
		
		NewBufferedReaderAnalyzer analyzer = new NewBufferedReaderAnalyzer();
		if(bufferedReaderArg.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			boolean feasible = analyzer.isInitializedWithNewReader((ClassInstanceCreation)bufferedReaderArg);
			if(!feasible) {
				return true;
			}
			AST ast = fragment.getAST();
			MethodInvocation pathsGet = ast.newMethodInvocation();
			String pathsIdentifier = findTypeNameForStaticMethodInvocation(PATHS_QUALIFIED_NAME);
			pathsGet.setExpression(ast.newName(pathsIdentifier));
			pathsGet.setName(ast.newSimpleName("get")); //$NON-NLS-1$
			@SuppressWarnings("unchecked")
			List<Expression> pathsGetParameters = pathsGet.arguments();
			List<Expression> pathExpressions = analyzer.getPathExpressions();
			pathExpressions.forEach(pathArgument -> pathsGetParameters.add((Expression)astRewrite.createCopyTarget(pathArgument)));

			Expression charset = analyzer.getCharset()
					.map(exp -> (Expression)astRewrite.createCopyTarget(exp))
					.orElse(createDefaultCharsetExpression(ast));
			List<Expression> arguments = new ArrayList<>();
			arguments.add(pathsGet);
			arguments.add(charset);
			Expression filesExpression = ast.newName(findTypeNameForStaticMethodInvocation(FILES_QUALIFIED_NAME));
			MethodInvocation filesNewBufferedReader = NodeBuilder.newMethodInvocation(ast, filesExpression,
					ast.newSimpleName("newBufferedReader"), arguments); //$NON-NLS-1$
			
			astRewrite.replace(fragment.getInitializer(), filesNewBufferedReader, null);
		} else if(bufferedReaderArg.getNodeType() == ASTNode.SIMPLE_NAME) {
			if(fragment.getLocationInParent() == VariableDeclarationExpression.FRAGMENTS_PROPERTY) {
				VariableDeclarationExpression declarationExpression = (VariableDeclarationExpression) fragment.getParent();
				if(declarationExpression.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY) {
					TryStatement tryStatement = (TryStatement)declarationExpression.getParent();
					List<VariableDeclarationExpression> resources = ASTNodeUtil.convertToTypedList(tryStatement.resources(), VariableDeclarationExpression.class);
					VariableDeclarationFragment fileReaderResource = resources.stream()
						.flatMap(resource -> 
							ASTNodeUtil
								.convertToTypedList(resource.fragments(), VariableDeclarationFragment.class)
								.stream())
						.filter(resource -> resource.getName().getIdentifier().equals(((SimpleName)bufferedReaderArg).getIdentifier()))
						.findFirst()
						.orElse(null);
					if(fileReaderResource != null) {
						FileReaderAnalyzer fileReaderAnalyzer = new FileReaderAnalyzer((VariableDeclarationExpression)fileReaderResource.getParent());
						if (!fileReaderAnalyzer.isFileReaderDeclaration()) {
							return false;
						}
						
						boolean isUsedInTryBody = hasUsagesOn(tryStatement.getBody(), fileReaderResource.getName());
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

						astRewrite.replace(initializer, filesNewBufferedReader, null);
						
						
					}
						
					
				}
			}
		}
		
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
