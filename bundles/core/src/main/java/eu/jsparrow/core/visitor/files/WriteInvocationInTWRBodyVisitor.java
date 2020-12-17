package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * This visitor is a helper visitor intended to be used for visiting
 * {@link org.eclipse.jdt.core.dom.TryStatement}-instances.<br>
 * It looks for invocations of {@link java.io.Writer#write(String)} which can be
 * replaced by invocations of methods which have the name "writeString" and are
 * available as static methods of the class {@link java.nio.file.Files} since
 * Java 11.<br>
 * Additionally, this visitor stores all data which are necessary for the
 * corresponding code transformation. <br>
 * 
 * 
 * @since 3.25.0
 *
 */
public class WriteInvocationInTWRBodyVisitor extends ASTVisitor {
	private final SignatureData write = new SignatureData(java.io.Writer.class, "write", java.lang.String.class); //$NON-NLS-1$
	private final TryStatement tryStatement;
	private final List<FilesNewBufferedIOTransformationData> filesNewBufferedWriterInvocationDataList = new ArrayList<>();
	private final List<UseFilesWriteStringAnalysisResult> bufferedWriterInstanceCreationDataList = new ArrayList<>();

	public WriteInvocationInTWRBodyVisitor(TryStatement tryStatement) {
		this.tryStatement = tryStatement;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null || methodInvocationExpression
			.getNodeType() != ASTNode.SIMPLE_NAME) {
			return true;
		}
		SimpleName writerVariableSimpleName = (SimpleName) methodInvocationExpression;

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}
		ExpressionStatement writeInvocationStatementToReplace = (ExpressionStatement) methodInvocation.getParent();

		if (!write.isEquivalentTo(methodInvocation.resolveMethodBinding())) {
			return true;
		}
		Expression charSequenceArgument = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);

		if (writeInvocationStatementToReplace.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return true;
		}
		Block blockOfInvocationStatement = (Block) writeInvocationStatementToReplace.getParent();

		if (blockOfInvocationStatement.getParent() != tryStatement) {
			return true;
		}

		TryResourceAnalyzer bufferedWriterResourceAnalyzer = new TryResourceAnalyzer();
		if (!bufferedWriterResourceAnalyzer.analyze(tryStatement, writerVariableSimpleName)) {
			return true;
		}

		if (!checkWriterVariableUsage(writerVariableSimpleName, blockOfInvocationStatement)) {
			return true;
		}

		Expression bufferedIOInitializer = bufferedWriterResourceAnalyzer.getResourceInitializer();
		if (bufferedIOInitializer.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation bufferedIOInitializerMethodInvocation = (MethodInvocation) bufferedIOInitializer;
			VariableDeclarationExpression resourceToRemove = bufferedWriterResourceAnalyzer.getResource();
			new WriteAnalyzerUsingFilesNewBufferedWriter().findTransformationDataUsingFilesNewBufferedWriter(
					writeInvocationStatementToReplace, charSequenceArgument,
					bufferedIOInitializerMethodInvocation, resourceToRemove)
				.ifPresent(filesNewBufferedWriterInvocationDataList::add);
		} else {
			new WriteAnalyzerUsingBufferedWriterConstructor()
				.findTransformationDataUsingBufferedWriterConstructor(writeInvocationStatementToReplace,
						charSequenceArgument, bufferedWriterResourceAnalyzer)
				.ifPresent(bufferedWriterInstanceCreationDataList::add);
		}
		return true;
	}

	private boolean checkWriterVariableUsage(SimpleName writerVariableName,
			Block blockOfInvocationStatement) {
		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(
				writerVariableName);
		blockOfInvocationStatement.accept(visitor);
		int usages = visitor.getUsages()
			.size();
		return usages == 1;
	}

	boolean hasTransformationData() {
		return !filesNewBufferedWriterInvocationDataList.isEmpty() || !bufferedWriterInstanceCreationDataList.isEmpty();
	}

	List<FilesNewBufferedIOTransformationData> getFilesNewBufferedWriterInvocationDataList() {
		return filesNewBufferedWriterInvocationDataList;
	}

	List<UseFilesWriteStringAnalysisResult> getBufferedWriterInstanceCreationDataList() {
		return bufferedWriterInstanceCreationDataList;
	}

	List<VariableDeclarationExpression> getResourcesToRemove() {
		List<VariableDeclarationExpression> resourcesToRemove = new ArrayList<>();
		filesNewBufferedWriterInvocationDataList.stream()
			.map(FilesNewBufferedIOTransformationData::getResourceToRemove)
			.forEach(resourcesToRemove::add);

		bufferedWriterInstanceCreationDataList.stream()
			.map(UseFilesWriteStringAnalysisResult::getResourcesToRemove)
			.forEach(resourcesToRemove::addAll);

		return resourcesToRemove;
	}
}
