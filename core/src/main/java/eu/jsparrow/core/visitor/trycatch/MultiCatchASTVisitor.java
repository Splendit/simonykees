package eu.jsparrow.core.visitor.trycatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.UnionType;

import eu.jsparrow.core.matcher.BijectiveSimpleNameASTMatcher;
import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor finds duplicated catch-blocks and combines it to a
 * multi-catch-block
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class MultiCatchASTVisitor extends AbstractASTRewriteASTVisitor {

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(TryStatement node) {
		List<CatchClause> catchClauses = ASTNodeUtil.returnTypedList(node.catchClauses(), CatchClause.class);
		List<Block> blockList = catchClauses.stream()
			.map(CatchClause::getBody)
			.collect(Collectors.toList());
		boolean onRewriteTriggered = false;
		while (!blockList.isEmpty()) {
			boolean combined = false;
			/*
			 * start from the last block because it could be the most general
			 * one.
			 */
			Block reference = blockList.remove(blockList.size() - 1);
			SingleVariableDeclaration referenceException = ((CatchClause) reference.getParent()).getException();
			Type referenceExceptionType = referenceException.getType();

			List<Type> allNewTypes = new ArrayList<>();
			addTypesFromBlock(allNewTypes, referenceExceptionType);
			List<Type> jumpedTypes = new ArrayList<>();

			/*
			 * Iterate blocks in the reverse order as the bottom ones could be
			 * more generic then the above ones.
			 */
			for (int i = blockList.size() - 1; i >= 0; i--) {
				Block compareBlock = blockList.get(i);
				CatchClause compareCatch = (CatchClause) compareBlock.getParent();
				SingleVariableDeclaration compareException = compareCatch.getException();
				Type compareExceptionType = compareException.getType();

				if (reference.subtreeMatch(
						new BijectiveSimpleNameASTMatcher(referenceException.getName(), compareException.getName()),
						compareBlock) && !jumpsSuperType(compareExceptionType, jumpedTypes)) {
					combined = true;
					addTypesFromBlock(allNewTypes, compareExceptionType);
					astRewrite.remove(compareCatch, null);
					blockList.remove(i);
				} else {
					jumpedTypes.add(compareExceptionType);
				}
			}

			if (combined) {
				UnionType uniontype = node.getAST()
					.newUnionType();
				removeSubTypes(allNewTypes);
				allNewTypes.forEach(insertType -> uniontype.types()
					.add(astRewrite.createMoveTarget(insertType)));
				astRewrite.replace(referenceExceptionType, uniontype, null);
				if (!onRewriteTriggered) {
					onRewrite();
					onRewriteTriggered = true;
				}
			}

		}

		return true;
	}

	private boolean jumpsSuperType(Type compareExceptionType, List<Type> jumpedTypes) {
		return jumpedTypes.stream()
			.anyMatch(superType -> isSubType(superType, compareExceptionType));
	}

	private void removeSubTypes(List<Type> allNewTypes) {
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

		return isSubType(supertypeBinding, subtypeBinding.getSuperclass());
	}

	private boolean isSubType(ITypeBinding supertypeBinding, ITypeBinding subtypeBinding) {
		if (supertypeBinding == null || subtypeBinding == null) {
			return false;
		}

		// used for the recursion to check if there is a inheritance connection
		if (supertypeBinding.equals(subtypeBinding)) {
			return true;
		}

		return isSubType(supertypeBinding, subtypeBinding.getSuperclass());
	}

}
