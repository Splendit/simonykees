package eu.jsparrow.core.visitor.loop.bufferedreader;

import java.io.BufferedReader;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * A helper visitor for finding and checking whether the {@link BufferedReader}
 * and the line variable are used for any purpose other than iterating through
 * the lines of a file with a while loop.
 * 
 * @since 3.3.0
 *
 */
public class BufferedReaderLinesPreconditionVisitor extends ASTVisitor {

	private Statement loop;
	private SimpleName line;
	private SimpleName bufferedReader;

	private boolean beforeLoop = true;
	private boolean inLoop = false;

	private boolean lineReferencesOutsideLoop;
	private boolean bufferReferencesBeforeLoop;

	private VariableDeclarationFragment lineDeclaration;
	private VariableDeclarationFragment bufferDeclaration;

	public BufferedReaderLinesPreconditionVisitor(Statement loop, SimpleName line, SimpleName bufferedReader) {
		this.loop = loop;
		this.line = line;
		this.bufferedReader = bufferedReader;
	}

	@Override
	public boolean visit(WhileStatement whileStatement) {
		updateInLoopState(whileStatement);
		return true;
	}

	@Override
	public void endVisit(WhileStatement whileStatement) {
		endInLoopStatement(whileStatement);
	}

	@Override
	public boolean visit(ForStatement forStatement) {
		updateInLoopState(forStatement);
		return true;
	}

	@Override
	public void endVisit(ForStatement forStatement) {
		endInLoopStatement(forStatement);
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {
		if (!beforeLoop && !inLoop) {
			return true;
		}
		SimpleName name = fragment.getName();
		String identifier = name.getIdentifier();
		if (identifier.equals(line.getIdentifier())) {
			lineDeclaration = fragment;
			lineReferencesOutsideLoop = false;
		}

		if (identifier.equals(bufferedReader.getIdentifier())) {
			bufferDeclaration = fragment;
			bufferReferencesBeforeLoop = false;

		}
		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {

		String identifier = simpleName.getIdentifier();
		if (!identifier.equals(line.getIdentifier()) && !identifier.equals(bufferedReader.getIdentifier())) {
			return false;
		}

		StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		if (locationInParent == VariableDeclarationFragment.NAME_PROPERTY
				|| locationInParent == FieldAccess.NAME_PROPERTY || locationInParent == QualifiedName.NAME_PROPERTY) {
			return false;
		}

		IBinding binding = simpleName.resolveBinding();
		if (binding == null || IBinding.VARIABLE != binding.getKind()) {
			return false;
		}

		if (beforeLoop && bufferDeclaration != null && identifier.equals(bufferedReader.getIdentifier())) {
			bufferReferencesBeforeLoop = true;
		}

		if (!inLoop && lineDeclaration != null && identifier.equals(line.getIdentifier())) {
			lineReferencesOutsideLoop = true;
		}

		return true;
	}

	private void updateInLoopState(Statement statement) {
		if (statement == loop) {
			inLoop = true;
			beforeLoop = false;
		}
	}

	private void endInLoopStatement(Statement whileStatement) {
		if (whileStatement == loop) {
			inLoop = false;
		}
	}

	/**
	 * Checks if the declaration of both {@link BufferedReader} and loop
	 * variable were found. Additionally, checks the following:
	 * 
	 * <ul>
	 * <li>the loop variable is not referenced outside the loop body.</li>
	 * <li>the buffered reader is not referenced neither inside, nor outside the
	 * loop body</li>
	 * <li>in case the loop is a {@link ForStatement} then the line declaration
	 * should either be a {@link VariableDeclarationExpression} with at most one
	 * fragment or a {@link VariableDeclarationStatement}. In the latter case,
	 * the loop must have empty initializers.</li>
	 * </ul>
	 * 
	 * @return if all the aforementioned conditions are met.
	 */
	public boolean isSatisfied() {

		if (lineDeclaration == null) {
			return false;
		}

		if (lineDeclaration.getLocationInParent() == VariableDeclarationExpression.FRAGMENTS_PROPERTY) {
			VariableDeclarationExpression declarationExpression = (VariableDeclarationExpression) lineDeclaration
				.getParent();
			if (declarationExpression.fragments()
				.size() > 1) {
				return false;
			}
		}

		if (loop.getNodeType() == ASTNode.FOR_STATEMENT) {
			ForStatement forLoop = (ForStatement) loop;
			if (lineDeclaration.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY
					&& !forLoop.initializers()
						.isEmpty()) {
				return false;
			}
		}

		return bufferDeclaration != null && !lineReferencesOutsideLoop && !bufferReferencesBeforeLoop;
	}

	/**
	 * 
	 * @return the {@link VariableDeclarationFragment} of the loop variable
	 *         which is used to hold the line value for each loop cycle.
	 */
	public VariableDeclarationFragment getLineDeclaration() {
		return this.lineDeclaration;
	}

}
