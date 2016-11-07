package at.splendit.simonykees.core.visitor.tryStatement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.UnionType;

import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor finds duplicated catch-blocks and combines it to a
 * multi-catch-block
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
		Type referenceExceptionType = null;
		while (!blockList.isEmpty()) {
			boolean combined = false;
			Block reference = blockList.remove(0);
			referenceExceptionType = ((CatchClause) reference.getParent()).getException().getType();
			List<Type> allNewTypes = new ArrayList<>();
			addTypesFromBlock(allNewTypes, referenceExceptionType);
			for (Iterator<Block> blockIterator = blockList.iterator(); blockIterator.hasNext();) {
				Block compareBlock = blockIterator.next();
				if (reference.subtreeMatch(astMatcher, compareBlock)) {
					CatchClause compareCatch = (CatchClause) compareBlock.getParent();
					Type compareExceptionType = compareCatch.getException().getType();
					combined = true;
					addTypesFromBlock(allNewTypes, compareExceptionType);
					// addExceptionToHeader(allNewTypes, referenceExceptionType,
					// compareExceptionDeclaration.getType());
					astRewrite.remove(compareCatch, null);
					blockIterator.remove();
				}
			}
			if (combined) {
				UnionType uniontype = node.getAST().newUnionType();
				removeSubTypes(allNewTypes);
				for (Type insertType : allNewTypes) {
					uniontype.types().add(astRewrite.createMoveTarget(insertType));
				}
				astRewrite.replace(referenceExceptionType, uniontype, null);
			}

		}

		return true;
	}

	private void removeSubTypes(List<Type> allNewTypes) {
		// TODO remove the subtypes of the list
		List<Type> filtedTypes = new ArrayList<>();
		for (Type iterationType : allNewTypes) {
			boolean add = true;
			for (Iterator<Type> filterTypeIterator = filtedTypes.iterator(); filterTypeIterator.hasNext();) {
				Type filterType = filterTypeIterator.next();
				if (isSubType(filterType, iterationType)) {
					add = false;
					break;
				}
				if (isSubType(iterationType, filterType)) {
					filterTypeIterator.remove();
				}
			}
			if (add) {
				filtedTypes.add(iterationType);
			}
		}
		allNewTypes.clear();
		allNewTypes.addAll(filtedTypes);
	}

	@SuppressWarnings("unchecked")
	private void addTypesFromBlock(List<Type> allNewTypes, Type referenceExceptionType) {
		if (referenceExceptionType instanceof UnionType) {
			allNewTypes.addAll(((UnionType) referenceExceptionType).types());
		}
		if (referenceExceptionType instanceof SimpleType) {
			allNewTypes.add(referenceExceptionType);
		}
	}

	/**
	 * w.l.o.g. (o.b.d.a.) supertype != subtype if they are the same type the
	 * result is true, but they aren't really subtypes of each other
	 * 
	 * @param supertype
	 *            assumed to be supertype of subtype
	 * @param subtype
	 *            assumed to be subtype of supertype
	 * @return returns true if supertype is a really supertype of subtype.
	 */
	private boolean isSubType(Type supertype, Type subtype) {
		if (supertype == null || subtype == null) {
			return false;
		}

		ITypeBinding supertypeBinding = supertype.resolveBinding();
		ITypeBinding subtypeBinding = subtype.resolveBinding();

		if (supertypeBinding == null || subtypeBinding == null) {
			return false;
		}

		return false || isSubType(supertypeBinding, subtypeBinding.getSuperclass());
	}

	private boolean isSubType(ITypeBinding supertypeBinding, ITypeBinding subtypeBinding) {
		if (supertypeBinding == null || subtypeBinding == null) {
			return false;
		}

		// used for the recursion to check if there is a inheritance connection
		if (supertypeBinding.equals(subtypeBinding)) {
			return true;
		}

		return false || isSubType(supertypeBinding, subtypeBinding.getSuperclass());
	}

}
