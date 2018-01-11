package eu.jsparrow.core.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;

/**
 * A visitor for collecting the comments. 
 * 
 * @author Ardit Ymeri
 * @since 2.5.0
 */
public class CommentsASTVisitor extends ASTVisitor {

	private List<LineComment> lineComments = new ArrayList<>();
	private List<BlockComment> blockComments = new ArrayList<>();
	private List<Javadoc> javadocs = new ArrayList<>();

	@Override
	public boolean visit(LineComment lineComment) {
		lineComments.add(lineComment);
		return true;
	}

	@Override
	public boolean visit(BlockComment blockComment) {
		blockComments.add(blockComment);
		return true;
	}

	@Override
	public boolean visit(Javadoc javaDoc) {
		javadocs.add(javaDoc);
		return true;
	}
	
	public List<LineComment> getLineComments() {
		return this.lineComments;
	}
	
	public List<BlockComment> getBlockComments() {
		return this.blockComments;
	}
	
	public List<Javadoc> getJavadocs() {
		return this.javadocs;
	}
}
