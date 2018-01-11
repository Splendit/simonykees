package eu.jsparrow.core.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A visitor for collecting the comments.
 * 
 * @author Ardit Ymeri
 * @since 2.5.0
 */
public class CommentsASTVisitor extends ASTVisitor {

	private static final Logger logger = LoggerFactory.getLogger(CommentsASTVisitor.class);

	private Map<LineComment, String> lineComments = new HashMap<>();
	private Map<BlockComment, String> blockComments = new HashMap<>();
	private Map<Javadoc, String> javadocs = new HashMap<>();
	private String source;

	public CommentsASTVisitor() {

	}

	public void parseSource(CompilationUnit compilationUnit) {
		try {
			String source1 = ((ICompilationUnit) compilationUnit.getJavaElement()).getSource();
			setSource(source1);
		} catch (JavaModelException e) {
			logger.error("Cannot read the source of the compilation unit", e); //$NON-NLS-1$
		}
	}

	private void setSource(String source2) {
		this.source = source2;
	}

	@Override
	public boolean visit(LineComment lineComment) {
		String content = findContent(lineComment);
		lineComments.put(lineComment, content);
		return true;
	}

	@Override
	public boolean visit(BlockComment blockComment) {
		blockComments.put(blockComment, findContent(blockComment));
		return true;
	}

	@Override
	public boolean visit(Javadoc javaDoc) {
		javadocs.put(javaDoc, findContent(javaDoc));
		return true;
	}

	public Map<LineComment, String> getLineComments() {
		return this.lineComments;
	}

	public Map<BlockComment, String> getBlockComments() {
		return this.blockComments;
	}

	public Map<Javadoc, String> getJavadocs() {
		return this.javadocs;
	}
	
	private String findContent(Comment comment) {
		if(this.source == null) {
			return null;
		}
		
		int start = comment.getStartPosition();
		int length = comment.getLength();
		int end = start + length;
		if(source.length() < end) {
			return null;
		}
		
		return source.substring(start, end);
	}
}
