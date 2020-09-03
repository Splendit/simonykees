package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

public class UseFilesBufferedReaderASTVisitor extends AbstractAddImportASTVisitor {
	
	@Override
	public boolean visit(TryStatement tryStatement) {
		List<VariableDeclarationExpression> resources = ASTNodeUtil.convertToTypedList(tryStatement.resources(), VariableDeclarationExpression.class);
		if(resources.size() != 2) {
			return true;
		}
		
		FileReaderAnalyzer fileReaderAnalyzer = new FileReaderAnalyzer(resources.get(0));
		if(!fileReaderAnalyzer.isFileReaderDeclaration()) {
			return false;
		}
		SimpleName fileReaderName = fileReaderAnalyzer.getFileReaderName();
		
		NewBufferedReaderAnalyzer bufferedReaderAnalyzer = new NewBufferedReaderAnalyzer(resources.get(1));
		boolean validBufferedReader = bufferedReaderAnalyzer.isInitializedWith(fileReaderName);
		if(!validBufferedReader) {
			return false;
		}
		
		boolean isUsedInTryBody = hasUsagesOn(tryStatement.getBody(), fileReaderName);
		if(isUsedInTryBody) {
			return false;
		}
		
		// Now the transformation happens
		Expression pathExpression = fileReaderAnalyzer.computePathExpression();
		Optional<Expression> charset = fileReaderAnalyzer.computeCharsetExpression();
		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathExpression);
		charset.ifPresent(arguments::add);
		AST ast = tryStatement.getAST();
		Expression filesExpression = createFilesExpression(ast);
		MethodInvocation filesNewBufferedReader = NodeBuilder.newMethodInvocation(ast, filesExpression, ast.newSimpleName("newBufferedReader"), arguments); //$NON-NLS-1$
		astRewrite.remove(resources.get(0), null);
		Expression initializer = bufferedReaderAnalyzer.getInitializer();
		astRewrite.replace(initializer, filesNewBufferedReader, null);
		return true;
	}

	private Expression createFilesExpression(AST ast) {
		//TODO: this has to be fixed. We need to consider cases when a fully qualified name is required
		SimpleName expression = ast.newSimpleName(java.nio.file.Files.class.getSimpleName());
		return expression;
	}

	private boolean hasUsagesOn(Block body, SimpleName fileReaderName) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(fileReaderName);
		body.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		return usages.isEmpty();
	}

}
