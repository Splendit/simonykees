package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;

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
	private final WriteMethodInvocationAnalyzer writeInvocationAnalyzer;

	public WriteInvocationInTWRBodyVisitor(TryStatement tryStatement) {
		this.writeInvocationAnalyzer = new WriteMethodInvocationAnalyzer(tryStatement);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		writeInvocationAnalyzer.analyze(methodInvocation);
		return true;
	}
	
	WriteMethodInvocationAnalyzer getWriteInvocationAnalyzer() {
		return writeInvocationAnalyzer;
	}
}
