package at.splendit.simonykees.core.visitor.tryWithResource;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.UnionType;

import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor finds duplicated catch-blocks and combines it to a multi-catch-block
 * 
 * @author Martin Huter
 *
 */
public class MultiCatchASTVisitor extends AbstractASTRewriteASTVisitor {

	// TODO: match exceptions with different name in header
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(TryStatement node) {
		List<CatchClause> catchClauses = (List<CatchClause>) node.catchClauses();
		List<Block> blockList = catchClauses.stream().map(catchClase -> catchClase.getBody())
				.collect(Collectors.toList());
		ASTMatcher astMatcher = new ASTMatcher();
		while (!blockList.isEmpty()) {
			Block reference = blockList.remove(0);
			Type referenceExceptionType = ((CatchClause) reference.getParent()).getException().getType();
			for (Iterator<Block> blockIterator = blockList.iterator(); blockIterator.hasNext();) {
				Block compareBlock = blockIterator.next();
				if (reference.subtreeMatch(astMatcher, compareBlock)) {
					CatchClause compareCatch = (CatchClause) compareBlock.getParent();
					SingleVariableDeclaration compareExceptionDeclaration = compareCatch.getException();
					if (referenceExceptionType instanceof UnionType) {
						astRewrite.getListRewrite((UnionType) referenceExceptionType, UnionType.TYPES_PROPERTY)
								.insertLast(astRewrite.createMoveTarget(compareExceptionDeclaration.getType()), null);

					} else if (referenceExceptionType instanceof SimpleType) {
						// Convert to UnionType
						UnionType uniontype = node.getAST().newUnionType();
						uniontype.types().add(astRewrite.createMoveTarget(referenceExceptionType));
						uniontype.types().add(astRewrite.createMoveTarget(compareExceptionDeclaration.getType()));
						astRewrite.replace(referenceExceptionType, uniontype, null);
						referenceExceptionType = uniontype;
					}
					astRewrite.remove(compareBlock.getParent(), null);
					blockIterator.remove();
				}
			}
		}
		return true;
	}
}
