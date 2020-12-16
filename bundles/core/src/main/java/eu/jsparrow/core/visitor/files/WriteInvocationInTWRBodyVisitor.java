package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

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

	private final TryStatement tryStatement;
	private final List<FilesNewBufferedIOTransformationData> filesNewBufferedWriterInvocationDataList = new ArrayList<>();
	private final List<UseFilesWriteStringAnalysisResult> bufferedWriterInstanceCreationDataList = new ArrayList<>();
	List<VariableDeclarationExpression> resourcesToRemove = new ArrayList<>();

	public WriteInvocationInTWRBodyVisitor(TryStatement tryStatement) {
		this.tryStatement = tryStatement;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		WriteMethodInvocationAnalyzer writeInvocationAnalyzer = new WriteMethodInvocationAnalyzer();
		if (writeInvocationAnalyzer.analyze(tryStatement, methodInvocation)) {

			FilesNewBufferedIOTransformationData replacementDataWithFilesNewBufferedWriter = writeInvocationAnalyzer
				.getInvocationReplecementDataWithFilesMethod()
				.orElse(null);
			UseFilesWriteStringAnalysisResult replacementDataWithBufferedWriterConstructor = writeInvocationAnalyzer
				.getInvocationReplacementDataWithConstructor()
				.orElse(null);

			if (replacementDataWithFilesNewBufferedWriter != null) {
				filesNewBufferedWriterInvocationDataList.add(replacementDataWithFilesNewBufferedWriter);
				resourcesToRemove.addAll(writeInvocationAnalyzer.getResourcesToRemove());
			} else if (replacementDataWithBufferedWriterConstructor != null) {
				bufferedWriterInstanceCreationDataList.add(replacementDataWithBufferedWriterConstructor);
				resourcesToRemove.addAll(writeInvocationAnalyzer.getResourcesToRemove());
			}
		}
		return true;
	}

	List<FilesNewBufferedIOTransformationData> getFilesNewBufferedWriterInvocationDataList() {
		return filesNewBufferedWriterInvocationDataList;
	}

	List<UseFilesWriteStringAnalysisResult> getBufferedWriterInstanceCreationDataList() {
		return bufferedWriterInstanceCreationDataList;
	}

	List<VariableDeclarationExpression> getResourcesToRemove() {
		return resourcesToRemove;
	}
}
